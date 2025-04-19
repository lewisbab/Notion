package com.example.notion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes WHERE workspaceName = :workspace")
    suspend fun getNotesForWorkspace(workspace: String): List<Note>

    @Query("UPDATE notes SET content = :content WHERE id = :id")
    suspend fun updateNoteContent(id: Int, content: String)

    @Update
    suspend fun updateNote(note: Note)
}