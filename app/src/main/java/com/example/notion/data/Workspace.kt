package com.example.notion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Defines the Workspace entity, representing a collection of notes under a common name
@Entity(tableName = "workspaces")
data class Workspace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Unique ID for the workspace
    val name: String                                   // Name of the workspace
)
