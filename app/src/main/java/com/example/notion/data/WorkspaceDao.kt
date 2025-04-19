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
}
