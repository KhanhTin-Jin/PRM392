package com.example.personaldiary.data.repo

import android.util.Log
import com.example.personaldiary.data.local.Note
import com.example.personaldiary.data.local.NoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.CancellationException // Import the correct exception

class NoteRepository(private val dao: NoteDao) {
    private val TAG = "NoteRepository"

    fun getAll(): Flow<List<Note>> = dao.getAll()
        .onStart {
            Log.d(TAG, "getAll() START - querying database")
        }
        .onEach { list ->
            Log.d(TAG, "getAll() onEach called with ${list.size} items")
            Log.d(TAG, "getAll() RESULT - returned ${list.size} notes from DAO")
            list.take(3).forEachIndexed { idx, note ->
                Log.d(TAG, "  getAll() item[$idx]: id=${note.id}, title='${note.title}'")
            }
        }
        .onCompletion { cause ->
            when {
                // Check for the general CancellationException
                cause is CancellationException -> {
                    Log.d(TAG, "getAll() cancelled (collector/lifecycle)")
                }
                cause != null -> {
                    Log.e(TAG, "getAll() COMPLETED with error: ${cause.message}", cause)
                }
                else -> {
                    Log.d(TAG, "getAll() COMPLETED successfully")
                }
            }
        }

    fun searchByTitle(q: String): Flow<List<Note>> = dao.searchByTitle(q)
        .onStart {
            Log.d(TAG, "searchByTitle('$q') START")
        }
        .onEach {
            Log.d(TAG, "searchByTitle('$q') RESULT - returned ${it.size} notes from DAO")
        }
        .onCompletion { cause ->
            when {
                // Check for the general CancellationException
                cause is CancellationException -> {
                    Log.d(TAG, "searchByTitle('$q') cancelled (collector/lifecycle)")
                }
                cause != null -> {
                    Log.e(TAG, "searchByTitle('$q') COMPLETED with error: ${cause.message}", cause)
                }
                else -> {
                    Log.d(TAG, "searchByTitle('$q') COMPLETED successfully")
                }
            }
        }

    suspend fun getById(id: Long): Note? {
        val note = dao.getById(id)
        Log.d(TAG, "getById($id) = ${note?.title ?: "null"}")
        return note
    }

    suspend fun add(title: String, content: String) {
        val now = System.currentTimeMillis()
        val note = Note(title = title.trim(), content = content, createdAt = now, updatedAt = now)
        dao.insert(note)
        Log.d(TAG, "Added note: title='${note.title}'")
    }

    suspend fun update(id: Long, title: String, content: String) {
        val ex = dao.getById(id) ?: return
        val updated = ex.copy(title = title.trim(), content = content, updatedAt = System.currentTimeMillis())
        dao.update(updated)
        Log.d(TAG, "Updated note id=$id: title='${updated.title}'")
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
        Log.d(TAG, "Deleted note id=$id")
    }
}