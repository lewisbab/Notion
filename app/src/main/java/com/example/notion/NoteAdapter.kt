package com.example.notion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.AppDatabase
import com.example.notion.data.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// RecyclerView Adapter for displaying a list of notes in a workspace
class NoteAdapter(
    private var notes: List<Note>,                      // List of notes to display
    private val onNoteUpdated: (List<Note>) -> Unit,    // Callback when notes are modified
    private val workspaceName: String                   // The name of the current workspace
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    // ViewHolder class holding references to the note item view elements
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentTextView: TextView = view.findViewById(R.id.tvNoteContent) // TextView for note title
    }

    // Inflate the layout for each note item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return ViewHolder(view)
    }

    // Bind note data to the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.contentTextView.text = note.title

        // When a note item is clicked, navigate to NoteEditorActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, NoteEditorActivity::class.java)
            intent.putExtra("note_id", note.id) // Pass the note ID to the editor activity
            context.startActivity(intent)
        }
    }

    // Return total number of notes in the list
    override fun getItemCount(): Int = notes.size

    // Update the internal list of notes and refresh the RecyclerView
    fun updateData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
