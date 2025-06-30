package com.example.tnotes.di

import android.app.Application
import androidx.room.Room
import com.example.tnotes.data.local.NoteDatabase
import com.example.tnotes.data.local.dao.NoteDao
import com.example.tnotes.data.repository.NoteRepositoryImpl
import com.example.tnotes.domain.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Lives as long as the application
object AppModule {

    @Provides
    @Singleton // Ensures only one instance of the database
    fun provideNoteDatabase(app: Application): NoteDatabase {
        return Room.databaseBuilder(
            app,
            NoteDatabase::class.java,
            "tnotes_db" // Database name
        )
        // .addMigrations(...) // Add migrations if needed later
        .fallbackToDestructiveMigration() // Not for production! For dev, if schema changes, it clears db.
        .build()
    }

    @Provides
    @Singleton // Dao is tied to the database instance
    fun provideNoteDao(database: NoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton // Repository is also a singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        // If you had a remote data source, you'd inject it here too
        // return NoteRepositoryImpl(noteDao, noteApiService)
        return NoteRepositoryImpl(noteDao)
    }

    // Add other application-wide dependencies here if needed
}
