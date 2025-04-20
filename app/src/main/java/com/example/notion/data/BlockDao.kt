package com.example.notion.data

import androidx.room.*

@Dao
interface BlockDao {
    @Insert
    suspend fun insert(block: Block): Long

    @Update
    suspend fun update(block: Block)

    @Delete
    suspend fun delete(block: Block)

    @Query("SELECT * FROM blocks WHERE noteId = :noteId ORDER BY position ASC")
    suspend fun getBlocksForNote(noteId: Int): List<Block>

    @Query("DELETE FROM blocks WHERE noteId = :noteId")
    suspend fun deleteBlocksForNote(noteId: Int)
}
