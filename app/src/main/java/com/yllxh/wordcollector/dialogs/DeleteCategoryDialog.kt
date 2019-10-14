package com.yllxh.wordcollector.dialogs

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
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
            binding.alertMessageTextView.text = getString(R.string.delete_all_items)
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
                        Toast.makeText(activity, "$clicks", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            noButton.setOnClickListener {
                wasCategoryDeleted = if (!viewModel.deleteCategory(category)) {
                    Toast.makeText(activity, getString(R.string.look_again), Toast.LENGTH_SHORT).show()
                    false
                } else {
                    true
                }
                dialog.cancel()
            }
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Calling the onActivityResult on the target fragment,
        // to allow it to execute any necessary UI refreshing.
        targetFragment?.onActivityResult(DELETE_CATEGORY_REQUEST, RESULT_OK, Intent())
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

    companion object {
        private const val CLICKS = "CLICKS"
        private const val KEY = "DeleteCategoryDialog"
        const val TAG: String = KEY
        const val DELETE_CATEGORY_REQUEST = 101

        fun newInstance(fragment: Fragment, category: Category): DeleteCategoryDialog {

            val bundle = Bundle()
            bundle.putParcelable(KEY, category)

            val newDialog = DeleteCategoryDialog()
            newDialog.setTargetFragment(fragment, DELETE_CATEGORY_REQUEST)
            newDialog.arguments = bundle
            return newDialog

        }
    }

}

