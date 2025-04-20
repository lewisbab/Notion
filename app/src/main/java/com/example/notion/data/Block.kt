package com.example.notion.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Block(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int,
    val title: String = "",
    val content: String = "",
    val position: Int = 0
)
