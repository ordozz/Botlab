package com.example.tnotes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tnotes.data.local.model.Note
import com.example.tnotes.ui.theme.TNotesTheme
import com.example.tnotes.ui.viewmodel.NotesListUiState
import com.example.tnotes.ui.viewmodel.NotesListViewModel
import java.text.SimpleDateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel = hiltViewModel(),
    onNavigateToAddNote: () -> Unit,
    onNavigateToEditNote: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var noteToDelete: Note? by remember { mutableStateOf(null) }

    if (showDeleteConfirmationDialog && noteToDelete != null) {
        DeleteConfirmationDialog(
            noteTitle = noteToDelete!!.title,
            onConfirmDelete = {
                viewModel.deleteNote(noteToDelete!!)
                showDeleteConfirmationDialog = false
                noteToDelete = null
            },
            onDismiss = {
                showDeleteConfirmationDialog = false
                noteToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TNotes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddNote) {
                Icon(Icons.Filled.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Column( // Changed from Box to Column to stack SearchBar and List/States
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Search notes") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                colors = TextFieldDefaults.outlinedTextFieldColors()
            )

            Box(modifier = Modifier.weight(1f)) { // Box to allow aligning Empty/Error states in center
                when (val state = uiState) {
                    is NotesListUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is NotesListUiState.Success -> {
                        if (state.notes.isEmpty()) {
                            if (searchQuery.isBlank()) {
                                EmptyState()
                            } else {
                                NoSearchResultsState(query = searchQuery)
                            }
                        } else {
                            NotesLazyList(
                                notes = state.notes,
                                onNoteClick = { noteId -> onNavigateToEditNote(noteId) },
                                onDeleteRequest = { note ->
                                    noteToDelete = note
                                    showDeleteConfirmationDialog = true
                                }
                            )
                        }
                    }
                    is NotesListUiState.Error -> {
                        ErrorState(message = state.message, onRetry = { viewModel.onSearchQueryChange(searchQuery) }) // Re-trigger current search/fetch
                    }
                }
            }
        }
    }
}

@Composable
fun NotesLazyList(
    notes: List<Note>,
    onNoteClick: (Int) -> Unit,
    onDeleteRequest: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), // Keep top padding for search bar
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { note -> note.id }) { note ->
            NoteItem(
                note = note,
                onClick = { onNoteClick(note.id) },
                onDeleteRequest = { onDeleteRequest(note) }
            )
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit, // Changed parameter name
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column { // Changed from Row to Column to stack image above text
            note.imagePath?.let { imageUriString ->
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = imageUriString)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Note image", // Decorative, title provides context
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f), // Common aspect ratio for thumbnails
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDeleteRequest) { // Changed parameter name
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete Note",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No notes yet. Tap '+' to add one!", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = onRetry) { // Using IconButton for a simpler retry, could be a Button
                 Text("Retry")
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    noteTitle: String,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Note?") },
        text = { Text("Are you sure you want to delete the note \"$noteTitle\"? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


// Previews
@Preview(showBackground = true, name = "Notes List - Populated")
@Composable
fun NotesListScreenPopulatedPreview() {
    TNotesTheme {
        // Mock ViewModel or pass state directly for preview
        val notes = listOf(
            Note(1, "Grocery List", "Milk, Eggs, Bread, Cheese, Fruits, Vegetables, Chicken", System.currentTimeMillis()),
            Note(2, "Meeting Notes", "Discuss Q3 roadmap. Key action items: Alice to send proposal, Bob to schedule follow-up.", System.currentTimeMillis() - 100000),
            Note(3, "Book Ideas", "A sci-fi novel about AI discovering ancient alien tech.", System.currentTimeMillis() - 200000)
        )
        // This preview won't interact with a real ViewModel, so interactions are visual only.
        Scaffold(
            topBar = { TopAppBar(title = { Text("TNotes Preview") }) },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "") } }
        ) { padding ->
             Box(modifier=Modifier.padding(padding)) {
                NotesLazyList(notes = notes, onNoteClick = {}, onDeleteRequest = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "Notes List - Empty")
@Composable
fun NotesListScreenEmptyPreview() {
    TNotesTheme {
         Scaffold(
            topBar = { TopAppBar(title = { Text("TNotes Preview") }) },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "") } }
        ) { padding ->
            Box(modifier=Modifier.padding(padding)) {
                EmptyState()
            }
        }
    }
}

@Preview(showBackground = true, name = "Notes List - Error")
@Composable
fun NotesListScreenErrorPreview() {
    TNotesTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("TNotes Preview") }) },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "") } }
        ) { padding ->
            Box(modifier=Modifier.padding(padding)) {
                ErrorState(message = "Failed to load notes.", onRetry = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "Note Item")
@Composable
fun NoteItemPreview() {
    TNotesTheme {
        NoteItem(
            note = Note(1, "Sample Note Title", "This is the content of the sample note. It might be a bit long to see how it overflows.", System.currentTimeMillis()),
            onClick = {},
            onDeleteRequest = {}
        )
    }
}

@Preview(showBackground = true, name = "Delete Confirmation Dialog")
@Composable
fun DeleteConfirmationDialogPreview() {
    TNotesTheme {
        DeleteConfirmationDialog(
            noteTitle = "My Important Note",
            onConfirmDelete = {},
            onDismiss = {}
        )
    }
}
