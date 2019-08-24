package com.yllxh.wordcollector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.adapters.WordAdapter
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.databinding.DialogEditWordBinding
import com.yllxh.wordcollector.databinding.FragmentWordDisplayBinding


class WordDisplayFragment : Fragment() {
    private lateinit var binding: FragmentWordDisplayBinding
    private lateinit var wordAdapter: WordAdapter

    @SuppressLint("RestrictedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        initializeSharedPreferences()
        val viewModel = ViewModelProviders.of(this).get(WordDisplayViewModel::class.java)
        binding = FragmentWordDisplayBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        // Create an instance of the CategoryAdapter with the necessary parameters
        val categoryAdapter = CategoryAdapter(activity as Context, false) {
            viewModel.currentCategory.value = it?.name
        }
        binding.categoryRecycleview.adapter = categoryAdapter

        // Observing the categories for changes.
        viewModel.categories.observe(this, Observer {
            categoryAdapter.submitList(it)
        })

        val onEditClickListener: (Word) -> Unit = { word ->
            DialogEditWordBinding.inflate(inflater, container, false).apply {
                editedWord.setText(word.word)
                editedDefinition.setText(word.definition)
                dialogCategoryRecycleview.adapter = categoryAdapter

                AlertDialog.Builder(activity).apply {
                    setView(root)
                    setPositiveButton(R.string.done) { _, _ ->
                        val newWord = editedWord.text.toString()
                        val newDefinition = editedDefinition.text.toString()

                        // If the word is not updated display a toast to inform the user
                        val wasWordValid = viewModel.updateWordIfValid(
                            Word(newWord,newDefinition, viewModel.currentCategory.value ?: word.category),
                            word
                        )
                        if (!wasWordValid) {
                            toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)
                        }
                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    create().show()
                }
            }
        }

        // Creating an instance of the WordAdapter class and setting a clickListener for the Edit ImageButton.
        wordAdapter = WordAdapter(onEditClickListener)
        binding.wordRecycleview.adapter = wordAdapter

        // Enable the deletion of words, by swiping the item left or right.
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
                val word = wordAdapter.getWordAtPosition(position)
                Snackbar.make(
                    binding.root,
                    getString(R.string.deleting) + word.word,
                    Snackbar.LENGTH_LONG
                )
                .setAction(R.string.undo) {
                        viewModel.insertWordIfValid(word)
                }.show()

                viewModel.deleteWord(word)
            }
        }).attachToRecyclerView(binding.wordRecycleview)

        // Observes changes to the current category, in case it changes
        // the list of words shown in updated to the correct category.
        viewModel.currentCategory.observe(this, Observer {
            wordAdapter.submitList(
                viewModel.filterWordsToCurrentCategory(it ?: viewModel.defaultCategory)
            )
        })

        // Observes the list of words for changes. In case it changes it only need
        viewModel.words.observe(this, Observer {
            wordAdapter.submitList(
                viewModel.filterWordsToCurrentCategory()
            )
            // If the new word was inserted to the list, scroll to the Top of the recycleView
            if (viewModel.newItemInserted) {
                binding.wordRecycleview.smoothScrollToPosition(0)
                viewModel.newItemInserted = false
            }
        })

        viewModel.isUserSearching.observe(this, Observer {
            when (it) {
                true -> {
                    binding.enterNewWordCv.visibility = View.GONE
                    binding.floatingActionButton.visibility = View.GONE
                    binding.categoryRecycleview.visibility = View.GONE
                }
                else -> {
                    binding.enterNewWordCv.visibility = View.VISIBLE
                    binding.floatingActionButton.visibility = View.VISIBLE
                    binding.categoryRecycleview.visibility = View.VISIBLE
                    viewModel.currentCategory.value = viewModel.currentCategory.value
                }
            }
        })

        // Setting click listener for the Save textView button
        // if the EditTexts have valid content the word will be saved
        binding.saveTextView.setOnClickListener {
            binding.apply {
                val word = Word(
                    newWordEditText.text.toString(),
                    newDefinitionEditText.text.toString(),
                    viewModel.currentCategory.value ?: viewModel.defaultCategory
                )

                newWordEditText.setText("")
                newDefinitionEditText.setText("")
                // If the word is not inserted, than it means that it is not valid
                if (!viewModel.insertWordIfValid(word)) {
                    toast(getString(R.string.word_is_not_valid))
                }
            }
        }

        binding.lookUpTextView.setOnClickListener {
            val str = binding.newWordEditText.text.toString().trim()
            if (str.isEmpty()) {
                toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)
            } else {
                lookUpTheNewWord(str)
            }
        }

        // Sets onClickListener for the hideHeader imageButton, to hide the header on this fragment
        // and show a fab instead.
        binding.hideHeaderButton.setOnClickListener {
            binding.enterNewWordCv.visibility = View.GONE
            binding.floatingActionButton.visibility = View.VISIBLE
        }

        // Sets onClickListener for the fab, to hide the itself and show the header of this fragment.
        binding.floatingActionButton.setOnClickListener {
            binding.enterNewWordCv.visibility = View.VISIBLE
            binding.floatingActionButton.visibility = View.GONE
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.main_menu, menu)
        val viewModel = ViewModelProviders.of(this).get(WordDisplayViewModel::class.java)

        val searchView = menu?.findItem(R.id.menu_item_search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                wordAdapter.submitList(
                    viewModel.filterWordsToMatchQuery(newText)
                )
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

        })
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnSearchClickListener {
            viewModel.isUserSearching.value = true
        }
        searchView.setOnCloseListener (SearchView.OnCloseListener {
            viewModel.isUserSearching.value = false
            false
        })


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Update the NightMode of the app.
        if (item?.itemId == R.id.night_mode_menu_item) {
            item.let {
                val dayNightKey = getString(R.string.day_night_key)
                val sharedPref = activity?.getSharedPreferences(
                    getString(R.string.shared_preferences_file_key),
                    Context.MODE_PRIVATE
                )
                val isNightMode = sharedPref?.getBoolean(dayNightKey, false)
                if (isNightMode == true) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sharedPref.edit().putBoolean(dayNightKey, false).apply()
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPref?.edit()?.putBoolean(dayNightKey, true)?.apply()
                }
                activity?.recreate()
                return true
            }
        }
        return NavigationUI.onNavDestinationSelected(item!!, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

    /**
     * Checks if the app has stored a mode for the night mode.
     * If it does not, have a saved mode, it saves a new mode (MODE_NIGHT_YES) and sets the current
     * mode for the app, if it does have a night mode set, then it sets the mode of the app to it.
     */
    private fun initializeSharedPreferences() {
        activity?.getSharedPreferences(
            getString(R.string.shared_preferences_file_key),
            Context.MODE_PRIVATE
        )?.let {
            val key = getString(R.string.day_night_key)
            if (!it.contains(key)) {
                it.edit().putBoolean(key, false).apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                val isNightMode = it.getBoolean(key, false)
                if (isNightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }

    /* Opens a browser to look up the new work on Google Translate. */
    private fun lookUpTheNewWord(str: String) {
        val url = getString(R.string.google_translate_site) + str
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)

        if (i.resolveActivity(activity?.packageManager) != null) {
            startActivity(i)
        }else{
            toast(getString(R.string.look_up_failed))
        }
    }

    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }
}
