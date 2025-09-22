package com.example.noteboard.data

import com.example.noteboard.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryDataSource {
    private var autoId = 0L
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    fun add(content: String): Note {
        val note = Note(id = ++autoId, content = content)
        _notes.value = _notes.value + note
        return note
    }

    fun delete(noteId: Long): Note? {
        val current = _notes.value
        val note = current.firstOrNull { it.id == noteId } ?: return null
        _notes.value = current.filterNot { it.id == noteId }
        return note
    }

    fun restore(note: Note) {
        _notes.value = _notes.value + note.copy(isSelected = false)
    }

    fun toggleSelected(noteId: Long, selected: Boolean) {
        _notes.value = _notes.value.map { if (it.id == noteId) it.copy(isSelected = selected) else it }
    }
}
