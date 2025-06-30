package com.example.tnotes.domain.repository

import com.example.tnotes.data.local.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    suspend fun insertNote(note: Note): Long

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

    fun getNoteById(noteId: Int): Flow<Note?>

    fun getAllNotes(): Flow<List<Note>>

    fun searchNotes(query: String): Flow<List<Note>>
}
