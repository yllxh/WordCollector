package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Word
import kotlinx.coroutines.*


class WordDisplayViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Job needed by the coroutine scope.
     */
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    var isUserSearching = false
    var newItemInserted = false

    private val repository = AppRepository(application)
    val defaultCategory = repository.defaultCategory
    /**
     * Used to keep track of the current category
     */
    private val _currentCategory by lazy {
        MutableLiveData<String>().apply {
            value = AppPreferences.getLastSelectedCategory(application)
        }
    }
    val currentCategory: LiveData<String>
        get() = _currentCategory

    var words = repository.words
    var categories = repository.categories

    /**
     * Used to check if a word is valid to perform operations with it.
     * @param oldWord  Can be used in cases where the newWord needs to be compared with it.
     */
    private fun isValidWord(newWord: Word, oldWord: Word? = null): Boolean {
        if (newWord.word.isEmpty() && newWord.definition.isEmpty())
            return false
        else if (oldWord != null) {
            if (newWord.word != oldWord.word
                || newWord.definition != oldWord.definition)
                return true
        }
        return true
    }

    /**
     * Inserts a word in the database,
     * if the word is inserted it returns true and false otherwise.
     *
     * @param isNewWord  Indicates if this word existed before.
     */
    fun insert(word: Word, isNewWord: Boolean = true): Boolean {
        if (isValidWord(word)) {
            coroutineScope.launch {
                repository.insert(word)
                newItemInserted = isNewWord
            }
            return true
        }
        return false
    }

    /**
     * Updates a word in the database,
     * it returns true if the word is updated and false otherwise.
     */
    fun update(newWord: Word, oldWord: Word): Boolean {
        return if (isValidWord(newWord, oldWord)) {
            newWord.id = oldWord.id
            coroutineScope.launch {
                repository.update(newWord)
            }
            true
        }
        else false
    }

    fun delete(word: Word) {
        coroutineScope.launch {
            repository.delete(word)
        }
    }

    /**
     * Returns a filtered list of the all the words that contain the queryString.
     */
    fun filterWordsToMatchQuery(queryString: String): List<Word>? {
        return words.value?.filter {
            it.word.contains(queryString, true)
                    || it.definition.contains(queryString, true)
        }
    }

    /**
     * Returns a filtered list of words of the current category.
     */
    fun filterWordsToCategory(s: String? = _currentCategory.value): List<Word>? {
        return when (s ?: defaultCategory) {
            defaultCategory -> words.value
            else -> words.value?.filter { it.category == _currentCategory.value }
        }
    }

    fun setCurrentCategory(selectedCategory: String?) {
        _currentCategory.value = selectedCategory ?: defaultCategory
    }

    override fun onCleared() {
        super.onCleared()
        // Cancels the job used by the coroutine.
        viewModelJob.cancel()
    }
}
