package com.example.notion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.Workspace

// RecyclerView Adapter for displaying a list of workspaces
class WorkspaceAdapter(
    private var items: List<Workspace>,           // List of workspace items to display
    private val onClick: (Workspace) -> Unit       // Callback for when a workspace is clicked
) : RecyclerView.Adapter<WorkspaceAdapter.ViewHolder>() {

    // ViewHolder holds references to the views in each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvWorkspaceName) // TextView for workspace name
    }

    // Inflate the layout for each workspace item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workspace, parent, false)
        return ViewHolder(view)
    }

    // Bind workspace data to the view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workspace = items[position]
        holder.titleTextView.text = workspace.name

        // Trigger click callback when item is clicked
        holder.itemView.setOnClickListener { onClick(workspace) }
    }

    // Return the total number of items
    override fun getItemCount(): Int = items.size

    // Update the list of workspaces and refresh the UI
    fun updateData(newItems: List<Workspace>) {
        items = newItems
        notifyDataSetChanged()
    }
}
