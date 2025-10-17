// com/example/personaldiary/core/storage/DbBackupHelper.kt
package com.example.personaldiary.core.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.personaldiary.data.local.DiaryDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

object DbBackupHelper {
    private const val TAG = "DbBackupHelper"

    /** Backup local DB → Uri do SAF chọn - WAL-safe version */
    fun backupDbToUri(context: Context, dest: Uri) {
        Log.d(TAG, "🔄 Starting WAL-safe backup...")
        DiaryDatabase.debugLogPath(context)

        // 1) Đóng DB để bỏ lock và flush WAL trước khi copy
        DiaryDatabase.closeAndClear()

        val dbFile = DiaryDatabase.filePath(context)
        val walFile = File(dbFile.parent, "${dbFile.name}-wal")
        val shmFile = File(dbFile.parent, "${dbFile.name}-shm")

        require(dbFile.exists()) { "DB file không tồn tại: ${dbFile.absolutePath}" }

        Log.d(TAG, "📁 Checking WAL files...")
        Log.d(TAG, "  Main DB: ${dbFile.absolutePath} (${dbFile.length()} bytes, exists=${dbFile.exists()})")
        Log.d(TAG, "  WAL file: ${walFile.absolutePath} (${walFile.length()} bytes, exists=${walFile.exists()})")
        Log.d(TAG, "  SHM file: ${shmFile.absolutePath} (${shmFile.length()} bytes, exists=${shmFile.exists()})")

        // Create backup bundle with all DB files
        val backupData = createWalSafeBackup(dbFile, walFile, shmFile)
        
        context.contentResolver.openOutputStream(dest, "w").use { out ->
            requireNotNull(out) { "Không mở được OutputStream cho Uri đích" }
            out.write(backupData)
            out.flush()
        }

        // (tuỳ chọn) giữ quyền ghi đọc Uri về sau để ghi đè dễ hơn
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(dest, flags)
        } catch (_: Throwable) {}

        Log.d(TAG, "✅ WAL-safe backup completed -> ${dest}")
    }

    /** Restore từ Uri (SAF) → ghi đè DB hiện tại - WAL-safe version */
    fun restoreDbFromUri(context: Context, src: Uri) {
        Log.d(TAG, "🔄 Starting WAL-safe restore...")
        DiaryDatabase.debugLogPath(context)

        // 1) Đóng DB trước khi ghi đè
        DiaryDatabase.closeAndClear()

        val dbFile = DiaryDatabase.filePath(context)
        val walFile = File(dbFile.parent, "${dbFile.name}-wal")
        val shmFile = File(dbFile.parent, "${dbFile.name}-shm")
        val dbFileBackup = File(dbFile.parent, "${dbFile.name}.backup")
        
        // 2) Backup ALL existing files for rollback
        val backupFiles = mutableListOf<Pair<File, File>>()
        try {
            listOf(dbFile, walFile, shmFile).forEach { file ->
                if (file.exists()) {
                    val backup = File(file.parent, "${file.name}.backup")
                    file.copyTo(backup, overwrite = true)
                    backupFiles.add(file to backup)
                    Log.d(TAG, "💾 Backed up: ${file.name} → ${backup.name}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup existing files", e)
        }

        // 3) Read backup data from Uri
        val backupData: ByteArray
        var bytesRead = 0L
        try {
            context.contentResolver.openInputStream(src).use { inStream ->
                requireNotNull(inStream) { "Không mở được InputStream từ Uri nguồn" }
                backupData = inStream.readBytes()
                bytesRead = backupData.size.toLong()
            }
            
            Log.d(TAG, "📥 Read backup data: $bytesRead bytes")
            
            // Check if it's a WAL-safe backup or legacy single-file backup
            val header = String(backupData.sliceArray(0..9))
            if (header.startsWith("WALBACKUP")) {
                Log.d(TAG, "📦 Detected WAL-safe backup format")
                restoreWalSafeBackup(context, backupData)
            } else {
                Log.d(TAG, "📄 Detected legacy single-file backup, treating as main DB only")
                // Legacy restore: just write to main DB file
                dbFile.writeBytes(backupData)
                
                // Verify SQLite header
                if (backupData.size < 16) {
                    throw IllegalStateException("File quá nhỏ, không phải DB hợp lệ: ${backupData.size} bytes")
                }
                val sqliteHeader = String(backupData.sliceArray(0..15), Charsets.ISO_8859_1)
                if (!sqliteHeader.startsWith("SQLite format")) {
                    throw IllegalStateException("File không phải SQLite database hợp lệ: $sqliteHeader")
                }
                Log.d(TAG, "✅ Valid SQLite header: $sqliteHeader")
            }
            
            Log.d(TAG, "✅ WAL-safe restore completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Restore failed, rolling back all files...", e)
            // Rollback ALL files
            backupFiles.forEach { (original, backup) ->
                if (backup.exists()) {
                    backup.copyTo(original, overwrite = true)
                    Log.d(TAG, "↩️ Rolled back: ${backup.name} → ${original.name}")
                }
            }
            throw e
        } finally {
            // Clean up ALL backup files
            backupFiles.forEach { (_, backup) -> backup.delete() }
        }

        // (tuỳ chọn) giữ quyền Uri src
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(src, flags)
        } catch (_: Throwable) {}
    }

    /** Create WAL-safe backup with all 3 database files */
    private fun createWalSafeBackup(dbFile: File, walFile: File, shmFile: File): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        
        // Header: "WALBACKUP" + version
        output.write("WALBACKUP1".toByteArray())
        
        // Write main DB file
        writeFileToBackup(output, "main", dbFile)
        
        // Write WAL file if exists
        if (walFile.exists() && walFile.length() > 0) {
            writeFileToBackup(output, "wal", walFile)
        }
        
        // Write SHM file if exists  
        if (shmFile.exists() && shmFile.length() > 0) {
            writeFileToBackup(output, "shm", shmFile)
        }
        
        // End marker
        output.write("END".toByteArray())
        
        Log.d(TAG, "📦 Created backup bundle: ${output.size()} bytes")
        return output.toByteArray()
    }
    
    /** Write a single file to backup stream with metadata */
    private fun writeFileToBackup(output: java.io.ByteArrayOutputStream, type: String, file: File) {
        if (!file.exists()) return
        
        val data = file.readBytes()
        Log.d(TAG, "  📄 Adding $type file: ${file.name} (${data.size} bytes)")
        
        // File header: type(4) + size(8) + data
        output.write(type.padEnd(4).toByteArray().sliceArray(0..3))
        output.write(longToBytes(data.size.toLong()))
        output.write(data)
    }
    
    /** Restore WAL-safe backup from bundle */
    private fun restoreWalSafeBackup(context: Context, backupData: ByteArray) {
        val input = java.io.ByteArrayInputStream(backupData)
        
        // Read and verify header
        val header = ByteArray(10)
        input.read(header)
        val headerStr = String(header)
        if (!headerStr.startsWith("WALBACKUP")) {
            throw IllegalStateException("Invalid backup format: $headerStr")
        }
        Log.d(TAG, "📦 Valid backup header: $headerStr")
        
        val dbFile = DiaryDatabase.filePath(context)
        val walFile = File(dbFile.parent, "${dbFile.name}-wal")
        val shmFile = File(dbFile.parent, "${dbFile.name}-shm")
        
        // Delete existing files
        Log.d(TAG, "🗑️ Cleaning existing DB files...")
        listOf(dbFile, walFile, shmFile).forEach { file ->
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "   Deleted: ${file.name}")
            }
        }
        
        // Read files from backup
        while (input.available() > 0) {
            val typeBytes = ByteArray(4)
            if (input.read(typeBytes) != 4) break
            
            val type = String(typeBytes).trim()
            if (type == "END") break
            
            val sizeBytes = ByteArray(8)
            input.read(sizeBytes)
            val size = bytesToLong(sizeBytes)
            
            val data = ByteArray(size.toInt())
            input.read(data)
            
            val targetFile = when (type) {
                "main" -> dbFile
                "wal" -> walFile
                "shm" -> shmFile
                else -> {
                    Log.w(TAG, "Unknown file type: $type")
                    continue
                }
            }
            
            targetFile.writeBytes(data)
            Log.d(TAG, "✅ Restored $type file: ${targetFile.name} (${data.size} bytes)")
        }
    }
    
    /** Helper: Convert long to byte array */
    private fun longToBytes(value: Long): ByteArray {
        val bytes = ByteArray(8)
        for (i in 0..7) {
            bytes[i] = (value shr (8 * (7 - i))).toByte()
        }
        return bytes
    }
    
    /** Helper: Convert byte array to long */
    private fun bytesToLong(bytes: ByteArray): Long {
        var value = 0L
        for (i in 0..7) {
            value = (value shl 8) or (bytes[i].toLong() and 0xFF)
        }
        return value
    }
}
