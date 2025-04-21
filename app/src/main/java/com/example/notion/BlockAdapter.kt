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

class BlockAdapter(
    private var blocks: MutableList<Block>,
    private val onBlockChanged: (Block) -> Unit,
    private val onStartDrag: (view: View, position: Int, block: Block) -> Unit
) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

    inner class BlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val etTitle: EditText = itemView.findViewById(R.id.etBlockTitle)
        private val etContent: EditText = itemView.findViewById(R.id.etBlockContent)
        private val ivDragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)
        private val ivCollapse: ImageView = itemView.findViewById(R.id.btnCollapseToggle)

        fun bind(block: Block, position: Int) {
            etTitle.setText(block.title)
            etContent.setText(block.content)

            etTitle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    blocks[position] = blocks[position].copy(title = s.toString())
                    onBlockChanged(blocks[position])
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            etContent.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    blocks[position] = blocks[position].copy(content = s.toString())
                    onBlockChanged(blocks[position])
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            ivCollapse.setOnClickListener {
                if (etContent.visibility == View.VISIBLE) {
                    etContent.visibility = View.GONE
                    ivCollapse.setImageResource(android.R.drawable.arrow_up_float)
                } else {
                    etContent.visibility = View.VISIBLE
                    ivCollapse.setImageResource(android.R.drawable.arrow_down_float)
                }
            }

            ivDragHandle.setOnTouchListener { _, _ ->
                onStartDrag(itemView, position, blocks[position])
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_block, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        holder.bind(blocks[position], position)
    }

    override fun getItemCount(): Int = blocks.size

    fun updateBlocks(newBlocks: List<Block>) {
        blocks = newBlocks.toMutableList()
        notifyDataSetChanged()
    }

    fun addBlock(block: Block) {
        blocks.add(block)
        notifyItemInserted(blocks.size - 1)
    }

    fun getBlocks(): List<Block> = blocks
}
