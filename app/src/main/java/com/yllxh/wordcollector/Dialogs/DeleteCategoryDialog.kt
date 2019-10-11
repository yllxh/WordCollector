package com.yllxh.wordcollector.Dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.DialogDeletingCategoryBinding
import com.yllxh.wordcollector.viewmodels.ManageCategoriesViewModel


private const val CLICKS = "CLICKS"
class DeleteCategoryDialog : DialogFragment(){
    private var clicks = 0

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(ManageCategoriesViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState != null){
            clicks = savedInstanceState.getInt(CLICKS)
        }
        val category: Category = arguments?.getParcelable(KEY) ?: Category("")

        val binding: DialogDeletingCategoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_deleting_category,
            null,
            false
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        val isDefaultCategory = category.name == viewModel.defaultCategory
        if (isDefaultCategory) {
            binding.alertMessageTextView.append(getString(R.string.press_yes_3_times))
        }

        // Set onClickListeners to dialog buttons.
        binding.apply {
            cancelButton.setOnClickListener {
                dialog.cancel()
            }

            yesButton.setOnClickListener {
                when {
                    !isDefaultCategory -> {
                        viewModel.deleteAllOfCategory(category)
                        viewModel.deleteCategory(category)
                        dialog.cancel()
                    }
                    isDefaultCategory -> {
                        clicks++
                        if (clicks == 3) {
                            viewModel.deleteAllWords()
                            dialog.cancel()
                        }
                        toast("$clicks")
                    }
                }
            }

            noButton.setOnClickListener {
                if (!viewModel.deleteCategory(category)) {
                    toast(getString(R.string.look_again))
                }
                dialog.cancel()
            }
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLICKS, clicks)
    }

    private fun toast(msg: String){
        Toast.makeText(
            requireContext(),
            msg,
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val KEY = "DeleteCategoryDialog"
        const val TAG: String = KEY

        fun newInstance(category: Category): DeleteCategoryDialog {
            val bundle = Bundle()
            bundle.putParcelable(KEY, category)

            val newDialog = DeleteCategoryDialog()
            newDialog.arguments = bundle

            return newDialog

        }
    }

}