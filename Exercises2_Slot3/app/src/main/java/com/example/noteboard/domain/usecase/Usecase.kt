package com.example.noteboard.domain.usecase

import com.example.noteboard.domain.model.Note
import com.example.noteboard.domain.repository.NoteRepository

class AddNoteUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(content: String): Note = repo.add(content)
}

class DeleteNoteUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(noteId: Long): Note? = repo.delete(noteId)
}

class RestoreNoteUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(note: Note) = repo.restore(note)
}
