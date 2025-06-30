package com.example.tnotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tnotes.ui.screens.AddEditNoteScreen
import com.example.tnotes.ui.screens.NotesListScreen
import com.example.tnotes.ui.theme.TNotesTheme
import dagger.hilt.android.AndroidEntryPoint

object NavRoutes {
    const val NOTE_LIST = "noteList"
    const val ADD_EDIT_NOTE = "addEditNote"
    const val NOTE_ID_ARG = "noteId" // Argument name for note ID
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TNotesTheme {
                TNotesAppNavigation()
            }
        }
    }
}

@Composable
fun TNotesAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.NOTE_LIST
    ) {
        composable(NavRoutes.NOTE_LIST) {
            NotesListScreen(
                onNavigateToAddNote = {
                    navController.navigate("${NavRoutes.ADD_EDIT_NOTE}/-1") // -1 or other indicator for new note
                },
                onNavigateToEditNote = { noteId ->
                    navController.navigate("${NavRoutes.ADD_EDIT_NOTE}/$noteId")
                }
            )
        }
        composable(
            route = "${NavRoutes.ADD_EDIT_NOTE}/{${NavRoutes.NOTE_ID_ARG}}",
            arguments = listOf(navArgument(NavRoutes.NOTE_ID_ARG) {
                type = NavType.IntType
            })
        ) {
            // noteId is now handled by AddEditNoteViewModel via SavedStateHandle
            AddEditNoteScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Keeping a simple preview for the main activity setup,
// detailed screen previews are in their respective files.
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TNotesTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Text("TNotes App with Navigation", modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
