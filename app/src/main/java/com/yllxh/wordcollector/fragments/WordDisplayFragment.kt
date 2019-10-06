package com.yllxh.wordcollector.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.viewmodels.WordDisplayViewModel
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.adapters.WordAdapter
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.databinding.DialogEditWordBinding
import com.yllxh.wordcollector.databinding.FragmentWordDisplayBinding

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
        initializeNightMode()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        binding = FragmentWordDisplayBinding.inflate(inflater, container, false)

        categoryAdapter = CategoryAdapter(requireContext()) {
            viewModel.setCurrentCategory(it.name)
        }
        binding.categoryRecycleview.adapter = categoryAdapter


        val onEditClickListener: (Word) -> Unit = { word ->
            val binding = DialogEditWordBinding.inflate(inflater, container, false).apply {
                data = word
                dialogCategoryRecycleview.adapter = categoryAdapter
            }

            val dialog = AlertDialog.Builder(activity)
                .setView(binding.root)
                .show()

            binding.saveButton.setOnClickListener {
                // If the word is not updated display a toast to inform the user
                val wasWordValid = viewModel.update(
                    Word(
                        binding.editedWord.text.toString(),
                        binding.editedDefinition.text.toString(),
                        viewModel.currentCategory.value ?: word.category
                    ), word)

                if (!wasWordValid) {
                    toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)
                }
                dialog.cancel()
            }
            binding.cancelButton.setOnClickListener {
                dialog.cancel()
            }
        }

        // Creating an instance of the WordAdapter class and setting a clickListener for the Edit ImageButton.
        wordAdapter = WordAdapter(onEditClickListener)
        binding.wordRecycleview.adapter = wordAdapter


        // Enable the deletion of words, by swiping the item left or right.
        ItemTouchHelper(object : ItemTouchHelper
        .SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val word = wordAdapter.getWordAtPosition(position)
                Snackbar.make(
                    binding.root,
                    getString(R.string.deleting) + word.word,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.undo) {
                    viewModel.insert(word, false)
                }.show()

                viewModel.delete(word)
            }
        }).attachToRecyclerView(binding.wordRecycleview)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observing the categories for changes.
        viewModel.categories.observe(this@WordDisplayFragment, Observer { list ->
            categoryAdapter.submitList(list.toMutableList())

            viewModel.apply {
                if (list.none { it.name == currentCategory.value }) {
                    setCurrentCategory(defaultCategory)
                }
            }
        })

        // Observes changes to the current category, in case it changes
        // the list of words shown in updated to the correct category.
        viewModel.currentCategory.observe(this, Observer {
            wordAdapter.submitList(
                viewModel.filterWordsToCategory(it)
            )
        })

        // Observes the list of words for changes. In case it changes it only need
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


        // Setting necessary onClickListeners to views
        binding.apply {
            // Setting click listener for the Save textView button
            // if the EditTexts have valid content the word will be saved
            saveTextview.setOnClickListener {
                val word = Word(
                    newWordEditText.text.toString(),
                    newDefinitionEditText.text.toString(),
                    viewModel.currentCategory.value ?: viewModel.defaultCategory
                )

                newWordEditText.setText("")
                newDefinitionEditText.setText("")
                // If the word is not inserted, than it means that it is not valid
                if (!viewModel.insert(word)) {
                    toast(getString(R.string.word_is_not_valid))
                }
            }

            lookUpTextview.setOnClickListener {
                val str = newWordEditText.text.toString().trim()
                if (str.isEmpty()) {
                    toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)
                } else {
                    lookUpTheNewWord(str)
                }
            }

            // Sets onClickListener for the hideHeader imageButton,
            // to hide the header on this fragment and show a fab instead.
            hideHeaderButton.setOnClickListener {
                enterNewWordCardview.visibility = View.GONE
                floatingActionButton.show()
            }

            // Hide the itself and show the header of this fragment.
            floatingActionButton.setOnClickListener {
                enterNewWordCardview.visibility = View.VISIBLE
                floatingActionButton.hide()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initializeSelectedCategory()
    }

    /**
     * Sets the correct visibility to views on the screen.
     */
    private fun onSearchingStateChange(isSearching: Boolean){
        when {
            isSearching -> {
                binding.enterNewWordCardview.visibility = View.GONE
                binding.categoryRecycleview.visibility = View.GONE
            }
            else -> {
                binding.enterNewWordCardview.visibility = View.VISIBLE
                binding.categoryRecycleview.visibility = View.VISIBLE
                viewModel.apply {
                    setCurrentCategory(currentCategory.value)
                }
            }
        }
        binding.floatingActionButton.hide()
        viewModel.isUserSearching = isSearching

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)

        // Setup up the behaviour of the SearchView
        val searchView = menu.findItem(R.id.menu_item_search)?.actionView as SearchView
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
                onSearchingStateChange(true)
            }
            setOnCloseListener {
                onSearchingStateChange(false)
                false
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.night_mode_menu_item) {
            toggleNightMode()
            return true
        }
        return NavigationUI.onNavDestinationSelected(item, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

    private fun toggleNightMode() {
        val activity = requireActivity()
        val nightMode: Boolean

        AppPreferences.apply {
            nightMode = getNightMode(activity)
            setDayNightMode(activity, !nightMode)
        }

        AppCompatDelegate.setDefaultNightMode(
            when {
                nightMode -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
        activity.recreate()
    }

    private fun initializeNightMode() {
        val isNightMode = AppPreferences.getNightMode(requireContext())
        AppCompatDelegate.setDefaultNightMode(
            when {
                isNightMode -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    private fun initializeSelectedCategory() {
        val selectedCategory = AppPreferences.getLastSelectedCategory(requireContext())
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
