package com.example.notion

import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.AppDatabase
import com.example.notion.data.Block
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        // Retrieve the note ID passed from the previous screen
        noteId = intent.getIntExtra("note_id", -1)

        // Initialize UI components
        etNoteTitle = findViewById(R.id.etNoteTitle)
        etNoteTags = findViewById(R.id.etNoteTags)
        tvTimestamps = findViewById(R.id.tvTimestamps)
        btnDeleteNote = findViewById(R.id.btnTrashBlock)
        btnAddBlock = findViewById(R.id.btnAddBlock)
        blockRecyclerView = findViewById(R.id.rvBlocks)

        // Initialize block adapter with change and drag callbacks
        blockAdapter = BlockAdapter(
            mutableListOf(),
            onBlockChanged = { updatedBlock ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@NoteEditorActivity)
                    db.blockDao().update(updatedBlock)
                }
            },
            onStartDrag = { view, _, _ ->
                val shadow = View.DragShadowBuilder(view)
                ViewCompat.startDragAndDrop(view, null, shadow, view, 0)
            }
        )

        // Set up drag-to-delete functionality on the trash button
        val trashButton = findViewById<FloatingActionButton>(R.id.btnTrashBlock)
        trashButton.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    trashButton.setColorFilter(android.graphics.Color.RED) // Highlight delete
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    trashButton.clearColorFilter() // Remove highlight
                }

                DragEvent.ACTION_DROP -> {
                    trashButton.clearColorFilter()
                    val draggedView = event.localState as? View ?: return@setOnDragListener true
                    val recycler = blockRecyclerView
                    val draggedIndex = recycler.getChildAdapterPosition(draggedView)

                    if (draggedIndex != RecyclerView.NO_POSITION) {
                        val blockToDelete = blockAdapter.getBlocks()[draggedIndex]

                        // Show confirmation dialog before deleting block
                        AlertDialog.Builder(this)
                            .setTitle("Delete Block")
                            .setMessage("Are you sure you want to delete this block?")
                            .setPositiveButton("Delete") { _, _ ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    AppDatabase.getInstance(this@NoteEditorActivity)
                                        .blockDao()
                                        .delete(blockToDelete)

                                    // Refresh block list after deletion
                                    val updatedBlocks = AppDatabase.getInstance(this@NoteEditorActivity)
                                        .blockDao()
                                        .getBlocksForNote(noteId)

                                    withContext(Dispatchers.Main) {
                                        blockAdapter.updateBlocks(updatedBlocks)
                                    }
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    trashButton.clearColorFilter()
                }
            }
            true
        }

        // Set up block RecyclerView
        blockRecyclerView.layoutManager = LinearLayoutManager(this)
        blockRecyclerView.adapter = blockAdapter

        // Load note and its blocks
        loadNote()
        loadBlocks()

        // Handle "Add Block" button click
        btnAddBlock.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(this@NoteEditorActivity)
                val newBlock = Block(noteId = noteId, title = "New Block", content = "", position = blockAdapter.itemCount)
                db.blockDao().insert(newBlock)

                // Refresh block list after insertion
                val updatedBlocks = db.blockDao().getBlocksForNote(noteId)
                withContext(Dispatchers.Main) {
                    blockAdapter.updateBlocks(updatedBlocks)
                }
            }
        }

        // Handle "Delete Note" button click
        btnDeleteNote.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(this@NoteEditorActivity)
                        val note = db.noteDao().getNoteById(noteId)

                        // If the note exists, delete it and its related blocks
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

    // Save the note automatically when the activity is paused
    override fun onPause() {
        super.onPause()
        saveNote()
    }

    // Save title, tags, and block positions to the database
    private fun saveNote() {
        val title = etNoteTitle.text.toString().trim()
        val tags = etNoteTags.text.toString().trim()

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@NoteEditorActivity)
            val note = db.noteDao().getNoteById(noteId)

            if (note != null) {
                // Update note metadata
                db.noteDao().updateNote(
                    note.copy(
                        title = title,
                        tags = tags,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                // Update block order based on current RecyclerView positions
                blockAdapter.getBlocks().forEachIndexed { index, block ->
                    db.blockDao().update(block.copy(position = index))
                }
            }
        }
    }

    // Load note details into the UI
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

    // Load blocks for the note into the adapter
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
