package com.example.personaldiary.core.storage

import android.content.Context
import android.net.Uri

object SafIO {
    fun writeText(context: Context, uri: Uri, text: String) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(text.toByteArray(Charsets.UTF_8))
            out.flush()
        } ?: error("Cannot open output stream")
    }
}