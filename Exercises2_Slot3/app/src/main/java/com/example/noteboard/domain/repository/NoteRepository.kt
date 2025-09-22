package com.example.noteboard.domain.repository

import com.example.noteboard.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    val notes: Flow<List<Note>>
    suspend fun add(content: String): Note
    suspend fun delete(noteId: Long): Note?
    suspend fun restore(note: Note)
    suspend fun toggleSelected(noteId: Long, selected: Boolean)
}
