package com.yllxh.wordcollector.fragments

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.viewmodels.WordDisplayViewModel
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.adapters.WordAdapter
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.databinding.FragmentWordDisplayBinding
import com.yllxh.wordcollector.dialogs.EditWordDialog
import com.yllxh.wordcollector.utils.getLastSelectedCategory
import com.yllxh.wordcollector.utils.getNightMode
import com.yllxh.wordcollector.utils.setDayNightMode


class WordDisplayFragment : Fragment() {

    private val delayTime by lazy {
        resources.getInteger(R.integer.short_delay).toLong()
    }
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(WordDisplayViewModel::class.java)
    }
    private lateinit var binding: FragmentWordDisplayBinding
    private lateinit var wordAdapter: WordAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentWordDisplayBinding.inflate(inflater, container, false)

        initAdapters()

        startObservingData()

        setOnClickListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initializeSelectedCategory()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)

        val searchView = menu.findItem(R.id.menu_item_search)?.actionView as SearchView
        setSearchBehavior(searchView)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.night_mode_menu_item) {
            toggleNightMode()
            return true
        }
        return NavigationUI.onNavDestinationSelected(item, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

    /**
     * Helper function together the initialization of OnClickListeners of Views.
     */
    private fun setOnClickListeners() {
        binding.apply {
            saveTextview.setOnClickListener {
                val word = Word(
                    newWordEditText.text.toString(),
                    newDefinitionEditText.text.toString(),
                    viewModel.currentCategory.value ?: viewModel.defaultCategory
                )

                newWordEditText.setText("")
                newDefinitionEditText.setText("")
                // If the word is not inserted, than it means that it is not valid
                if (!viewModel.insertWord(word)) {
                    toast(getString(R.string.word_is_not_valid))
                }
            }
        }
        binding.apply {
            lookUpTextview.setOnClickListener {
                val str = newWordEditText.text.toString().trim()
                if (str.isEmpty()) {
                    toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)
                } else {
                    lookUpTheNewWord(str)
                }
            }
        }

        binding.apply {

            hideHeaderButton.setOnClickListener {
                enterNewWordCardview.visibility = View.GONE
                floatingActionButton.show()
            }
        }
        binding.apply {
            floatingActionButton.setOnClickListener {
                floatingActionButton.hide()
                enterNewWordCardview.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Initializes the recycleViews with adapters.
     * Must be called before startObservingData()
     */
    private fun initAdapters() {
        categoryAdapter = CategoryAdapter(requireContext()) {
            viewModel.setCurrentCategory(it.name)
        }
        binding.categoryRecycleview.adapter = categoryAdapter

        wordAdapter = WordAdapter { word ->
            EditWordDialog.newInstance(word)
                .show(requireFragmentManager(), EditWordDialog.TAG)
        }
        binding.wordRecycleview.adapter = wordAdapter

        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(binding.wordRecycleview)
    }

    /**
     * Helper function used to group together subscriptions to observe data changes.
     */
    private fun startObservingData() {
        viewModel.categories.observe(this@WordDisplayFragment, Observer { list ->
            categoryAdapter.submitList(list)

            viewModel.apply {
                if (list.none { it.name == currentCategory.value }) {
                    setCurrentCategory(defaultCategory)
                }
            }
        })

        viewModel.currentCategory.observe(this, Observer {
            wordAdapter.submitList(
                viewModel.filterWordsToCategory(it)
            )
        })

        viewModel.words.observe(this, Observer {
            wordAdapter.submitList(
                viewModel.filterWordsToCategory()
            )

            // If the new word was inserted to the list, scroll to the Top of the recycleView
            if (viewModel.newItemInserted || viewModel.isUserSearching) {
                binding.wordRecycleview.smoothScrollToPosition(0)

                if (viewModel.isUserSearching) {
                    viewModel.newItemInserted = false
                }
            }
        })
    }

    /**
     * Sets the correct visibility to views on the screen.
     */
    private fun onSearchStateChange(isSearching: Boolean) {
        when {
            isSearching -> {
                setVisibilityHeaderAndCategoryAdapter(View.GONE)
            }
            else -> {
                setVisibilityHeaderAndCategoryAdapter(View.VISIBLE)
                viewModel.apply {
                    setCurrentCategory(currentCategory.value)
                }
            }
        }
        binding.floatingActionButton.hide()
        viewModel.isUserSearching = isSearching

    }

    private fun setVisibilityHeaderAndCategoryAdapter(visibility: Int) {
        binding.enterNewWordCardview.visibility = visibility
        binding.categoryRecycleview.visibility = visibility
    }

    private fun setSearchBehavior(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                wordAdapter.submitList(
                    viewModel.filterWordsToMatchQuery(newText)
                )
                binding.wordRecycleview.apply {
                    postDelayed({
                        smoothScrollToPosition(0)
                    }, delayTime)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })
        searchView.apply {
            this.imeOptions = EditorInfo.IME_ACTION_DONE
            setOnSearchClickListener {
                onSearchStateChange(true)
            }
            setOnCloseListener {
                onSearchStateChange(false)
                false
            }
        }
    }


    private fun toggleNightMode() {
        val activity = requireActivity()
        val nightMode: Boolean = getNightMode(activity)

        setDayNightMode(activity, !nightMode)

        AppCompatDelegate.setDefaultNightMode(
            when {
                nightMode -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
        activity.recreate()
    }

    /**
     * Enable the deletion of words, by swiping the item left or right.
     */
    private val itemTouchHelper = object : ItemTouchHelper
    .SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val word = wordAdapter.getWordAtPosition(position)
            notifyWordDeletion(word)

            viewModel.deleteWord(word)
        }

        private fun notifyWordDeletion(word: Word) {
            Snackbar.make(
                binding.root,
                getString(R.string.deleting) + word.word,
                Snackbar.LENGTH_LONG
            ).setAction(R.string.undo) {
                viewModel.insertWord(word, false)
            }.show()
        }
    }

    private fun initializeSelectedCategory() {
        val selectedCategory = getLastSelectedCategory(requireContext())
        viewModel.setCurrentCategory(selectedCategory)
    }

    /**
     * Navigates to the LookUpFragment and passes to it the word,
     * that needs to be looked up.
     */
    private fun lookUpTheNewWord(wordStr: String) {
        val url = getString(R.string.google_translate_site) + wordStr
        findNavController().navigate(
            WordDisplayFragmentDirections.actionWordDisplayFragmentToLookUpFragment(url)
        )
    }

    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }
}
