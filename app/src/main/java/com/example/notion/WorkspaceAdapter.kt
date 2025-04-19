package com.example.notion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.Workspace

class WorkspaceAdapter(
    private var items: List<Workspace>,
    private val onClick: (Workspace) -> Unit
) : RecyclerView.Adapter<WorkspaceAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.tvWorkspaceName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workspace, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workspace = items[position]
        holder.titleTextView.text = workspace.name
        holder.itemView.setOnClickListener { onClick(workspace) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Workspace>) {
        items = newItems
        notifyDataSetChanged()
    }
}