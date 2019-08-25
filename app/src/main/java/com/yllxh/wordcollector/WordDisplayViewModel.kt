package com.yllxh.wordcollector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.yllxh.wordcollector.data.AppDatabase
import com.yllxh.wordcollector.data.Word
import kotlinx.coroutines.*


class WordDisplayViewModel(application: Application) : AndroidViewModel(application) {
    val defaultCategory: String = application.getString(R.string.default_category_name)
    /**
     * Used to indicate if a new word was added to the list.
     */
    var isNewItemInserted = MutableLiveData<Boolean>()

    /**
     * Used to indicate if the whether or not the user is searching.
     */
    var isUserSearching = MutableLiveData<Boolean>()

    /**
     * Used to keep track of the current category
     */
    var currentCategory = MutableLiveData<String>()

    /**
     * Database instance use to get the necessary Dao's
     */
    private val db = AppDatabase.getInstance(application)
    private val wordDao = db.wordDao
    private val categoryDao = db.categoryDao

    /* Job needed by the coroutine scope. */
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /* Gets all the words as a List of LiveData */
    var words = wordDao.getAll()

    /* Gets all the categories as a List of LiveData */
    var categories = categoryDao.getAll()

    /**
     * Initializing LiveData with default values.
     */
    init {
        isNewItemInserted.value = false
        isUserSearching.value = false
    }

    /**
     * Returns a filtered list of words of the current category.
     */
    fun filterWordsToCurrentCategory(s: String = currentCategory.value ?: defaultCategory): List<Word>? {
        return when (s) {
            defaultCategory -> words.value
            else -> words.value?.filter { it.category == currentCategory.value }
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
     * In case that the function is used to undo a deletion the isNewWord parameter
     * can be used to indicate that.
     * @param isNewWord  Indicates if this word existed before.
     */
    fun insertWordIfValid(word: Word, isNewWord: Boolean = true): Boolean {
        if (checkWord(word)) {
            coroutineScope.launch {
                insert(word)
                isNewItemInserted.value = isNewWord
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

    override fun onCleared() {
        super.onCleared()
        // Cancels the job used by the coroutine.
        viewModelJob.cancel()
    }

    /**
     * Returns a filtered list of the all the words that contain the queryString.
     */
    fun filterWordsToMatchQuery(queryString: String): MutableList<Word>? {
        return words.value?.filter {
            it.word.contains(queryString, true)
                    || it.definition.contains(queryString, true)
        }?.toMutableList()
    }

}
