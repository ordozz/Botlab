package com.example.tnotes.repository

import com.example.tnotes.data.local.model.Note
import com.example.tnotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Mock implementation of NoteRepository for Unit Tests.
 * Allows controlling the data emitted by flows.
 */
class MockTestNoteRepository : NoteRepository {

    private val notesFlow = MutableSharedFlow<List<Note>>(replay = 1)
    // private val singleNoteFlow = MutableSharedFlow<Note?>(replay = 1) // Not used currently
    private val notesList = mutableListOf<Note>()
    private var nextId: Long = 1

    private var deleteShouldThrowError: Boolean = false
    private var deleteErrorMessage: String = "Unknown delete error"

    fun prepareToDeleteWithError(message: String) {
        deleteShouldThrowError = true
        deleteErrorMessage = message
    }

    suspend fun emitNotes(notes: List<Note>) {
        notesFlow.emit(notes)
    }

    suspend fun emitSingleNote(note: Note?) {
        singleNoteFlow.emit(note)
    }

    fun setInitialNotes(initialNotes: List<Note>) {
        notesList.clear()
        notesList.addAll(initialNotes)
        nextId = (initialNotes.maxOfOrNull { it.id.toLong() } ?: 0L) + 1
        // Emit initial state to flow if needed for tests that collect immediately
        // notesFlow.tryEmit(notesList.toList())
    }


    override suspend fun insertNote(note: Note): Long {
        val idToAssign = if (note.id != 0) note.id.toLong() else nextId++
        val newNote = note.copy(id = idToAssign.toInt())
        notesList.removeAll { it.id == newNote.id } // Remove if exists, effectively an upsert
        notesList.add(newNote)
        notesFlow.emit(notesList.toList().sortedByDescending { it.timestamp })
        return idToAssign
    }

    override suspend fun updateNote(note: Note) {
        val index = notesList.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notesList[index] = note
            notesFlow.emit(notesList.toList().sortedByDescending { it.timestamp })
        } else {
            // Optionally handle case where note to update is not found, e.g., throw exception
        }
    }

    override suspend fun deleteNote(note: Note) {
        if (deleteShouldThrowError) {
            deleteShouldThrowError = false // Reset for next call
            throw RuntimeException(deleteErrorMessage)
        }
        notesList.removeIf { it.id == note.id }
        notesFlow.emit(notesList.toList().sortedByDescending { it.timestamp })
    }

    override fun getNoteById(noteId: Int): Flow<Note?> {
        // For more control, this could also be a MutableSharedFlow if tests need to change it
        return flowOf(notesList.find { it.id == noteId })
        // return singleNoteFlow
    }

    override fun getAllNotes(): Flow<List<Note>> {
        // Ensure it emits current list if not already done by other operations
        if (notesFlow.replayCache.isEmpty()) {
            notesFlow.tryEmit(notesList.toList().sortedByDescending { it.timestamp })
        }
        return notesFlow
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        val filtered = notesList.filter {
            it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
        }.sortedByDescending { it.timestamp }
        return flowOf(filtered)
    }
}
