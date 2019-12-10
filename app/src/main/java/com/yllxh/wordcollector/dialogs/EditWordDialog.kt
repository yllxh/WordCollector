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
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(EditWordViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogEditWordBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_edit_word,
            null,
            false
        )

        val word: Word = arguments!!.getParcelable(KEY) ?: Word()

        binding.data = word
        val adapter = CategoryAdapter(requireContext(), false, inDialog = true) {
            viewModel.setCurrentCategory(it.name)
        }
        binding.dialogCategoryRecycleview.adapter = adapter
        viewModel.categories.observe(this, Observer {
            adapter.submitList(it, word.category)
            viewModel.setCurrentCategory(word.category)
        })
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
        binding.saveButton.setOnClickListener {
            // If the word is not updated display a toast to inform the user
            val wasWordValid = viewModel.update(
                Word(
                    binding.editedWord.text.toString(),
                    binding.editedDefinition.text.toString(),
                    viewModel.currentCategory.value ?: word.category
                ),
                word
            )

            if (!wasWordValid) {
                toast(getString(R.string.word_is_not_valid))
            }
            dialog.cancel()
        }
        binding.cancelButton.setOnClickListener {
            dialog.cancel()
        }

        return dialog
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