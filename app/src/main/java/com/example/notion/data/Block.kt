package com.example.notion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Defines the Block entity, representing a section within a Note
@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = Note::class, // Link each block to a parent note
            parentColumns = ["id"], // Reference the note's primary key
            childColumns = ["noteId"], // Foreign key in this table
            onDelete = ForeignKey.CASCADE // Delete blocks if the parent note is deleted
        )
    ]
)
data class Block(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Unique ID for the block
    val noteId: Int, // Foreign key linking to the note
    val title: String = "", // Block title
    val content: String = "", // Block content
    val position: Int = 0 // Order of the block within the note
)
