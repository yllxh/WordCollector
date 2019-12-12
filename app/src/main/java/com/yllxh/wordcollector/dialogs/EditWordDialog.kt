package com.yllxh.wordcollector.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.databinding.DialogEditWordBinding
import com.yllxh.wordcollector.viewmodels.EditWordViewModel

class EditWordDialog : DialogFragment(){
    private lateinit var binding: DialogEditWordBinding
    private lateinit var oldWord: Word
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(EditWordViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        oldWord = arguments!!.getParcelable(KEY) ?: Word()

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_edit_word,
            null,
            false
        )
        binding.data = oldWord

        val categoryAdapter = CategoryAdapter(requireContext(), inDialog = true) {
            viewModel.setSelectedCategory(it.name)
        }
        binding.dialogCategoryRecycleview.adapter = categoryAdapter

        viewModel.categories.observe(this, Observer {
            categoryAdapter.submitList(it, oldWord.category)
            viewModel.setSelectedCategory(oldWord.category)
        })
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        setOnClickListeners(dialog)

        return dialog
    }

    private fun setOnClickListeners(dialog: AlertDialog) {
        binding.saveButton.setOnClickListener {
            // If the word is not updated display a toast to inform the user
            val newWord = extractNewWord()
            val wasWordValid = viewModel.update(newWord, oldWord)

            if (!wasWordValid) {
                toast(getString(R.string.word_was_not_edited))
            }
            dialog.cancel()
        }
        binding.cancelButton.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun extractNewWord(): Word {
        val selectedCategory = viewModel.selectedCategory.value ?: oldWord.category
        val definitionText = binding.editedDefinition.text.toString()
        val wordText = binding.editedWord.text.toString()
        return Word(wordText, definitionText, selectedCategory)
    }

    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }

    companion object{
        private const val KEY = "EditWordDialog"
        const val TAG = KEY

        fun newInstance(word: Word): EditWordDialog {
            val bundle = Bundle()
            bundle.putParcelable(KEY, word)
            val editWordDialog = EditWordDialog()
            editWordDialog.arguments = bundle

            return  editWordDialog
        }
    }
}