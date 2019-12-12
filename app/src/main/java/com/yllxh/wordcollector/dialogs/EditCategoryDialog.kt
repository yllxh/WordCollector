package com.yllxh.wordcollector.dialogs

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
import com.yllxh.wordcollector.databinding.DialogAddEditCategoryBinding
import com.yllxh.wordcollector.utils.setLastSelectedCategory
import com.yllxh.wordcollector.viewmodels.EditCategoryViewModel


class EditCategoryDialog : DialogFragment(){
    private lateinit var binding: DialogAddEditCategoryBinding
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(EditCategoryViewModel::class.java)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (savedInstanceState == null) {
            viewModel.passedCategory = arguments?.getParcelable(KEY) ?: Category("")
        }

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_add_edit_category,
            null,
            false
        )

        binding.newCategoryEt.setText(
            viewModel.passedCategory.name)
        val dialog = createDialog()
        setOnClickListeners(dialog)

        return dialog
    }

    private fun setOnClickListeners(dialog: AlertDialog) {
        binding.cancelButton.setOnClickListener {
            dialog.cancel()
        }

        binding.saveButton.setOnClickListener {
            val newCategoryName = binding.newCategoryEt.text.toString()

            val successful =
                if (viewModel.passedCategory.name.isEmpty()) {
                    viewModel.insertCategory(Category(newCategoryName))
                } else {
                    viewModel.updateCategory(Category(newCategoryName), viewModel.passedCategory)
                }

            if (!successful) {
                toast(getString(R.string.category_name_alert))
            } else {
                setLastSelectedCategory(requireContext(), newCategoryName)
                toast(getString(R.string.category_saved))
            }
            dialog.cancel()
        }
    }

    private fun createDialog(): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }

    companion object {
        private const val KEY = "AddEditCategoryDialog"

        const val TAG: String = KEY

        fun newInstance(category: Category? = null): EditCategoryDialog {
            val bundle = Bundle()
            bundle.putParcelable(KEY, category ?: Category(""))

            val newDialog = EditCategoryDialog()
            newDialog.arguments = bundle

            return newDialog

        }
    }

}