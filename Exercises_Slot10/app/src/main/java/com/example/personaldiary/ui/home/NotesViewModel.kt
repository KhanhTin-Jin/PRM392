package com.example.personaldiary.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.personaldiary.data.local.Note
import com.example.personaldiary.data.repo.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(private val repo: NoteRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    
    val notes: StateFlow<List<Note>> =
        query
            .debounce(150)
            .flatMapLatest { q -> 
                Log.d("NotesViewModel", "🔍 flatMapLatest triggered with query: '$q'")
                if (q.isBlank()) {
                    Log.d("NotesViewModel", "📋 Calling repo.getAll()")
                    repo.getAll()
                } else {
                    Log.d("NotesViewModel", "🔍 Calling repo.searchByTitle('$q')")
                    repo.searchByTitle(q)
                }
            }
            .catch { throwable ->
                // Log nhẹ nhàng, không crash
                Log.w("NotesViewModel", "Flow error: ${throwable.javaClass.simpleName}")
                emit(emptyList())
            }
            .onEach { list ->
                Log.d("NotesViewModel", "📋 Flow emitted ${list.size} notes")
            }
            // ✅ viewModelScope thay vì GlobalScope
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { 
        Log.d("NotesViewModel", "setQuery('$q')")
        query.value = q 
    }
    
    fun refreshAfterBackup() {
        // Đơn giản hoá: chỉ re-emit query hiện tại
        viewModelScope.launch {
            val current = query.value
            query.emit(current)
        }
    }

    fun delete(id: Long) {
        Log.d("NotesViewModel", "🗑️ Deleting note id=$id")
        viewModelScope.launch { 
            repo.delete(id) 
        }
    }

    class Factory(private val repo: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NotesViewModel(repo) as T
    }
}
