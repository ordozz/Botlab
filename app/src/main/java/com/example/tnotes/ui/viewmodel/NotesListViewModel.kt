package com.example.tnotes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tnotes.data.local.model.Note
import com.example.tnotes.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.flowOf // Added for onStart emitting Loading if needed, though map is better

// Debounce time for search query input
private const val SEARCH_DEBOUNCE_MS = 300L

sealed class NotesListUiState {
    object Loading : NotesListUiState()
    data class Success(val notes: List<Note>) : NotesListUiState()
    data class Error(val message: String) : NotesListUiState()
}

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // This StateFlow will hold the UI state, reacting to search queries or direct fetches.
    // It's driven by the combination of searchQuery and repository calls.
    val uiState: StateFlow<NotesListUiState> = searchQuery
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            val notesDataFlow = if (query.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                noteRepository.searchNotes(query)
            }
            notesDataFlow
                .map<List<Note>, NotesListUiState> { notes -> NotesListUiState.Success(notes) }
                .onStart { emit(NotesListUiState.Loading) } // Emit Loading before data flow starts
                .catch { e -> emit(NotesListUiState.Error(e.localizedMessage ?: "Error fetching notes")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = NotesListUiState.Loading // Initial state
        )

    // The _uiState backing property is no longer strictly necessary for uiState's construction
    // but might be useful if other functions need to directly push an error state, e.g. deleteNote error.
    // For now, let's assume deleteNote errors are not pushed to the main uiState this way,
    // but could be exposed via a separate SharedFlow for events if needed.
    // private val _uiState = MutableStateFlow<NotesListUiState>(NotesListUiState.Loading)


    init {
        // Initial fetch is handled by the uiState StateFlow definition.
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
                // The list will automatically update due to the reactive nature of Room flows
                // being observed by uiState.
            } catch (e: Exception) {
                // Option 1: Log the error, user might see stale data momentarily if delete fails silently.
                // Option 2: Expose this error via a separate SharedFlow<String> for event-like errors.
                // Option 3: If absolutely necessary to push to main uiState, it gets more complex here
                // as uiState is now a derived flow. For simplicity, we'll log or use a SharedFlow.
                // For now, let's just log it or consider a SharedFlow for "transient" errors.
                // e.g., viewModelScope.launch { _errorEvents.emit("Failed to delete note") }
                System.err.println("Failed to delete note: ${e.localizedMessage}")
            }
        }
    }

    // Example for a SharedFlow for transient error messages (like delete failing)
    // private val _errorEvents = MutableSharedFlow<String>()
    // val errorEvents = _errorEvents.asSharedFlow()
}
