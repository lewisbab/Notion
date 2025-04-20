package com.example.notion

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.AppDatabase
import com.example.notion.data.Block
import com.example.notion.data.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class NoteEditorActivity : AppCompatActivity() {
    private var noteId: Int = -1
    private lateinit var etNoteTitle: EditText
    private lateinit var etNoteTags: EditText
    private lateinit var tvTimestamps: TextView
    private lateinit var btnDeleteNote: FloatingActionButton
    private lateinit var btnAddBlock: FloatingActionButton
    private lateinit var blockRecyclerView: RecyclerView
    private lateinit var blockAdapter: BlockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        noteId = intent.getIntExtra("note_id", -1)

        etNoteTitle = findViewById(R.id.etNoteTitle)
        etNoteTags = findViewById(R.id.etNoteTags)
        tvTimestamps = findViewById(R.id.tvTimestamps)
        btnDeleteNote = findViewById(R.id.btnDeleteNote)
        btnAddBlock = findViewById(R.id.btnAddBlock)
        blockRecyclerView = findViewById(R.id.rvBlocks)

        blockAdapter = BlockAdapter(mutableListOf()) { updatedBlock ->
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(this@NoteEditorActivity)
                db.blockDao().update(updatedBlock)
            }
        }

        blockRecyclerView.layoutManager = LinearLayoutManager(this)
        blockRecyclerView.adapter = blockAdapter

        loadNote()
        loadBlocks()

        btnAddBlock.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(this@NoteEditorActivity)
                val newBlock = Block(noteId = noteId, title = "New Block", content = "", position = blockAdapter.itemCount)
                db.blockDao().insert(newBlock)
                val updatedBlocks = db.blockDao().getBlocksForNote(noteId)
                withContext(Dispatchers.Main) {
                    blockAdapter.updateBlocks(updatedBlocks)
                }
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
                            db.noteDao().deleteNote(note)
                            db.blockDao().deleteBlocksForNote(noteId)
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

    override fun onPause() {
        super.onPause()
        saveNote()
    }

    private fun saveNote() {
        val title = etNoteTitle.text.toString().trim()
        val tags = etNoteTags.text.toString().trim()

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val note = db.noteDao().getNoteById(noteId)
            if (note != null) {
                db.noteDao().updateNote(
                    note.copy(
                        title = title,
                        tags = tags,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                blockAdapter.getBlocks().forEachIndexed { index, block ->
                    db.blockDao().update(block.copy(position = index))
                }
            }
        }
    }

    private fun loadNote() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val note = db.noteDao().getNoteById(noteId)
            if (note != null) {
                val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                withContext(Dispatchers.Main) {
                    etNoteTitle.setText(note.title)
                    etNoteTags.setText(note.tags)
                    tvTimestamps.text = "Created: ${formatter.format(Date(note.createdAt))}\nUpdated: ${formatter.format(Date(note.updatedAt))}"
                }
            }
        }
    }

    private fun loadBlocks() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val blocks = db.blockDao().getBlocksForNote(noteId)
            withContext(Dispatchers.Main) {
                blockAdapter.updateBlocks(blocks)
            }
        }
    }
}
