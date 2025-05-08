package com.example.notion

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.Block

// RecyclerView Adapter for displaying and editing blocks inside a note
class BlockAdapter(
    private var blocks: MutableList<Block>, // List of block items
    private val onBlockChanged: (Block) -> Unit, // Callback when a block is edited
    private val onStartDrag: (view: View, position: Int, block: Block) -> Unit // Callback for drag-and-drop handling
) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

    // ViewHolder class for each block item
    inner class BlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val etTitle: EditText = itemView.findViewById(R.id.etBlockTitle)
        private val etContent: EditText = itemView.findViewById(R.id.etBlockContent)
        private val ivDragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)
        private val ivCollapse: ImageView = itemView.findViewById(R.id.btnCollapseToggle)

        // Binds data from a Block to the UI and sets up listeners
        fun bind(block: Block, position: Int) {
            etTitle.setText(block.title)
            etContent.setText(block.content)

            // Listen for title changes and update the corresponding block
            etTitle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    blocks[position] = blocks[position].copy(title = s.toString())
                    onBlockChanged(blocks[position])
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Listen for content changes and update the corresponding block
            etContent.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    blocks[position] = blocks[position].copy(content = s.toString())
                    onBlockChanged(blocks[position])
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Toggle content visibility and change icon when collapse button is clicked
            ivCollapse.setOnClickListener {
                if (etContent.visibility == View.VISIBLE) {
                    etContent.visibility = View.GONE
                    ivCollapse.setImageResource(android.R.drawable.arrow_up_float)
                } else {
                    etContent.visibility = View.VISIBLE
                    ivCollapse.setImageResource(android.R.drawable.arrow_down_float)
                }
            }

            // Trigger drag action when the drag handle is touched
            ivDragHandle.setOnTouchListener { _, _ ->
                onStartDrag(itemView, position, blocks[position])
                true
            }
        }
    }

    // Inflate the layout for each block item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_block, parent, false)
        return BlockViewHolder(view)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        holder.bind(blocks[position], position)
    }

    // Return the number of blocks
    override fun getItemCount(): Int = blocks.size

    // Replace the entire list of blocks
    fun updateBlocks(newBlocks: List<Block>) {
        blocks = newBlocks.toMutableList()
        notifyDataSetChanged()
    }

    // Add a single block to the list
    fun addBlock(block: Block) {
        blocks.add(block)
        notifyItemInserted(blocks.size - 1)
    }

    // Return the current list of blocks
    fun getBlocks(): List<Block> = blocks
}
