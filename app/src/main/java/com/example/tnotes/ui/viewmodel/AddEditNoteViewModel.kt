package com.example.tnotes.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tnotes.data.local.model.Note
import com.example.tnotes.domain.repository.NoteRepository
import com.example.tnotes.ui.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditNoteUiState(
    val title: String = "",
    val content: String = "",
    val imagePath: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNoteSaved: Boolean = false // To signal navigation
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle // Hilt automatically provides this
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    private var currentNoteId: Int? = null

    init {
        savedStateHandle.get<Int>(NavRoutes.NOTE_ID_ARG)?.let { noteId ->
            if (noteId != -1) { // -1 indicates a new note
                currentNoteId = noteId
                loadNote(noteId)
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle, errorMessage = null)
    }

    fun onContentChange(newContent: String) {
        _uiState.value = _uiState.value.copy(content = newContent, errorMessage = null)
    }

    fun onImagePathChange(newPath: String?) {
        _uiState.value = _uiState.value.copy(imagePath = newPath)
    }

    private fun loadNote(noteId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val note = noteRepository.getNoteById(noteId).first() // Assuming we want the first emission
            if (note != null) {
                _uiState.value = _uiState.value.copy(
                    title = note.title,
                    content = note.content,
                    imagePath = note.imagePath,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Note not found."
                )
            }
        }
    }

    fun saveNote() {
        val title = _uiState.value.title
        val content = _uiState.value.content

        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title cannot be empty.")
            return
        }
        // Content can be blank if allowed

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val noteToSave = Note(
                    id = currentNoteId ?: 0, // If currentNoteId is null, it's a new note (id 0 for autoGenerate)
                    title = title,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    imagePath = _uiState.value.imagePath
                )
                if (currentNoteId == null) {
                    noteRepository.insertNote(noteToSave)
                } else {
                    noteRepository.updateNote(noteToSave)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, isNoteSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error saving note."
                )
            }
        }
    }

    fun acknowledgeNoteSaved() {
        _uiState.value = _uiState.value.copy(isNoteSaved = false)
    }

    fun acknowledgeErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
