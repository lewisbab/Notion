package com.example.notion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.AppDatabase
import com.example.notion.data.Note
import com.example.notion.NoteAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkspaceDetailsActivity : AppCompatActivity() {
    private var originalName: String? = null
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workspace_details)

        // UI references
        val titleTextView = findViewById<TextView>(R.id.tvWorkspaceTitle)
        val nameEditText = findViewById<EditText>(R.id.etEditName)
        val renameButton = findViewById<Button>(R.id.btnRename)
        val deleteButton = findViewById<Button>(R.id.btnDelete)
        val notesRecyclerView = findViewById<RecyclerView>(R.id.notesRecyclerView)
        val fabAddNote = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddNote)
        val tagFilterEdit = findViewById<EditText>(R.id.etTagFilter)

        // Get workspace name from intent
        originalName = intent.getStringExtra("workspace_name")
        titleTextView.text = originalName
        nameEditText.setText(originalName)

        // Initialize RecyclerView adapter for notes in this workspace
        noteAdapter = NoteAdapter(
            emptyList(),
            onNoteUpdated = { updatedNotes -> noteAdapter.updateData(updatedNotes) },
            workspaceName = originalName!!
        )
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.adapter = noteAdapter

        // Load notes for this workspace, filtered by tag if provided
        fun loadNotesFiltered(filter: String = "") {
            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(this@WorkspaceDetailsActivity)
                val allNotes = db.noteDao().getNotesForWorkspace(originalName!!)
                val filtered = if (filter.isBlank()) allNotes else allNotes.filter {
                    it.tags.contains(filter, ignoreCase = true)
                }
                withContext(Dispatchers.Main) {
                    noteAdapter.updateData(filtered)
                }
            }
        }

        // Initial load
        loadNotesFiltered()

        // Filter notes by tag as user types
        tagFilterEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadNotesFiltered(s.toString())
            }
        })

        // Handle adding a new note
        fabAddNote.setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter note content"

            AlertDialog.Builder(this)
                .setTitle("New Note")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val title = input.text.toString().trim()
                    if (title.isNotEmpty()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(this@WorkspaceDetailsActivity)
                            db.noteDao().insert(
                                Note(
                                    workspaceName = originalName!!,
                                    title = title
                                )
                            )
                            loadNotesFiltered(tagFilterEdit.text.toString())
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Handle renaming workspace
        renameButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            if (newName.isNotEmpty() && newName != originalName) {
                AlertDialog.Builder(this)
                    .setTitle("Confirm Rename")
                    .setMessage("Rename workspace to '$newName'?")
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(this@WorkspaceDetailsActivity)
                            val existing = db.workspaceDao().getByName(newName)
                            if (existing != null) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@WorkspaceDetailsActivity, "Name already in use", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                db.workspaceDao().renameWorkspace(originalName!!, newName)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@WorkspaceDetailsActivity, "Workspace renamed", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        // Handle deleting workspace
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete workspace '$originalName'?")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getInstance(this@WorkspaceDetailsActivity)
                        db.workspaceDao().deleteByName(originalName!!)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@WorkspaceDetailsActivity, "Workspace deleted", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Refresh notes when returning to this activity
    override fun onResume() {
        super.onResume()
        val tagFilterEdit = findViewById<EditText>(R.id.etTagFilter)
        val filter = tagFilterEdit.text.toString()
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@WorkspaceDetailsActivity)
            val allNotes = db.noteDao().getNotesForWorkspace(originalName!!)
            val filtered = if (filter.isBlank()) allNotes else allNotes.filter {
                it.tags.contains(filter, ignoreCase = true)
            }
            withContext(Dispatchers.Main) {
                noteAdapter.updateData(filtered)
            }
        }
    }
}
