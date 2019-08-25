package com.yllxh.wordcollector.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.CategoryListItemBinding

class CategoryAdapter(
    private val context: Context,
    private val widthMatchParent: Boolean,
    private val onItemClickListener: (category: Category?) -> Boolean
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(CategoryDiffCallback()) {
    /**
     * Notifies the adapter that a new item is selected, and it informs the adapter
     * about the oldSelection and the newSelection positions in the adapter.
     */
    private val onNewCategorySelected = { oldSelection: Int, newSelection: Int ->
        notifyItemChanged(oldSelection)
        notifyItemChanged(newSelection)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, widthMatchParent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(context, category, onItemClickListener, onNewCategorySelected)
    }


    class ViewHolder private constructor(val binding: CategoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            context: Context,
            category: Category,
            onItemClickListener: (category: Category?) -> Boolean,
            itemChangedListener: (Int, Int) -> Unit
            ) {
            binding.apply {
                categoryTextView.text = category.name
                cardView.setOnClickListener {
                    // If the onItemClickListener returns true, then highlight the selected category.
                    if(onItemClickListener(category)){
                        itemChangedListener(lastSelectedItemId, adapterPosition)
                        lastSelectedItemId = adapterPosition
                    }
                }

                // Paints the current view with the correct colors
                when (lastSelectedItemId) {
                    adapterPosition -> {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
                        categoryTextView.setTextColor(ContextCompat.getColor(context,R.color.categorySelectedTextColor))
                    }else -> {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.categoryBackground))
                        categoryTextView.setTextColor(ContextCompat.getColor(context, R.color.categoryTextColor))
                    }
                }
            }
        }

        companion object {
            /**
             * Variable used to keep track of the last selected item in of the parent adapter.
             * It is used to highlight the correct ViewHolder.
             */
            private var lastSelectedItemId: Int = 0

            fun from(parent: ViewGroup, widthMatchParent: Boolean): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CategoryListItemBinding.inflate(layoutInflater, parent, false)
                if (widthMatchParent) {
                    binding.categoryTextView.minimumWidth = parent.width
                }
                return ViewHolder(binding)
            }
        }
    }

    /**
     * Gives access to the items of the adapter to classes outside the adapter.
     */
    fun getCategoryAtPosition(position: Int): Category {
        return getItem(position)
    }
}


private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}