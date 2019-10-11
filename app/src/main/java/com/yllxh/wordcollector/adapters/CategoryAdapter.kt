package com.yllxh.wordcollector.adapters

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.CategoryListItemBinding

/**
 * CategoryAdapter class for presenting data in a RecycleView.
 *
 * @param widthMatchParent  Used to determine whether the width of views should
 *                          expand to match parent(RecycleView)
 *
 * @param forDialog         Used to inform the adapter whether it is used inside a dialog.
 */
class CategoryAdapter(
    private val context: Context,
    private val widthMatchParent: Boolean = false,
    private val forDialog: Boolean = false,
    private val onItemClickListener: (category: Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, widthMatchParent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(
            context,
            category,
            onItemClickListener,
            notifyNewSelection,
            widthMatchParent,
            forDialog
        )
    }

    /**
     * Overriding the submitList function, in order to inform the recycleView
     * about the last item which was selected, so that it is highlighted properly by the ViewHolder.
     */
    override fun submitList(list: MutableList<Category>?) {
        super.submitList(list)
        updateSelectedItemId()
    }

    /**
     * Notifies the adapter that a new item is selected, and it informs the adapter
     * about the oldSelection and the newSelection positions in the adapter.
     */
    private val notifyNewSelection = { oldSelection: Int, newSelection: Int ->
        notifyItemChanged(oldSelection)
        notifyItemChanged(newSelection)
    }

    /**
     * Gives access to the items of the adapter to classes outside the adapter.
     */
    fun getCategoryAtPosition(position: Int): Category {
        return getItem(position)
    }


    private fun updateSelectedItemId() {
        if (itemCount > 0 && lastSelectedItemId == -1) {
            val selectedCategory = AppPreferences.getLastSelectedCategory(context)
            for (i in 0 until itemCount) {
                if (getItem(i).name == selectedCategory) {
                    lastSelectedItemId = i
                    break
                }
            }
        }
    }

    companion object {
        /**
         * It is used to highlight the correct ViewHolder.
         */
        var lastSelectedItemId: Int = -1
            private set
    }

    class ViewHolder private constructor(private val binding: CategoryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            context: Context,
            category: Category,
            onItemClickListener: (category: Category) -> Unit,
            itemChangedListener: (Int, Int) -> Unit,
            widthMatchParent: Boolean,
            isDialog: Boolean
        ) {

            onViewHolderClickListener(category, onItemClickListener, context, itemChangedListener)

            highlightSelectedCategory(context)

            adaptViewHolderWidth(context, isDialog, widthMatchParent, category)

        }

        private fun onViewHolderClickListener(
            category: Category,
            onItemClickListener: (category: Category) -> Unit,
            context: Context,
            itemChangedListener: (Int, Int) -> Unit
        ) {
            binding.categoryTextView.text = category.name
            binding.root.setOnClickListener {
                onItemClickListener(category)
                AppPreferences.setLastSelectedCategory(context, category.name)
                itemChangedListener(lastSelectedItemId, adapterPosition)
                lastSelectedItemId = adapterPosition
            }
        }

        private fun adaptViewHolderWidth(
            context: Context,
            isDialog: Boolean,
            widthMatchParent: Boolean,
            category: Category
        ) {
            val isCountVisible = binding.categoryCountTextView.visibility == View.VISIBLE

            val isLandscape =
                context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (isLandscape && isDialog) {
                binding.root.layoutParams.width = WRAP_CONTENT
                binding.categoryCountTextView.visibility = View.GONE
            } else {
                if (widthMatchParent || isCountVisible) {
                    if (!isCountVisible) {
                        binding.categoryCountTextView.visibility = View.VISIBLE
                    }
                    binding.categoryCountTextView.text = category.wordCount.toString()
                }
            }
        }

        private fun highlightSelectedCategory(context: Context) {
            // Paints the current view with the correct colors
            when (lastSelectedItemId) {
                adapterPosition -> {

                    // Highlight the selected category.
                    binding.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.colorAccent
                        )
                    )
                    binding.categoryTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.categorySelectedTextColor
                        )
                    )
                }
                else -> {
                    binding.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.categoryBackground
                        )
                    )
                    binding.categoryTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.categoryTextColor
                        )
                    )
                }
            }
        }

        companion object {
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
}

private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {

    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}