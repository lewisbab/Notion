package com.example.notion

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.notion.data.Block

class BlockAdapter(
    private var blocks: MutableList<Block>,
    private val onBlockChanged: (Block) -> Unit,
) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

    inner class BlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val etTitle: EditText = itemView.findViewById(R.id.etBlockTitle)
        val etContent: EditText = itemView.findViewById(R.id.etBlockContent)

        fun bind(block: Block) {
            etTitle.setText(block.title)
            etContent.setText(block.content)

            etTitle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        blocks[pos] = blocks[pos].copy(title = s.toString())
                        onBlockChanged(blocks[pos])
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            etContent.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        blocks[pos] = blocks[pos].copy(content = s.toString())
                        onBlockChanged(blocks[pos])
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_block, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        holder.bind(blocks[position])
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
