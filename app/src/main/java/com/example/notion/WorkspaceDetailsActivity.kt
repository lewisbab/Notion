package com.example.notion

import android.os.Bundle
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

        val titleTextView = findViewById<TextView>(R.id.tvWorkspaceTitle)
        val nameEditText = findViewById<EditText>(R.id.etEditName)
        val renameButton = findViewById<Button>(R.id.btnRename)
        val deleteButton = findViewById<Button>(R.id.btnDelete)
        val notesRecyclerView = findViewById<RecyclerView>(R.id.notesRecyclerView)
        val fabAddNote = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddNote)

        originalName = intent.getStringExtra("workspace_name")
        titleTextView.text = originalName
        nameEditText.setText(originalName)

        noteAdapter = NoteAdapter(
            emptyList(),
            onNoteUpdated = { updatedNotes -> noteAdapter.updateData(updatedNotes) },
            workspaceName = originalName!!
        )
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.adapter = noteAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@WorkspaceDetailsActivity)
            val notes = db.noteDao().getNotesForWorkspace(originalName!!)
            withContext(Dispatchers.Main) {
                noteAdapter.updateData(notes)
            }
        }

        fabAddNote.setOnClickListener {
            val input = EditText(this)
            input.hint = "Enter note content"

            AlertDialog.Builder(this)
                .setTitle("New Note")
                .setView(input)
                .setPositiveButton("Add") { _, _ ->
                    val content = input.text.toString().trim()
                    if (content.isNotEmpty()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val db = AppDatabase.getInstance(this@WorkspaceDetailsActivity)
                            db.noteDao().insert(Note(workspaceName = originalName!!, content = content))
                            val notes = db.noteDao().getNotesForWorkspace(originalName!!)
                            withContext(Dispatchers.Main) {
                                noteAdapter.updateData(notes)
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

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
}
