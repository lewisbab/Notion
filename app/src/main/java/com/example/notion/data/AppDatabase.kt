package com.example.notion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Workspace::class, Note::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun noteDao(): NoteDao  // ✅ add this

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notion_database"
                )
                    .fallbackToDestructiveMigration() // ✅ required for version bump
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
