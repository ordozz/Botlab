package com.example.tnotes.ui.screens

import com.example.tnotes.data.local.model.Note
import com.example.tnotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mock implementation of [NoteRepository] for use in Composable Previews.
 */
class MockNoteRepository : NoteRepository {
    private val notes = mutableListOf<Note>()

    override suspend fun insertNote(note: Note): Long {
        val newId = (notes.maxOfOrNull { it.id } ?: 0) + 1
        notes.add(note.copy(id = newId.toInt()))
        return newId
    }

    override suspend fun updateNote(note: Note) {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index] = note
        }
    }

    override suspend fun deleteNote(note: Note) {
        notes.removeIf { it.id == note.id }
    }

    override fun getNoteById(noteId: Int): Flow<Note?> {
        return flowOf(notes.find { it.id == noteId })
    }

    override fun getAllNotes(): Flow<List<Note>> {
        // Simulate some data for previews if needed
        if (notes.isEmpty()) {
            notes.addAll(listOf(
                Note(1, "Preview Note 1", "Content for preview note 1", System.currentTimeMillis()),
                Note(2, "Preview Note 2", "Content for preview note 2", System.currentTimeMillis() - 100000)
            ))
        }
        return flowOf(notes.toList())
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return flowOf(notes.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) })
    }
}
