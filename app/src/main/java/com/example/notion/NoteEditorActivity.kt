package com.example.notion

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.example.notion.data.AppDatabase
import com.example.notion.data.Block
import com.example.notion.data.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NoteEditorActivity : AppCompatActivity() {
    private var noteId: Int = -1
    private lateinit var etNoteTitle: EditText
    private lateinit var etNoteTags: EditText
    private lateinit var tvTimestamps: TextView
    private lateinit var btnDeleteNote: FloatingActionButton
    private lateinit var btnAddBlock: FloatingActionButton
    private lateinit var collapsibleLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        noteId = intent.getIntExtra("note_id", -1)

        etNoteTitle = findViewById(R.id.etNoteTitle)
        etNoteTags = findViewById(R.id.etNoteTags)
        tvTimestamps = findViewById(R.id.tvTimestamps)
        btnDeleteNote = findViewById(R.id.btnDeleteNote)
        btnAddBlock = findViewById(R.id.btnAddBlock)
        collapsibleLayout = findViewById(R.id.collapsibleLayout)

        loadNote()
        loadBlocks()

        btnAddBlock.setOnClickListener {
            addBlockUI("", "")
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(this@NoteEditorActivity)
                val position = db.blockDao().getBlocksForNote(noteId).size
                db.blockDao().insert(Block(noteId = noteId, title = "New Block", content = "", position = position))
            }
        }

        btnDeleteNote.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(this@NoteEditorActivity)
                        val note = db.noteDao().getNoteById(noteId)
                        if (note != null) {
                            db.blockDao().deleteBlocksForNote(noteId)
                            db.noteDao().deleteNote(note)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@NoteEditorActivity, "Note deleted", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadNote() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val note = db.noteDao().getNoteById(noteId)
            if (note != null) {
                withContext(Dispatchers.Main) {
                    etNoteTitle.setText(note.title)
                    etNoteTags.setText(note.tags)
                    tvTimestamps.text = "Created: ${note.createdAt}\nUpdated: ${note.updatedAt}"
                }
            }
        }
    }

    private fun loadBlocks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val blocks = AppDatabase.getInstance(this@NoteEditorActivity).blockDao().getBlocksForNote(noteId)
            withContext(Dispatchers.Main) {
                blocks.forEach { block ->
                    addBlockUI(block.title, block.content)
                }
            }
        }
    }

    private fun addBlockUI(title: String, content: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val titleEdit = EditText(this).apply {
            hint = "Block Title"
            setText(title)
        }

        val contentEdit = EditText(this).apply {
            hint = "Block Content"
            setText(content)
        }

        container.addView(titleEdit)
        container.addView(contentEdit)
        collapsibleLayout.addView(container)
    }

    override fun onPause() {
        super.onPause()
        saveNoteAndBlocks()
    }

    private fun saveNoteAndBlocks() {
        val title = etNoteTitle.text.toString().trim()
        val tags = etNoteTags.text.toString().trim()

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val note = db.noteDao().getNoteById(noteId)
            if (note != null) {
                db.noteDao().updateNote(note.copy(
                    title = title,
                    tags = tags,
                    updatedAt = System.currentTimeMillis()
                ))
            }

            val blocks = db.blockDao().getBlocksForNote(noteId)
            val currentBlocks = mutableListOf<Block>()

            for (i in 0 until collapsibleLayout.childCount) {
                val container = collapsibleLayout.getChildAt(i) as ViewGroup
                val titleEdit = container.getChildAt(0) as EditText
                val contentEdit = container.getChildAt(1) as EditText

                val existing = blocks.getOrNull(i)
                if (existing != null) {
                    currentBlocks.add(existing.copy(
                        title = titleEdit.text.toString(),
                        content = contentEdit.text.toString(),
                        position = i
                    ))
                }
            }

            currentBlocks.forEach { db.blockDao().update(it) }
        }
    }
}
