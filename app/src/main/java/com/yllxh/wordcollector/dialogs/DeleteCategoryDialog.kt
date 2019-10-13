package com.yllxh.wordcollector.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.DialogDeletingCategoryBinding
import com.yllxh.wordcollector.viewmodels.DeleteCategoryViewModel


class DeleteCategoryDialog : DialogFragment(){

    private var clicks = 0
    private var wasCategoryDeleted = false
    lateinit var category: Category
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(DeleteCategoryViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState != null){
            clicks = savedInstanceState.getInt(CLICKS)
        }

        category = arguments?.getParcelable(KEY) ?: Category("")

        val currentCategory = AppPreferences.getLastSelectedCategory(requireContext())
        val isCurrentCategory = currentCategory == category.name


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
                wasCategoryDeleted = false
                dialog.cancel()
            }

            yesButton.setOnClickListener {
                when {
                    !isDefaultCategory -> {
                        viewModel.deleteAllOfCategory(category)
                        wasCategoryDeleted = true
                        dismissDialog(isCurrentCategory, dialog)
                    }
                    isDefaultCategory -> {
                        clicks++

                        if (clicks == 3) {
                            wasCategoryDeleted = true
                            viewModel.deleteAllWords()
                            dismissDialog(isCurrentCategory, dialog)
                        }
                        toast("$clicks")
                    }
                }
            }

            noButton.setOnClickListener {
                wasCategoryDeleted = if (!viewModel.deleteCategory(category)) {
                    toast(getString(R.string.look_again))
                    false
                } else {
                    true
                }
                dialog.cancel()
            }
        }
        return dialog
    }

    private fun dismissDialog(isCurrentCategory: Boolean, dialog: AlertDialog) {
        if (isCurrentCategory) {
            AppPreferences.setLastSelectedCategory(
                requireContext(),
                viewModel.defaultCategory
            )
        }
        dialog.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLICKS, clicks)
    }

    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }

    companion object {
        private const val CLICKS = "CLICKS"
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