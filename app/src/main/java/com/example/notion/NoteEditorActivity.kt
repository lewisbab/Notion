package com.example.notion

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notion.data.AppDatabase
import com.example.notion.data.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteEditorActivity : AppCompatActivity() {
    private var noteId: Int = -1
    private lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        val noteTitle = findViewById<EditText>(R.id.etNoteTitle)
        val noteBody = findViewById<EditText>(R.id.etNoteBody)
        noteId = intent.getIntExtra("note_id", -1)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val loadedNote = db.noteDao().getNoteById(noteId)
            if (loadedNote != null) {
                note = loadedNote
                withContext(Dispatchers.Main) {
                    noteTitle.setText(note.title)
                    noteBody.setText(note.body)
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
                if (::note.isInitialized && (updatedTitle != note.title || updatedBody != note.body)) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getInstance(this@NoteEditorActivity)
                            .noteDao().updateNote(note.copy(title = updatedTitle, body = updatedBody))
                    }
                }
            }
        })
    }
}