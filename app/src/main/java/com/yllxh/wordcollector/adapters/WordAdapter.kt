package com.yllxh.wordcollector.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.databinding.WordListItemBinding
import kotlinx.coroutines.Deferred


class WordAdapter(private var onEditClickListener: (Word) -> Unit)
    : ListAdapter<Word, WordAdapter.ViewHolder>(WordDiffCallback()){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onEditClickListener)

    }

    class ViewHolder private constructor(private var binding: WordListItemBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(
            word: Word,
            onEditClickListener: (Word) -> Unit
        ) {
            binding.apply {
                this.word = word
                editButton.setOnClickListener { onEditClickListener(word) }
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = WordListItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    fun getWordAtPosition(position: Int): Word{
        return getItem(position)
    }

}



private class WordDiffCallback : DiffUtil.ItemCallback<Word>() {

    override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
        return oldItem == newItem
    }
}