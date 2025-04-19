package com.example.notion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.AppDatabase
import com.example.notion.data.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteAdapter(
    private var notes: List<Note>,
    private val onNoteUpdated: (List<Note>) -> Unit,
    private val workspaceName: String
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentTextView: TextView = view.findViewById(R.id.tvNoteContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.contentTextView.text = note.content

        holder.itemView.setOnClickListener {
            val input = TextView(holder.itemView.context).apply {
                text = note.content
                setPadding(24, 24, 24, 24)
            }

            val edit = android.widget.EditText(holder.itemView.context).apply {
                setText(note.content)
            }

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Edit Note")
                .setView(edit)
                .setPositiveButton("Save") { _, _ ->
                    val newContent = edit.text.toString().trim()
                    if (newContent.isNotEmpty() && newContent != note.content) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val db = AppDatabase.getInstance(holder.itemView.context)
                            db.noteDao().updateNote(note.copy(content = newContent))
                            val updatedNotes = db.noteDao().getNotesForWorkspace(workspaceName)
                            withContext(Dispatchers.Main) {
                                onNoteUpdated(updatedNotes)
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}