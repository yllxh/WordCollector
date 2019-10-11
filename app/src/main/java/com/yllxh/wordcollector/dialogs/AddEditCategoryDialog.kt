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
import com.yllxh.wordcollector.viewmodels.ManageCategoriesViewModel

private const val KEY = "AddEditCategoryDialog"

class AddEditCategoryDialog : DialogFragment(){
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(ManageCategoriesViewModel::class.java)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val category: Category = arguments?.getParcelable(KEY) ?: Category("")
        val binding: DialogAddEditCategoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_add_edit_category,
            null,
            false
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        val oldCategoryName = category.name
        // Set onClickListeners to dialog buttons.


        binding.newCategoryEt.setText(oldCategoryName)

        binding.cancelButton.setOnClickListener {
            dialog.cancel()
        }

        binding.saveButton.setOnClickListener {
            val newCategoryName = binding.newCategoryEt.text.toString()

            val successful =
            if (category.name.isEmpty()) {
                viewModel.insertCategory(Category(newCategoryName))
            } else {
                viewModel.updateCategory(
                    Category(newCategoryName),
                    Category(oldCategoryName)
                )
            }
            // If the category is not updated/inserted display a toast to inform the user.
            if (!successful) {
                toast(getString(R.string.category_name_alert))
            } else {
                toast(getString(R.string.category_saved))
            }
            dialog.cancel()
        }


        return dialog
    }

    private fun toast(msg: String){
        Toast.makeText(
            requireContext(),
            msg,
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        const val TAG: String = "AddEditCategoryDialog"

        fun newInstance(category: Category? = null): AddEditCategoryDialog {
            val bundle = Bundle()
            bundle.putParcelable(KEY, category ?: Category(""))

            val newDialog = AddEditCategoryDialog()
            newDialog.arguments = bundle

            return newDialog

        }
    }

}