package com.example.notion.data

import androidx.room.*

// DAO interface for managing Block entities in the Room database
@Dao
interface BlockDao {

    // Insert a new block into the database and return its generated ID
    @Insert
    suspend fun insert(block: Block): Long

    // Update an existing block's data
    @Update
    suspend fun update(block: Block)

    // Delete a specific block from the database
    @Delete
    suspend fun delete(block: Block)

    // Retrieve all blocks associated with a specific note, ordered by position
    @Query("SELECT * FROM blocks WHERE noteId = :noteId ORDER BY position ASC")
    suspend fun getBlocksForNote(noteId: Int): List<Block>

    // Delete all blocks linked to a given note ID
    @Query("DELETE FROM blocks WHERE noteId = :noteId")
    suspend fun deleteBlocksForNote(noteId: Int)
}
