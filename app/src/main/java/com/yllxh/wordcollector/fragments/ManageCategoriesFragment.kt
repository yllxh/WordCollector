package com.yllxh.wordcollector.fragments


import android.content.Context
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
import com.google.android.material.snackbar.Snackbar
import com.yllxh.wordcollector.viewmodels.ManageCategoriesViewModel
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.databinding.DialogAddEditCategoryBinding
import com.yllxh.wordcollector.databinding.FragmentManageCategoriesBinding

class ManageCategoriesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProviders.of(this).get(ManageCategoriesViewModel::class.java)


        // Function used to create a dialog in cases when a category needs to be is inserted/updated.
        // To insert a category the category parameter must be set to null, and to update a category
        // the category should be passed to it.
        val onAddOrEditCategory: (category: Category?) -> Boolean = { category ->
            DialogAddEditCategoryBinding.inflate(inflater, container, false).apply {
                val oldCategoryName = category?.name ?: ""
                newCategoryEt.setText(oldCategoryName)

                AlertDialog.Builder(activity as Context).apply {
                    setView(root)
                    setPositiveButton(R.string.done) { _, _ ->
                        val newCategoryName = newCategoryEt.text.toString()
                        val successful = if (category == null) {
                            viewModel.insertCategoryIfValid(Category(newCategoryName))
                        }else{
                            viewModel.updateCategoryIfValid(Category(newCategoryName), Category(oldCategoryName))
                        }
                        // If the category is not updated/inserted display a toast to inform the user
                        if (!successful){
                            toast(getString(R.string.category_name_alert), Toast.LENGTH_LONG)
                        }
                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    create().show()
                }
            }
            // Returning false to tell the adapter not to highlight the selected color.
            false
        }

        // Create an instance of the CategoryAdapter
        val categoryAdapter = CategoryAdapter(activity as Context,true, onAddOrEditCategory)
        binding.categoryRecycleview.adapter = categoryAdapter

        viewModel.categories.observe(this, Observer {
            categoryAdapter.submitList(it)

            // If the new category was inserted to the list, scroll to the Top of the recycleView.
            if (viewModel.newItemInserted) {
                binding.categoryRecycleview.smoothScrollToPosition(0)
                viewModel.newItemInserted = false
            }
        })

        // Enable the deletion of categories, by swiping the item left or right.
        ItemTouchHelper(object : ItemTouchHelper
        .SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val category = categoryAdapter.getCategoryAtPosition(position)
                if (viewModel.deleteCategory(category)) {
                    Snackbar.make(binding.root,
                        getString(R.string.deleting) + category.name,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            viewModel.insertCategoryIfValid(category)
                        }.show()
                } else {
                    toast(getString(R.string.look_again))
                }
            }
        }).attachToRecyclerView(binding.categoryRecycleview)

        // Fab button used to pop up a dialog, for inserting a new category.
        binding.fab.setOnClickListener {
            onAddOrEditCategory(null)
        }
        return binding.root
    }

    /**
     * Function used as shortcut the Toast.makeText() function.
     */
    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }
}
