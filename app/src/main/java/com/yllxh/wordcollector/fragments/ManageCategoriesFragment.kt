package com.yllxh.wordcollector.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.dialogs.AddEditCategoryDialog
import com.yllxh.wordcollector.dialogs.DeleteCategoryDialog
import com.yllxh.wordcollector.viewmodels.ManageCategoriesViewModel
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.databinding.FragmentManageCategoriesBinding


class ManageCategoriesFragment : Fragment() {
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(ManageCategoriesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)

        val categoryAdapter = CategoryAdapter(requireContext(), true){ category ->
            AddEditCategoryDialog.newInstance(category)
                .show(requireFragmentManager(), AddEditCategoryDialog.TAG)
        }
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
                DeleteCategoryDialog.newInstance(category)
                    .show(requireFragmentManager(), DeleteCategoryDialog.TAG)
            }
        }).attachToRecyclerView(binding.categoryRecycleview)

        // Fab button used to pop up a dialog, for inserting a new category.
        binding.fab.setOnClickListener {
            AddEditCategoryDialog.newInstance()
                .show(requireFragmentManager(), AddEditCategoryDialog.TAG)
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
