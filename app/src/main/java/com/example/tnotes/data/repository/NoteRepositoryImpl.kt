package com.example.tnotes.data.repository

import com.example.tnotes.data.local.dao.NoteDao
import com.example.tnotes.data.local.model.Note
import com.example.tnotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // To ensure only one instance of the repository
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
    // In the future, a remote data source (e.g., NoteApiService) could be injected here
) : NoteRepository {

    override suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    override fun getNoteById(noteId: Int): Flow<Note?> {
        return noteDao.getNoteById(noteId)
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes()
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        // Add wildcards for LIKE query
        val searchQuery = "%${query.trim()}%"
        return noteDao.searchNotes(searchQuery)
    }
}
