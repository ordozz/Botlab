package com.example.tnotes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tnotes.ui.theme.TNotesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    // viewModel: AddEditNoteViewModel = hiltViewModel(), // Will be added later
    noteId: Int?, // Null for new note, non-null for existing note
    onNavigateBack: () -> Unit,
    onSaveNote: () -> Unit // Could pass note details or rely on ViewModel
) {
    // Dummy state for now
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // In a real scenario, you would load the note if noteId is not null
    // LaunchedEffect(key1 = noteId) {
    //    if (noteId != null) {
    //        viewModel.loadNote(noteId)
    //        // Observe viewModel state for title and content
    //    }
    // }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "Add Note" else "Edit Note") },
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
                    IconButton(onClick = {
                        // viewModel.saveNote(title, content) // Or however you handle save
                        onSaveNote()
                    }) {
                        Icon(Icons.Filled.Done, contentDescription = "Save Note")
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes available space
                maxLines = 10 // Adjust as needed
            )
            // Add other fields like image picker, color picker later
            Spacer(modifier = Modifier.height(16.dp))
            // Button(onClick = onSaveNote, modifier = Modifier.fillMaxWidth()) {
            //    Text("Save Note")
            // }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNoteScreenPreview() {
    TNotesTheme {
        AddEditNoteScreen(noteId = null, onNavigateBack = {}, onSaveNote = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EditNoteScreenPreview() {
    TNotesTheme {
        AddEditNoteScreen(noteId = 1, onNavigateBack = {}, onSaveNote = {})
    }
}
