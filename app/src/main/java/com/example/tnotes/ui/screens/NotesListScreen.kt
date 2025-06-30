package com.example.tnotes.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tnotes.ui.theme.TNotesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    // viewModel: NotesListViewModel = hiltViewModel(), // Will be added later
    onNavigateToAddNote: () -> Unit,
    onNavigateToEditNote: (Int) -> Unit // Pass noteId for editing
) {
    // val notesState = viewModel.notes.collectAsStateWithLifecycle()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Placeholder for notes list
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notes yet. Tap '+' to add one!", style = MaterialTheme.typography.bodyLarge)
            }
            // Later, this will be a LazyColumn displaying notesState.value
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesListScreenPreview() {
    TNotesTheme {
        NotesListScreen(
            onNavigateToAddNote = {},
            onNavigateToEditNote = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NotesListScreenDarkPreview() {
    TNotesTheme {
        NotesListScreen(
            onNavigateToAddNote = {},
            onNavigateToEditNote = {}
        )
    }
}
