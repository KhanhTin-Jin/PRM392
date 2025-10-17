    package com.example.personaldiary.core.storage

    import com.example.personaldiary.data.local.Note
    import java.text.SimpleDateFormat
    import java.util.Date
    import java.util.Locale

    object ExportAllBuilder {
        fun build(notes: List<Note>): String {
            val fmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            return notes.joinToString("\n\n====================\n\n") { n ->
                """
                Title: ${n.title}
                Date: ${fmt.format(Date(n.updatedAt))}
                -----
                ${n.content}
                """.trimIndent()
            }
        }
    }