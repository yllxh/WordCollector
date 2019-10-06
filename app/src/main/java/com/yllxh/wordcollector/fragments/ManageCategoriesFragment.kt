package com.yllxh.wordcollector.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.viewmodels.ManageCategoriesViewModel
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.DialogAddEditCategoryBinding
import com.yllxh.wordcollector.databinding.FragmentManageCategoriesBinding
import com.yllxh.wordcollector.databinding.DialogDeletingCategoryBinding


class ManageCategoriesFragment : Fragment() {
    private lateinit var binding: FragmentManageCategoriesBinding
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(ManageCategoriesViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)


        // Used to create a dialog in cases when a category needs to be is inserted/updated.
        // To insert a category the category parameter must be set to null, and to updateCategory a category
        // the category should be passed to it.
        val onAddOrEditCategory: (category: Category) -> Unit = { category ->
            val dialogBinding = DialogAddEditCategoryBinding.inflate(inflater, container, false)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .show()

            // Set onClickListeners to dialog buttons.
            dialogBinding.apply {
                val oldCategoryName = category.name
                newCategoryEt.setText(oldCategoryName)

                cancelButton.setOnClickListener {
                    dialog.cancel()
                }

                saveButton.setOnClickListener {
                    val newCategoryName = dialogBinding.newCategoryEt.text.toString()
                    val successful = if (category.name.isEmpty()) {
                        viewModel.insertCategory(Category(newCategoryName))
                    } else {
                        viewModel.updateCategory(
                            Category(newCategoryName),
                            Category(oldCategoryName)
                        )
                    }
                    // If the category is not updated/inserted display a toast to inform the user.
                    if (!successful) {
                        toast(getString(R.string.category_name_alert), Toast.LENGTH_LONG)
                    }
                    dialog.cancel()
                }
            }
        }

        val categoryAdapter = CategoryAdapter(
            requireContext(),
            true,
            forDialog = false,
            onItemClickListener = onAddOrEditCategory
        )
        binding.categoryRecycleview.adapter = categoryAdapter

        viewModel.categories.observe(this, Observer {
            categoryAdapter.submitList(it.toMutableList())

            // If the new category was inserted to the list, scroll to the Top of the recycleView.
            if (viewModel.newItemInserted) {
                binding.categoryRecycleview.smoothScrollToPosition(0)
                viewModel.newItemInserted = false
            }
        })

        // Enable the deletion of categories, by swiping the item left or right.
        ItemTouchHelper(object : ItemTouchHelper
        .SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val category = categoryAdapter.getCategoryAtPosition(position)
                onDeleteCategory(category, container)
            }
        }).attachToRecyclerView(binding.categoryRecycleview)

        // Fab button used to pop up a dialog, for inserting a new category.
        binding.fab.setOnClickListener {
            onAddOrEditCategory(Category(""))
        }
        return binding.root
    }

    /**
     * Function used as shortcut the Toast.makeText() function.
     */
    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }

    private fun onDeleteCategory(category: Category, container: ViewGroup?) {
        val dialogBinding = DialogDeletingCategoryBinding.inflate(layoutInflater, container, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .show()

        var clicks = 0
        val isDefaultCategory = category.name == viewModel.defaultCategory
        if (isDefaultCategory) {
            dialogBinding.alertMessageTextView.append(getString(R.string.press_yes_3_times))
        }

        // Set onClickListeners to dialog buttons.
        dialogBinding.apply {
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
    }
}
