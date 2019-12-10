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
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.CategoryListItemBinding
import com.yllxh.wordcollector.utils.getLastSelectedCategory


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
        val selectedCategory = getLastSelectedCategory(context)
        updateSelectedItemPosition(list, selectedCategory)
        super.submitList(list)
    }


    /**
     * Overloaded submitList method that updates the selected category
     * and directly updates the list using the super class method.
     *
     *
     */
    fun submitList(list: List<Category>?, selectedCategory: String) {
        updateSelectedItemPosition(list, selectedCategory)
        super.submitList(list)
    }
    private fun updateSelectedItemPosition(list: List<Category>?, selectedCategory: String) {
        list?.let {
            for (i in list.indices) {
                if (list[i].name == selectedCategory) {
                    selectedItemPosition = i
                    break
                }
            }
        }
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

            if (isOrientationLandscape(parent)
                && inDialog) {
                setViewHolderWidth(binding, WRAP_CONTENT)
                hideWordCountNumber(binding)
            } else if (widthMatchParent) {
                setViewHolderWidth(binding, MATCH_PARENT)
            }
            return CategoryViewHolder(binding)
        }

        private fun hideWordCountNumber(binding: CategoryListItemBinding) {
            binding.categoryCountTextView.visibility = View.GONE
        }

        private fun setViewHolderWidth(
            binding: CategoryListItemBinding,
            width: Int
        ) {
            binding.root.layoutParams.width = width
        }

        private fun isOrientationLandscape(parent: ViewGroup) =
            parent.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
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