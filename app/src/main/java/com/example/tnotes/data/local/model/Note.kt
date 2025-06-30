package com.example.tnotes.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long, // Can be used for created/last modified date
    val imagePath: String? = null, // Path to an attached image, if any
    // val color: Int = 0 // Optional: for note color
)
