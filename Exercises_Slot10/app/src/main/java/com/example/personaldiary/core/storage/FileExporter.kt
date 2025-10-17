package com.example.personaldiary.core.storage

import android.content.Context
import java.io.File

object FileExporter {
    fun exportSingleNoteToInternal(context: Context, id: Long, title: String, content: String): File {
        val dir = File(context.filesDir, "notes").apply { if (!exists()) mkdirs() }
        val safeTitle = title.ifBlank { "note" }.replace(Regex("[^A-Za-z0-9-_ ]"), "_")
        val file = File(dir, "note_${id}_${safeTitle}.txt")
        file.writeText(buildString {
            appendLine(title)
            appendLine("-----")
            append(content)
        }, Charsets.UTF_8)
        return file
    }
}
