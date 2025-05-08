package com.example.notion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Defines the Note entity, representing an individual note within a workspace
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,    // Unique ID for the note
    val workspaceName: String,                           // Name of the workspace this note belongs to
    val title: String = "Untitled",                      // Note title (defaults to "Untitled")
    val tags: String = "",                               // Optional tags as a comma-separated string
    val createdAt: Long = System.currentTimeMillis(),    // Timestamp of note creation
    val updatedAt: Long = System.currentTimeMillis()     // Timestamp of last update
)
