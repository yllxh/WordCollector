package com.yllxh.wordcollector

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.yllxh.wordcollector.adapters.WordAdapter
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.databinding.DialogEditWordBinding
import com.yllxh.wordcollector.databinding.FragmentWordDisplayBinding


class WordDisplayFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentWordDisplayBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        val viewModel = ViewModelProviders.of(this).get(WordDisplayViewModel::class.java)

        val adapter = WordAdapter(WordAdapter.OnEditClickListener { word ->
            DialogEditWordBinding.inflate(inflater, container, false).apply {
                editedWord.setText(word.word)
                editedDefinition.setText(word.definition)

                AlertDialog.Builder(activity).apply {
                    setView(root)
                    setPositiveButton(R.string.done) { _, _ ->
                        val newWord = editedWord.text.toString()
                        val newDefinition = editedDefinition.text.toString()

                        // If the word is not updated display a toast to inform the user
                        if (!viewModel.updateWordIfValid(Word(newWord, newDefinition), word))
                            toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)

                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    create().show()
                }
            }
        })

        viewModel.words.observe(this, Observer {
            adapter.submitList(it)

            // If the new word was inserted to the list, scroll to the Top of the recycleView
            if (viewModel.newItemInserted) {
                binding.recycleviewWordDisplay.smoothScrollToPosition(0)
                viewModel.newItemInserted = false
            }
        })
        binding.recycleviewWordDisplay.adapter = adapter
        binding.viewModel = viewModel

        viewModel.saveNewWord.observe(this, Observer {
            if (it) {
                val word = Word(
                    binding.newWordEditText.text.toString(),
                    binding.newDefinitionEditText.text.toString()
                )
                binding.apply {
                    newWordEditText.setText("")
                    newDefinitionEditText.setText("")
                }
                // If the word is not inserted, than it means that it is not valid
                if(!viewModel.insertWordIfValid(word)){
                    toast(getString(R.string.word_is_not_valid))
                }
            }

        })

        viewModel.lookUpWord.observe(this, Observer {
            if (it) {
                val str = binding.newWordEditText.text.toString().trim()
                if (str.isEmpty()) {
                    toast(getString(R.string.word_is_not_valid), Toast.LENGTH_LONG)
                } else {
                    lookUpTheNewWord(str)
                }
                viewModel.onLookUpWordCompleted()
            }
        })

        ItemTouchHelper(object : ItemTouchHelper
        .SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val word = adapter.getWordAtPosition(position)
                toast("Deleting " + word.word, Toast.LENGTH_LONG)
                viewModel.deleteWord(word)
            }
        }).attachToRecyclerView(binding.recycleviewWordDisplay)

        return binding.root
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item!!,
            view!!.findNavController()
        )
                || super.onOptionsItemSelected(item)
    }
}
