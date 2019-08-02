package com.yllxh.wordcollector

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.yllxh.wordcollector.data.AppDatabase
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.Word
import kotlinx.coroutines.*


class WordDisplayViewModel(application: Application) : AndroidViewModel(application) {
    val defaultCategory: String = application.getString(R.string.all)
    /**
     * Used to indicate if a new word was added to the list,
     * only used by the recycleView to check when the list of words is changes,
     * if the change was caused because a new item was inserted it should be set to true,
     * this is done to inform you that the recycleView should scroll up to show the new item.
     */
    var newItemInserted = false

    // Used to keep track of the current category
    var currentCategory: MutableLiveData<String> = MutableLiveData()

    // Database instance use to get the necessary Dao's
    private val db = AppDatabase.getInstance(application)
    private val wordDao = db.wordDao
    private val categoryDao = db.categoryDao

    // Job needed by the coroutine scope.
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /**
     * Used to insert a category named as the defaultCategory in case
     * there is no such category, and also used  to get the last selected
     * category from the database.
     */
    init {
        currentCategory.value = defaultCategory
        coroutineScope.launch {
            insert(Category(defaultCategory, 1))
        }
    }

    // The words that are displayed will depend
    // on the currentCategory which is selected by the user
    var words: LiveData<List<Word>> = Transformations.switchMap(currentCategory) {
        when (it) {
            defaultCategory -> wordDao.getAll()
            else -> wordDao.getWordsOfCategory(it)
        }
    }
    // Gets all the categories as a List of LiveData
    var categories = categoryDao.getAll()

    /**
     * Used to update the selected category in the database.
     */
    fun onSelectCategory(s: String) {
        currentCategory.value = s
        coroutineScope.launch {
            withContext(Dispatchers.IO){
                categoryDao.deselectPreviousSelection()
                categoryDao.updateSelectedCategory(currentCategory.value)
            }
        }
    }


    /**
     * Used to check if a word is valid to perform operations with it.
     * oldWord can be used in cases where the newWord needs to be compared
     * with it.
     */
    private fun checkWord(newWord: Word, oldWord: Word? = null): Boolean {
        if (newWord.word.isEmpty() && newWord.definition.isEmpty())
            return false
        else if (oldWord != null) {
            if (newWord.word != oldWord.word || newWord.definition != oldWord.definition)
                return true
        }
        return true
    }

    /**
     * Inserts a word in the database if it is a valid word,
     * if the word is inserted it returns true and false otherwise.
     */
    fun insertWordIfValid(word: Word): Boolean {
        if (checkWord(word)) {
            coroutineScope.launch {
                insert(word)
                newItemInserted = true
            }
            return true
        }
        return false
    }

    /**
     * Updates a word in the database if it is a valid word,
     * it returns true if the word is updated and false otherwise.
     */
    fun updateWordIfValid(newWord: Word, oldWord: Word): Boolean {
        return when (checkWord(newWord, oldWord)) {
            true -> {
                newWord.id = oldWord.id
                coroutineScope.launch {
                    update(newWord)
                }
                true
            }
            else -> false
        }
    }

    /**
     * Used to delete a word from the database using a coroutine.
     */
    fun deleteWord(word: Word) {
        coroutineScope.launch {
            delete(word)
        }
    }

    /**
     * Suspend function used to update a word
     */
    private suspend fun update(word: Word) {
        withContext(Dispatchers.IO) {
            wordDao.update(word)
        }
    }

    /**
     * Suspend function used to insert a word.
     */
    private suspend fun insert(word: Word) {
        withContext(Dispatchers.IO) {
            wordDao.insert(word)
        }
    }

    /**
     * Suspend function used to delete a word.
     */
    private suspend fun delete(word: Word) {
        withContext(Dispatchers.IO) {
            wordDao.delete(word)
        }
    }

    /**
     * Suspend function used to insert a category.
     */
    private suspend fun insert(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }




    override fun onCleared() {
        super.onCleared()
        // Cancels the job used by the coroutine.
        viewModelJob.cancel()
    }

}
