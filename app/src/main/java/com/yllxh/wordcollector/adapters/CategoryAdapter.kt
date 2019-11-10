package com.yllxh.wordcollector.adapters

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
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
 *
 *  @param context              Context needed for the AppPreferences and the ViewHolder
 *  @param widthMatchParent     Used to determine whether the width of views should
 *                              expand to match parent(RecycleView).
 *
 *  @param inDialog             Used to indicate whether the adapter is used in a Dialog.
 *  @param onItemClickListener  Determines what should happen when an item is clicked.
 */
class CategoryAdapter(
    private val context: Context,
    private val widthMatchParent: Boolean = false,
    private val inDialog: Boolean = false,
    private val onItemClickListener: (category: Category) -> Unit
) : ListAdapter<Category, CategoryViewHolder>(CategoryDiffCallback()),
    CategoryViewHolder.SelectionListener{
    override var selectedItemPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent, widthMatchParent, inDialog)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(this, category, widthMatchParent)
    }


    override fun onNewItemSelected(newItemId: Int, category: Category) {
        val oldItemId = selectedItemPosition
        selectedItemPosition = newItemId
        notifyItemChanged(newItemId)
        notifyItemChanged(oldItemId)
        onItemClickListener(category)
    }

    override fun getContext(): Context {
        return context
    }

    /**
     * Overriding the submitList function, in order to inform the recycleView
     * about the last item which was selected, so that it is highlighted properly by the CategoryViewHolder.
     */
    override fun submitList(list: List<Category>?) {
        val selectedCategory = AppPreferences.getLastSelectedCategory(context)
        updateSelectedItemPosition(list, selectedCategory)
        super.submitList(list)
    }

    private fun updateSelectedItemPosition(
        list: List<Category>?,
        selectedCategory: String
    ) {
        list?.let {
            for (i in list.indices) {
                if (list[i].name == selectedCategory) {
                    selectedItemPosition = i
                    break
                }
                if (i == list.size) {
                    selectedItemPosition = 0
                }
            }
        }
    }

    fun submitList(list: MutableList<Category>?, selectedCategory: String) {
        updateSelectedItemPosition(list, selectedCategory)
        super.submitList(list)
    }

    /**
     * Gives access to the items of the adapter to classes outside the adapter.
     */
    fun getCategoryAtPosition(position: Int): Category {
        return getItem(position)
    }
}
class CategoryViewHolder private constructor(private val binding: CategoryListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    interface SelectionListener {
        var selectedItemPosition: Int

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
            when (listener.selectedItemPosition) {
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
        fun from(parent: ViewGroup, widthMatchParent: Boolean, inDialog: Boolean = false): CategoryViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CategoryListItemBinding.inflate(layoutInflater, parent, false)

            val orientation = parent.context.resources.configuration.orientation

            val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
            if (isLandscape && inDialog) {
                binding.root.layoutParams.width = WRAP_CONTENT
                binding.categoryCountTextView.visibility = View.GONE
            } else if (widthMatchParent) {
                binding.root.layoutParams.width = MATCH_PARENT
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