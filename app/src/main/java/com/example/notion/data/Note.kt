package com.example.notion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceName: String,
    val title: String = "Untitled",
    val content: String,
    val body: String = ""
)