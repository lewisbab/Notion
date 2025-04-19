package com.example.notion

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notion.data.AppDatabase
import com.example.notion.data.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class NoteEditorActivity : AppCompatActivity() {
    private var noteId: Int = -1
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        val noteTitle = findViewById<EditText>(R.id.etNoteTitle)
        val noteBody = findViewById<EditText>(R.id.etNoteBody)
        val noteTags = findViewById<EditText>(R.id.etNoteTags)
        val timestampText = findViewById<TextView>(R.id.tvTimestamps)
        noteId = intent.getIntExtra("note_id", -1)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val loadedNote = db.noteDao().getNoteById(noteId)
            if (loadedNote != null) {
                note = loadedNote
                withContext(Dispatchers.Main) {
                    noteTitle.setText(note.title)
                    noteBody.setText(note.body)
                    noteTags.setText(note.tags)
                    val fmt = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    val created = fmt.format(Date(note.createdAt))
                    val updated = fmt.format(Date(note.updatedAt))
                    timestampText.text = "Created: $created\nLast edited: $updated"
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NoteEditorActivity, "Note not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onPause(owner: androidx.lifecycle.LifecycleOwner) {
                super.onPause(owner)
                val updatedTitle = noteTitle.text.toString()
                val updatedBody = noteBody.text.toString()
                val updatedTags = noteTags.text.toString()
                if (::note.isInitialized && (
                            updatedTitle != note.title ||
                                    updatedBody != note.body ||
                                    updatedTags != note.tags)) {
                    val updatedNote = note.copy(
                        title = updatedTitle,
                        body = updatedBody,
                        tags = updatedTags,
                        updatedAt = System.currentTimeMillis()
                    )
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getInstance(this@NoteEditorActivity).noteDao().updateNote(updatedNote)
                    }
                }
            }
        })
    }
}