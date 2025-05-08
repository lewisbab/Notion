package com.example.notion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// DAO interface for managing Workspace entities in the Room database
@Dao
interface WorkspaceDao {

    // Insert a new workspace into the database
    @Insert
    suspend fun insert(workspace: Workspace)

    // Retrieve all workspaces stored in the database
    @Query("SELECT * FROM workspaces")
    suspend fun getAll(): List<Workspace>

    // Retrieve a single workspace by its name
    @Query("SELECT * FROM workspaces WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Workspace?

    // Update the name of a workspace
    @Query("UPDATE workspaces SET name = :newName WHERE name = :oldName")
    suspend fun renameWorkspace(oldName: String, newName: String)

    // Delete a workspace by its name
    @Query("DELETE FROM workspaces WHERE name = :name")
    suspend fun deleteByName(name: String)
}
