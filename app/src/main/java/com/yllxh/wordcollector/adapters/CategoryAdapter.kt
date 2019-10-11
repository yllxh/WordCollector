package com.yllxh.wordcollector.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
 * @param widthMatchParent  used to determine whether the width of views should
 *                          expand to match parent(RecycleView)
 */
class CategoryAdapter(
    private val context: Context,
    private val widthMatchParent: Boolean = false,
    private val onItemClickListener: (category: Category) -> Unit
) : ListAdapter<Category, CategoryViewHolder>(CategoryDiffCallback()),
    CategoryViewHolder.SelectionListener{
    override var lastSelectedItemId: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent, widthMatchParent)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(
            this,
            category,
            widthMatchParent
        )
    }


    override fun onNewItemSelected(newItemId: Int, category: Category) {
        notifyItemChanged(newItemId)
        if (lastSelectedItemId >= 0) {
            notifyItemChanged(lastSelectedItemId)
        }
        lastSelectedItemId = newItemId

        onItemClickListener(category)

        AppPreferences.setLastSelectedCategory(context, category.name)
    }

    override fun getContext(): Context {
        return context
    }


    /**
     * Overriding the submitList function, in order to inform the recycleView
     * about the last item which was selected, so that it is highlighted properly by the CategoryViewHolder.
     */
    override fun submitList(list: MutableList<Category>?) {
        super.submitList(list)
        updateSelectedItemId()
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
}
class CategoryViewHolder private constructor(private val binding: CategoryListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    interface SelectionListener {
        var lastSelectedItemId: Int

        fun onNewItemSelected(
            newItemId: Int,
            category: Category
        )
        fun getContext(): Context
    }

    fun bind(
        listener: SelectionListener,
        category: Category,
        widthMatchParent: Boolean
    ) {
        binding.apply {
            categoryTextView.text = category.name
            root.setOnClickListener {
                listener.onNewItemSelected(adapterPosition, category)
            }

            // Paints the current view with the correct colors
            when (listener.lastSelectedItemId) {
                adapterPosition -> {
                    // Highlight the selected category.
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(listener.getContext(), R.color.colorAccent)
                    )
                    categoryTextView.setTextColor(
                        ContextCompat.getColor(listener.getContext(), R.color.categorySelectedTextColor)
                    )
                }
                else -> {
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(listener.getContext(), R.color.categoryBackground)
                    )
                    categoryTextView.setTextColor(
                        ContextCompat.getColor(listener.getContext(), R.color.categoryTextColor)
                    )
                }
            }

            val isCountVisible = categoryCountTextView.visibility == View.VISIBLE

            if (widthMatchParent || isCountVisible) {
                if (!isCountVisible) {
                    categoryCountTextView.visibility = View.VISIBLE
                }
                categoryCountTextView.text = category.wordCount.toString()
            }
        }
    }

    companion object {
        fun from(parent: ViewGroup, widthMatchParent: Boolean): CategoryViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CategoryListItemBinding.inflate(layoutInflater, parent, false)
            if (widthMatchParent) {
                binding.categoryTextView.minimumWidth = parent.width
            }
            return CategoryViewHolder(binding)
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