package com.yllxh.wordcollector.adapters

import android.content.Context
import android.view.ViewGroup
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.CategoryListItemBinding

class DialogCategoryAdapter(
    context: Context,
    private val widthMatchParent: Boolean = false,
    private val onItemClickListener: (category: Category) -> Unit
)  : CategoryAdapter(context, widthMatchParent, onItemClickListener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder.from(parent, true, true)
    }
}