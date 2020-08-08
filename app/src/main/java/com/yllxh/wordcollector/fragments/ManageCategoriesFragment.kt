package com.yllxh.wordcollector.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.dialogs.EditCategoryDialog
import com.yllxh.wordcollector.dialogs.DeleteCategoryDialog
import com.yllxh.wordcollector.viewmodels.ManageCategoriesViewModel
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.databinding.FragmentManageCategoriesBinding

class ManageCategoriesFragment : Fragment(){

    lateinit var categoryAdapter: CategoryAdapter
    private val viewModel by lazy {
        ViewModelProvider(this).get(ManageCategoriesViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)

        categoryAdapter = CategoryAdapter(requireContext(), true){ category ->
            viewModel.setCurrentCategory(category.name)
            categoryAdapter.notifySelectedCategoryChanged(category.name)
            if (category.name != viewModel.defaultCategory) {
                EditCategoryDialog.newInstance(category)
                    .show(parentFragmentManager, EditCategoryDialog.TAG)
            }
        }
        binding.categoryRecycleview.adapter = categoryAdapter
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(binding.categoryRecycleview)

        viewModel.categories.observe(viewLifecycleOwner, Observer {
            categoryAdapter.submitList(it)
            categoryAdapter.notifyDataSetChanged()
        })

        // Fab button used to pop up a dialog, for inserting a new category.
        binding.fab.setOnClickListener {
            EditCategoryDialog.newInstance()
                .show(parentFragmentManager, EditCategoryDialog.TAG)

        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == DeleteCategoryDialog.DELETE_CATEGORY_REQUEST){
            categoryAdapter.notifyDataSetChanged()
        }
    }

    // Enable the deletion of categories, by swiping the item left or right.
    private val itemTouchHelper = object : ItemTouchHelper
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
            val category = viewModel.categories.value!![position]
            DeleteCategoryDialog.newInstance(this@ManageCategoriesFragment, category)
                .show(parentFragmentManager, DeleteCategoryDialog.TAG)
        }
    }
}
