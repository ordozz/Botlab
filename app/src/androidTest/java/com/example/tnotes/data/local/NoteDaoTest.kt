package com.example.tnotes.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.tnotes.data.local.dao.NoteDao
import com.example.tnotes.data.local.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat // For assertions

@RunWith(AndroidJUnit4::class)
@SmallTest // Indicates these are unit tests for a small component, run quickly
class NoteDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule() // For LiveData if used, good for Room's async queries

    private lateinit var database: NoteDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NoteDatabase::class.java
        ).allowMainThreadQueries().build() // allowMainThreadQueries for testing only
        noteDao = database.noteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertNote_retrievesSameNoteById() = runTest { // Use runTest for coroutines
        val note = Note(id = 1, title = "Test Title", content = "Test Content", timestamp = System.currentTimeMillis())
        noteDao.insertNote(note)

        val retrievedNote = noteDao.getNoteById(1).first() // Use first() to get the first emitted value from Flow
        assertThat(retrievedNote).isNotNull()
        assertThat(retrievedNote?.title).isEqualTo(note.title)
        assertThat(retrievedNote?.content).isEqualTo(note.content)
    }

    @Test
    fun getAllNotes_whenMultipleNotesInserted_returnsAllNotesSortedByTimestampDesc() = runTest {
        val note1 = Note(id = 1, title = "Note 1", content = "Content 1", timestamp = 1000L)
        val note2 = Note(id = 2, title = "Note 2", content = "Content 2", timestamp = 2000L) // Newer
        val note3 = Note(id = 3, title = "Note 3", content = "Content 3", timestamp = 500L)  // Older

        noteDao.insertNote(note1)
        noteDao.insertNote(note2)
        noteDao.insertNote(note3)

        val allNotes = noteDao.getAllNotes().first()
        assertThat(allNotes.size).isEqualTo(3)
        assertThat(allNotes[0]).isEqualTo(note2) // note2 is newest
        assertThat(allNotes[1]).isEqualTo(note1)
        assertThat(allNotes[2]).isEqualTo(note3) // note3 is oldest
    }

    @Test
    fun updateNote_changesArePersisted() = runTest {
        val originalNote = Note(id = 1, title = "Original Title", content = "Original Content", timestamp = System.currentTimeMillis())
        noteDao.insertNote(originalNote)

        val updatedNote = originalNote.copy(title = "Updated Title", content = "Updated Content")
        noteDao.updateNote(updatedNote)

        val retrievedNote = noteDao.getNoteById(1).first()
        assertThat(retrievedNote?.title).isEqualTo("Updated Title")
        assertThat(retrievedNote?.content).isEqualTo("Updated Content")
    }

    @Test
    fun deleteNote_noteIsRemovedFromDatabase() = runTest {
        val note = Note(id = 1, title = "Test Delete", content = "Content Delete", timestamp = System.currentTimeMillis())
        noteDao.insertNote(note)

        var retrievedNote = noteDao.getNoteById(1).first()
        assertThat(retrievedNote).isNotNull()

        noteDao.deleteNote(note)
        retrievedNote = noteDao.getNoteById(1).first()
        assertThat(retrievedNote).isNull()
    }

    @Test
    fun searchNotes_findsMatchingNotesByTitle() = runTest {
        val note1 = Note(id = 1, title = "Apple Pie Recipe", content = "Ingredients for apple pie...", timestamp = 1L)
        val note2 = Note(id = 2, title = "Shopping List", content = "Apples, Milk, Bread", timestamp = 2L)
        val note3 = Note(id = 3, title = "Another Recipe", content = "Banana bread details...", timestamp = 3L)

        noteDao.insertNote(note1)
        noteDao.insertNote(note2)
        noteDao.insertNote(note3)

        val searchResults = noteDao.searchNotes("%Apple%").first()
        assertThat(searchResults.size).isEqualTo(1)
        assertThat(searchResults).contains(note1) // Only title matches "Apple Pie Recipe"
                                                // Note: The search in DAO is title OR content.
                                                // For this test, we are specifically checking title.
                                                // A more robust search test would check content too or use more distinct terms.
    }

     @Test
    fun searchNotes_findsMatchingNotesByContent() = runTest {
        val note1 = Note(id = 1, title = "Recipe", content = "How to make apple pie.", timestamp = 1L)
        val note2 = Note(id = 2, title = "List", content = "Buy apples and milk.", timestamp = 2L)
        val note3 = Note(id = 3, title = "Ideas", content = "Thoughts on bananas.", timestamp = 3L)

        noteDao.insertNote(note1)
        noteDao.insertNote(note2)
        noteDao.insertNote(note3)

        val searchResults = noteDao.searchNotes("%apple%").first() // Case-insensitive depends on DB collation with LIKE
        assertThat(searchResults.size).isEqualTo(2)
        assertThat(searchResults).containsExactly(note2, note1) // Ordered by timestamp desc
    }


}
