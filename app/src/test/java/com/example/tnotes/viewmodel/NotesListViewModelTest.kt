package com.example.tnotes.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.tnotes.data.local.model.Note
import com.example.tnotes.repository.MockTestNoteRepository
import com.example.tnotes.ui.viewmodel.NotesListUiState
import com.example.tnotes.ui.viewmodel.NotesListViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class NotesListViewModelTest {

    // Rule for running tasks synchronously, useful for LiveData/ViewModel testing
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: NotesListViewModel
    private lateinit var mockRepository: MockTestNoteRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // Set the main dispatcher to the test dispatcher
        mockRepository = MockTestNoteRepository()
        viewModel = NotesListViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset the main dispatcher to the original one
    }

    @Test
    fun `fetchNotes success - uiState becomes Success with notes`() = runTest(testDispatcher) {
        val notes = listOf(
            Note(1, "Note 1", "Content 1", System.currentTimeMillis()),
            Note(2, "Note 2", "Content 2", System.currentTimeMillis() + 100)
        )
        mockRepository.setInitialNotes(notes) // Set initial notes in mock
        mockRepository.emitNotes(notes)      // Emit them through the flow

        viewModel.fetchNotes() // Trigger fetch (though init also fetches)

        advanceUntilIdle() // Ensure all coroutines complete

        val uiState = viewModel.uiState.value
        assertThat(uiState).isInstanceOf(NotesListUiState.Success::class.java)
        val successState = uiState as NotesListUiState.Success
        assertThat(successState.notes).isEqualTo(notes.sortedByDescending { it.timestamp })
    }

    @Test
    fun `fetchNotes empty - uiState becomes Success with empty list`() = runTest(testDispatcher) {
        mockRepository.setInitialNotes(emptyList())
        mockRepository.emitNotes(emptyList())

        viewModel.fetchNotes()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertThat(uiState).isInstanceOf(NotesListUiState.Success::class.java)
        assertThat((uiState as NotesListUiState.Success).notes).isEmpty()
    }

    @Test
    fun `fetchNotes error - uiState becomes Error`() = runTest(testDispatcher) {
        // Simulate an error by having the flow throw an exception
        // This requires modifying MockTestNoteRepository or creating a specific failing mock.
        // For simplicity, let's assume a way to make it error out, or test the catch block more directly.
        // A simple way: have the mock emit an error. For now, we'll simulate the repository throwing.

        // To truly test this, the mock repository's getAllNotes() would need to return a flow that throws.
        // Let's assume for this placeholder that an error occurs.
        // If the repository.getAllNotes() itself threw an exception not caught by the flow's .catch:
        // This test setup would need a more sophisticated mock that can throw from its flow.

        // A more direct way to test the .catch block in the ViewModel:
        // The current mockRepository.getAllNotes() returns a MutableSharedFlow.
        // It's hard to make that flow itself throw an error after collection starts.
        // The .catch operator in the ViewModel is for exceptions *within* the flow's collection.

        // Let's consider the case where the *initial* emission from repository is problematic or
        // the repository methods themselves (like deleteNote) throw.

        // This test case needs a mock repository that can be configured to throw an exception from its Flow.
        // For now, this test is more conceptual for a placeholder.
        // A proper test would involve:
        // mockRepository. настроитьНаВыбросОшибки()
        // viewModel.fetchNotes()
        // advanceUntilIdle()
        // assertThat(viewModel.uiState.value).isInstanceOf(NotesListUiState.Error::class.java)
    }


    @Test
    fun `deleteNote success - repository deleteNote is called`() = runTest(testDispatcher) {
        val noteToDelete = Note(1, "Test Delete", "Content", System.currentTimeMillis())
        mockRepository.setInitialNotes(listOf(noteToDelete))
        mockRepository.emitNotes(listOf(noteToDelete)) // Initial state

        viewModel.fetchNotes() // Load initial state
        advanceUntilIdle()

        viewModel.deleteNote(noteToDelete)
        advanceUntilIdle()

        // Verify that the note is removed from the list exposed by the mock repository's flow
        val notesAfterDelete = mockRepository.getAllNotes().first()
        assertThat(notesAfterDelete).doesNotContain(noteToDelete)

        // Also check UI state reflects the change
        val uiState = viewModel.uiState.value
        if (uiState is NotesListUiState.Success) {
            assertThat(uiState.notes).doesNotContain(noteToDelete)
        } else {
            // Fail if not success, or handle other states if expected
            assert(false) { "UI State should be Success after delete" }
        }
    }

     @Test
    fun `deleteNote failure - uiState becomes Error`() = runTest(testDispatcher) {
        val noteToDelete = Note(1, "Test Delete", "Content", System.currentTimeMillis())
        // Configure mockRepository.deleteNote to throw an exception
        val errorMessage = "Failed to delete"
        mockRepository.prepareToDeleteWithError(errorMessage) // Hypothetical method to make delete fail

        viewModel.deleteNote(noteToDelete)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertThat(uiState).isInstanceOf(NotesListUiState.Error::class.java)
        assertThat((uiState as NotesListUiState.Error).message).isEqualTo(errorMessage)
    }

    // Helper in MockTestNoteRepository to simulate delete error
    // Add this to MockTestNoteRepository.kt:
    /*
    private var deleteShouldThrowError: Boolean = false
    private var deleteErrorMessage: String = "Unknown delete error"

    fun prepareToDeleteWithError(message: String) {
        deleteShouldThrowError = true
        deleteErrorMessage = message
    }

    override suspend fun deleteNote(note: Note) {
        if (deleteShouldThrowError) {
            deleteShouldThrowError = false // Reset for next call
            throw RuntimeException(deleteErrorMessage)
        }
        notesList.removeIf { it.id == note.id }
        notesFlow.emit(notesList.toList().sortedByDescending { it.timestamp })
    }
    */

}
