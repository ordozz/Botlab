package com.example.tnotes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tnotes.ui.theme.TNotesTheme
import com.example.tnotes.ui.viewmodel.AddEditNoteUiState
import com.example.tnotes.ui.viewmodel.AddEditNoteViewModel
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel(),
    // noteId: Int?, // ViewModel now gets this from SavedStateHandle
    onNavigateBack: () -> Unit
    // onSaveNote: () -> Unit // ViewModel will handle navigation via state
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                // Persist permission to read this URI across device restarts
                // This is important if you're not copying the file immediately
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                viewModel.onImagePathChange(it.toString())
            }
        }
    )

    LaunchedEffect(key1 = uiState.isNoteSaved) {
        if (uiState.isNoteSaved) {
            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
            viewModel.acknowledgeNoteSaved() // Reset the flag
            onNavigateBack()
        }
    }

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.acknowledgeErrorMessage() // Reset after showing
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.currentNoteId == null) "Add Note" else "Edit Note") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 12.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.saveNote() }) {
                            Icon(Icons.Filled.Done, contentDescription = "Save Note")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (uiState.isLoading && viewModel.currentNoteId != null) { // Show loader only when loading existing note initially
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.errorMessage?.contains("Title", ignoreCase = true) == true,
                    colors = TextFieldDefaults.outlinedTextFieldColors()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.onContentChange(it) },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Takes available space
                    colors = TextFieldDefaults.outlinedTextFieldColors()
                    // Consider isError for content as well if needed
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Image Preview and Picker Button
                if (uiState.imagePath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(data = uiState.imagePath)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f) // Adjust aspect ratio as needed
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Button(
                    onClick = {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Pick Image Icon")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (uiState.imagePath == null) "Add Image" else "Change Image")
                }
            }
        }
    }
}

// Preview for Add Note
@Preview(showBackground = true, name = "Add Note Screen")
@Composable
fun AddNoteScreenPreview() {
    TNotesTheme {
        // For previews, we pass a simple SavedStateHandle.
        // The repository can be mocked if needed for more complex ViewModel logic during init.
        val previewSavedStateHandle = SavedStateHandle()
        val mockRepository = MockNoteRepository() // Assuming a simple mock

        val mockViewModel = AddEditNoteViewModel(mockRepository, previewSavedStateHandle)
        // Manually set the state for preview if needed, or rely on default init.
        // mockViewModel.uiState.value = AddEditNoteUiState(isLoading = false) // if needed

        AddEditNoteScreen(viewModel = mockViewModel, onNavigateBack = {})
    }
}

// Preview for Edit Note (simulating loaded state)
@Preview(showBackground = true, name = "Edit Note Screen")
@Composable
fun EditNoteScreenPreview() {
    TNotesTheme {
        val previewSavedStateHandle = SavedStateHandle().apply {
            set(NavRoutes.NOTE_ID_ARG, 1) // Simulate loading note with ID 1
        }
        val mockRepository = MockNoteRepository()
        val mockViewModel = AddEditNoteViewModel(mockRepository, previewSavedStateHandle)
        // To accurately preview loaded state, you might need to manually set the ViewModel's uiState
        // after it processes the savedStateHandle, or have the MockNoteRepository return a specific note.
        // For simplicity here, we'll just show the ViewModel initialized with a noteId.
        // A more elaborate mock would involve the ViewModel actually "loading" data.
        // For now, we'll set the state directly for the preview's purpose.
        mockViewModel._uiState.value = AddEditNoteUiState( // Accessing private member for preview setup
            title = "Sample Title (Loaded)",
            content = "This is some sample content for the loaded note.",
                isLoading = false
            ))
            override var currentNoteId: Int? = 1 // Simulate editing an existing note
        }
        AddEditNoteScreen(viewModel = mockViewModel, onNavigateBack = {})
    }
}

// Preview for Loading State
@Preview(showBackground = true, name = "Add/Edit Note - Loading")
@Composable
fun AddEditNoteScreenLoadingPreview() {
    TNotesTheme {
         val mockViewModel = object : AddEditNoteViewModel(hiltViewModel(), SavedStateHandle()) {
             override val uiState = MutableStateFlow(AddEditNoteUiState(isLoading = true))
             override var currentNoteId: Int? = 1
        }
        AddEditNoteScreen(viewModel = mockViewModel, onNavigateBack = {})
    }
}

// Preview for Error State
@Preview(showBackground = true, name = "Add/Edit Note - Error")
@Composable
fun AddEditNoteScreenErrorPreview() {
    TNotesTheme {
        val mockViewModel = object : AddEditNoteViewModel(hiltViewModel(), SavedStateHandle()) {
             override val uiState = MutableStateFlow(AddEditNoteUiState(errorMessage = "Title cannot be empty", title=""))
             override var currentNoteId: Int? = null
        }
        // For previewing snackbar, it's a bit tricky without real interactions.
        // This preview will show the fields, and you'd imagine the snackbar appearing.
        AddEditNoteScreen(viewModel = mockViewModel, onNavigateBack = {})
    }
}
