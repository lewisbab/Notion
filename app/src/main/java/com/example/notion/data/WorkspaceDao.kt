package com.example.notion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WorkspaceDao {
    @Insert
    suspend fun insert(workspace: Workspace)

    @Query("SELECT * FROM workspaces")
    suspend fun getAll(): List<Workspace>

    @Query("SELECT * FROM workspaces WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Workspace?

    @Query("UPDATE workspaces SET name = :newName WHERE name = :oldName")
    suspend fun renameWorkspace(oldName: String, newName: String)

    @Query("DELETE FROM workspaces WHERE name = :name")
    suspend fun deleteByName(name: String)
}
