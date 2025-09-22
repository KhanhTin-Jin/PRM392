package com.example.noteboard.data

import com.example.noteboard.domain.model.Note
import com.example.noteboard.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class NoteRepositoryImpl(
    private val ds: InMemoryDataSource = InMemoryDataSource()
) : NoteRepository {

    override val notes: Flow<List<Note>> = ds.notes

    override suspend fun add(content: String): Note = ds.add(content)

    override suspend fun delete(noteId: Long): Note? = ds.delete(noteId)

    override suspend fun restore(note: Note) = ds.restore(note)

    override suspend fun toggleSelected(noteId: Long, selected: Boolean) = ds.toggleSelected(noteId, selected)
}
