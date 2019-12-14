package com.yllxh.wordcollector.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.utils.getLastSelectedCategory
import com.yllxh.wordcollector.utils.isValidWord
import com.yllxh.wordcollector.utils.setLastSelectedCategory
import kotlinx.coroutines.*

private const val TAG = "WordDViewModelAAAAAA"
class WordDisplayViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Job needed by the coroutine scope.
     */
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    var isUserSearching = false
    var newItemInserted = false
    var lastSearchedQuery = ""

    private val repository = AppRepository(application)
    val defaultCategory = repository.defaultCategory

    private val _selectedCategory by lazy {
        MutableLiveData<String>().apply {
            value = getLastSelectedCategory(application)
            Log.d(TAG, "_currentCategory Initialized. $value")
        }
    }
    val selectedCategory: LiveData<String>
        get() = _selectedCategory

    var words = repository.words
    var categories = repository.categories

    fun setCurrentCategory(name: String?) {
        val selectedCategory = name ?: defaultCategory
        _selectedCategory.value = selectedCategory
        setLastSelectedCategory(getApplication(), selectedCategory)
    }

    fun filterWordsToMatchQuery(queryString: String = lastSearchedQuery): List<Word>? {
        return words.value?.filter {
            it.word.contains(queryString, true)
                    || it.definition.contains(queryString, true)
        }
    }

    fun filterWordsToCategory(s: String? = _selectedCategory.value): List<Word>? {
        return when (s ?: defaultCategory) {
            defaultCategory -> words.value
            else -> words.value?.filter { it.category == _selectedCategory.value }
        }
    }

    fun insertWord(word: Word, isNewWord: Boolean = true): Boolean {
        if (isValidWord(word)) {
            coroutineScope.launch {
                repository.insert(word)
                newItemInserted = isNewWord
            }
            return true
        }
        return false
    }

    fun deleteWord(word: Word) {
        coroutineScope.launch {
            repository.delete(word)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cancels the job used by the coroutine.
        viewModelJob.cancel()
    }
}
