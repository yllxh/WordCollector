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
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.DialogDeletingCategoryBinding
import com.yllxh.wordcollector.utils.getLastSelectedCategory
import com.yllxh.wordcollector.utils.setLastSelectedCategory
import com.yllxh.wordcollector.viewmodels.DeleteCategoryViewModel


class DeleteCategoryDialog : DialogFragment() {

    private var clicksCount = 0
    lateinit var passedCategory: Category
    private lateinit var binding: DialogDeletingCategoryBinding
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(DeleteCategoryViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState != null) {
            clicksCount = savedInstanceState.getInt(CLICKS_KEY)
        }

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_deleting_category,
            null,
            false
        )

        passedCategory = arguments?.getParcelable(KEY) ?: Category("")

        val currentCategory = getLastSelectedCategory(requireContext())
        val isCurrentCategory = currentCategory == passedCategory.name

        val isDefaultCategory = passedCategory.name == viewModel.defaultCategory
        if (isDefaultCategory) {
            binding.alertMessageTextView.text = getString(R.string.delete_all_items)
        }

        val dialog = createDialog(binding)

        setOnClickListeners(dialog, isDefaultCategory, isCurrentCategory)
        return dialog
    }

    private fun createDialog(binding: DialogDeletingCategoryBinding): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun setOnClickListeners(
        dialog: AlertDialog,
        isDefaultCategory: Boolean,
        isCurrentCategory: Boolean
    ) {
        binding.apply {
            cancelButton.setOnClickListener {
                dialog.cancel()
            }

            yesButton.setOnClickListener {
                when {
                    !isDefaultCategory -> {
                        viewModel.deleteAllOfCategory(passedCategory)
                        dismissDialog(isCurrentCategory, dialog)
                    }
                    isDefaultCategory -> {
                        clicksCount++

                        if (clicksCount == 3) {
                            viewModel.deleteAllWords()
                            dismissDialog(isCurrentCategory, dialog)
                        }
                        Toast.makeText(activity, "$clicksCount", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            noButton.setOnClickListener {
                val wasCategoryDeleted = viewModel.deleteCategory(passedCategory)
                if (!wasCategoryDeleted && isDefaultCategory) {
                    Toast.makeText(activity, getString(R.string.look_again), Toast.LENGTH_SHORT)
                        .show()
                }
                if (isCurrentCategory) {
                    dismissDialog(isCurrentCategory, dialog)
                }
                dialog.cancel()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Calling the onActivityResult on the target fragment,
        // to allow it to execute any necessary UI refreshing.
        targetFragment?.onActivityResult(DELETE_CATEGORY_REQUEST, RESULT_OK, Intent())
    }


    private fun dismissDialog(isCurrentCategory: Boolean, dialog: AlertDialog) {
        if (isCurrentCategory) {
            setLastSelectedCategory(
                requireContext(),
                viewModel.defaultCategory
            )
        }
        dialog.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLICKS_KEY, clicksCount)
    }

    companion object {
        private const val CLICKS_KEY = "CLICKS"
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

