package com.yllxh.wordcollector.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.CategoryListItemBinding

class CategoryAdapter(
    private val widthMatchParent: Boolean,
    private val onAddOrEditCategory: ((category: Category?) -> Unit)? = null
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, widthMatchParent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onAddOrEditCategory)

    }

    class ViewHolder private constructor(val binding: CategoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            category: Category,
            onAddOrEditCategory: ((category: Category?) -> Unit)? = null
        ) {
            binding.categoryTextView.text = category.name
            binding.cardView.setOnClickListener {
                onAddOrEditCategory?.let { it(category) }
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