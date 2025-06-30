package com.example.tnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tnotes.data.local.dao.NoteDao
import com.example.tnotes.data.local.model.Note

@Database(
    entities = [Note::class],
    version = 1, // Increment this on schema changes
    exportSchema = false // Set to true if you want to export schema to a folder
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        // We'll use Hilt to provide this instance, so a manual singleton
        // might not be strictly necessary here but is good practice for Room.
        // Hilt will manage the singleton scope.
        // const val DATABASE_NAME = "tnotes_db" // Can be defined in DI module
    }
}
