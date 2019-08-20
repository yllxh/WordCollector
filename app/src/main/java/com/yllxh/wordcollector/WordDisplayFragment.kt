package com.yllxh.wordcollector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.adapters.CategoryAdapter
import com.yllxh.wordcollector.adapters.WordAdapter
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.databinding.DialogEditWordBinding
import com.yllxh.wordcollector.databinding.FragmentWordDisplayBinding


class WordDisplayFragment : Fragment() {
    private lateinit var onEditWordListener: (Word) -> Unit

    @SuppressLint("RestrictedApi")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val binding = FragmentWordDisplayBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProviders.of(this).get(WordDisplayViewModel::class.java)
        binding.viewModel = viewModel

        // Create an instance of the CategoryAdapter with the necessary parameters
        val categoryAdapter = CategoryAdapter(activity as Context,false) {
            viewModel.onSelectCategory(it?.name ?: viewModel.defaultCategory)
        }
        binding.categoryRecycleview.adapter = categoryAdapter

        // Observing the categories for changes.
        viewModel.categories.observe(this, Observer {
            categoryAdapter.submitList(it)
        })

        onEditWordListener = { word ->
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
                        if (!viewModel.updateWordIfValid(
                                Word(
                                    newWord,
                                    newDefinition,
                                    viewModel.currentCategory.value ?: word.category),
                                word))
                            toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)

                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    create().show()
                }
            }
        }

        // Creating an instance of the WordAdapter class and setting a clickListener for the Edit ImageButton.
        val wordAdapter = WordAdapter(onEditWordListener)
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
                toast("Deleting " + word.word, Toast.LENGTH_LONG)
                viewModel.deleteWord(word)
            }
        }).attachToRecyclerView(binding.wordRecycleview)
        viewModel.words.observe(this, Observer {
            wordAdapter.submitList(it)

            // If the new word was inserted to the list, scroll to the Top of the recycleView
            if (viewModel.newItemInserted) {
                binding.wordRecycleview.smoothScrollToPosition(0)
                viewModel.newItemInserted = false
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
            binding.floatingActionButton.visibility= View.VISIBLE
        }

        // Sets onClickListener for the fab, to hide the itself and show the header of this fragment.
        binding.floatingActionButton.setOnClickListener {
            binding.enterNewWordCv.visibility = View.VISIBLE
            binding.floatingActionButton.visibility= View.GONE
        }
        initializeSharedPreferences()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater?.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            if (it.itemId == R.id.night_mode_menu){
                val dayNightKey = getString(R.string.day_night_key)
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
                val isNightMode = sharedPref?.getBoolean(dayNightKey, false)
                if (isNightMode == true){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    sharedPref.edit().putBoolean(dayNightKey, false).apply()
                }else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    sharedPref?.edit()?.putBoolean(dayNightKey, true)?.apply()
                }
                activity?.recreate()
                return true
            }
        }
        return NavigationUI.onNavDestinationSelected(
            item!!,
            view!!.findNavController()
        )
                || super.onOptionsItemSelected(item)
    }

    private fun initializeSharedPreferences(){
        val dayNightKey = getString(R.string.day_night_key)
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let {
            if (!it.contains(dayNightKey)) {
                it.edit().putBoolean(dayNightKey, false).apply()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }else{
                val isNightMode = it.getBoolean(dayNightKey, false)
                if (isNightMode){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        Log.d("AAA", ""+sharedPref?.getBoolean(dayNightKey, true))

    }

    private fun lookUpTheNewWord(str: String) {
        // Temporary solution to look up the new work on Google Translate
        val url = "https://translate.google.com/#view=home&op=translate&sl=auto&tl=en&text=$str"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun toast(s: String, lengthLong: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(activity, s, lengthLong).show()
    }
}
