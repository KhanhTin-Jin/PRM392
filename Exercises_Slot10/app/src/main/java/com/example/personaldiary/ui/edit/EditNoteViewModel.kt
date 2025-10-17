package com.example.personaldiary.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personaldiary.data.repo.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditNoteViewModel(
    private val repo: NoteRepository,
    private val noteId: Long      // ✅ nhận trực tiếp
) : ViewModel() {

    val isEditing: Boolean get() = noteId != -1L
    val currentId: Long get() = noteId  // ✅ tiện cho Export Internal

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    init {
        if (isEditing) {
            viewModelScope.launch {
                val n = repo.getById(noteId)
                _title.value = n?.title.orEmpty()
                _content.value = n?.content.orEmpty()
            }
        }
    }

    fun updateTitle(v: String) { _title.value = v }
    fun updateContent(v: String) { _content.value = v }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            if (!isEditing) repo.add(title.value, content.value)
            else repo.update(noteId, title.value, content.value)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        if (!isEditing) return
        viewModelScope.launch {
            repo.delete(noteId)
            onDone()
        }
    }

    class Factory(
        private val repo: NoteRepository,
        private val noteId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditNoteViewModel(repo, noteId) as T
    }
}
