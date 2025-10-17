// com/example/personaldiary/data/local/DiaryDatabase.kt
package com.example.personaldiary.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

@Database(entities = [Note::class], version = 1, exportSchema = true)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        private const val TAG = "DiaryDatabase"
        private const val DB_NAME = "diary.db"

        @Volatile private var INSTANCE: DiaryDatabase? = null

        fun get(context: Context): DiaryDatabase =
            INSTANCE ?: synchronized(this) {
                Log.d(TAG, "DB.get() called - creating/getting instance")
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    DB_NAME
                )
                    // ❗ Dùng TRUNCATE để không tạo wal/shm → copy 1 file là đủ
//                    .setJournalMode(JournalMode.TRUNCATE)
                    // .fallbackToDestructiveMigration() // (tuỳ chọn cho demo)
                    .build()
                    .also { db ->
                        INSTANCE = db
                        Log.d(TAG, "DB opened at: ${filePath(context).absolutePath}, isOpen=${db.isOpen}, inTransaction=${db.inTransaction()}")
                    }
            }

        fun filePath(context: Context): File = context.getDatabasePath(DB_NAME)

        /** Đóng DB (nếu mở) và xoá singleton để lần sau get() mở lại từ file hiện tại */
        fun closeAndClear() {
            synchronized(this) {
                try { 
                    INSTANCE?.close()
                    Log.d(TAG, "DB closed")
                } catch (e: Throwable) {
                    Log.e(TAG, "Error closing DB", e)
                }
                INSTANCE = null
            }
        }

        /** Log đường dẫn DB (giúp chắc chắn backup/restore đúng file) */
        fun debugLogPath(context: Context) {
            Log.d(TAG, "DB path = ${filePath(context).absolutePath}")
        }
    }
}
