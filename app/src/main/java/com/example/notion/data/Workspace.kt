package com.example.notion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspaces")
data class Workspace(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
