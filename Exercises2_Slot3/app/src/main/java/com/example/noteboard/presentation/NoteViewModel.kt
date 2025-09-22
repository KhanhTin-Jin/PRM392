package com.example.noteboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteboard.data.NoteRepositoryImpl
import com.example.noteboard.domain.model.Note
import com.example.noteboard.domain.usecase.AddNoteUseCase
import com.example.noteboard.domain.usecase.DeleteNoteUseCase
import com.example.noteboard.domain.usecase.RestoreNoteUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NoteUiState(
    val input: String = "",
    val notes: List<Note> = emptyList(),
    val lastDeleted: Note? = null,
    val isDragging: Boolean = false,
)

class NoteViewModel : ViewModel() {

    // DI đơn giản
    private val repo = NoteRepositoryImpl()
    private val addNote = AddNoteUseCase(repo)
    private val deleteNote = DeleteNoteUseCase(repo)
    private val restoreNote = RestoreNoteUseCase(repo)

    private val _state = MutableStateFlow(NoteUiState())
    val state: StateFlow<NoteUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.notes.collect { list ->
                _state.update { it.copy(notes = list) }
            }
        }
    }

    fun onInputChange(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun add() = viewModelScope.launch {
        val text = state.value.input.trim()
        if (text.isNotEmpty()) {
            addNote(text)
            _state.update { it.copy(input = "") }
        }
    }

    fun toggleSelected(noteId: Long, selected: Boolean) = viewModelScope.launch {
        repo.toggleSelected(noteId, selected)
    }

    fun delete(noteId: Long) = viewModelScope.launch {
        val deleted = deleteNote(noteId)
        _state.update { it.copy(lastDeleted = deleted) }
    }

    fun undoDelete() = viewModelScope.launch {
        state.value.lastDeleted?.let { restoreNote(it) }
        _state.update { it.copy(lastDeleted = null) }
    }

    fun setDragging(dragging: Boolean) {
        _state.update { it.copy(isDragging = dragging) }
    }
}
