package com.example.notion.data

import androidx.room.*

// DAO interface for accessing and managing Note entities in the Room database
@Dao
interface NoteDao {

    // Insert a new note into the database and return its generated ID
    @Insert
    suspend fun insert(note: Note): Long

    // Retrieve all notes that belong to a specific workspace
    @Query("SELECT * FROM notes WHERE workspaceName = :workspace")
    suspend fun getNotesForWorkspace(workspace: String): List<Note>

    // Retrieve a single note by its unique ID
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    // Update an existing note's data
    @Update
    suspend fun updateNote(note: Note)

    // Delete a specific note from the database
    @Delete
    suspend fun deleteNote(note: Note)
}
