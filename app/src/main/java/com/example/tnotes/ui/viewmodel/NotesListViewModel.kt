package com.example.tnotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tnotes.data.local.model.Note
import com.example.tnotes.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotesListUiState {
    object Loading : NotesListUiState()
    data class Success(val notes: List<Note>) : NotesListUiState()
    data class Error(val message: String) : NotesListUiState()
}

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotesListUiState>(NotesListUiState.Loading)
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    init {
        fetchNotes()
    }

    fun fetchNotes() {
        _uiState.value = NotesListUiState.Loading
        noteRepository.getAllNotes()
            .onEach { notes ->
                _uiState.value = NotesListUiState.Success(notes)
            }
            .catch { e ->
                _uiState.value = NotesListUiState.Error(e.localizedMessage ?: "An unknown error occurred")
            }
            .launchIn(viewModelScope)
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
                // The flow from getAllNotes should automatically update the UI.
                // If not, or for immediate feedback, one might need to refresh or manage list locally.
            } catch (e: Exception) {
                // Handle error, perhaps expose it to the UI
                _uiState.value = NotesListUiState.Error(e.localizedMessage ?: "Error deleting note")
                // Optionally re-fetch notes or manage state to show the error alongside existing notes
            }
        }
    }

    // Future: Implement search functionality
    // fun searchNotes(query: String) { ... }
}
