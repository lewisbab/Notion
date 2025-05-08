package com.example.notion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room database configuration with 3 entities and version 6
@Database(entities = [Workspace::class, Note::class, Block::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Abstract DAOs to be implemented by Room
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun noteDao(): NoteDao
    abstract fun blockDao(): BlockDao

    companion object {
        // Volatile instance to ensure visibility of changes across threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern to get the database instance
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Build the database using application context
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notion_database"
                )
                    // Destructive migration resets the DB if version changes without a migration strategy
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
