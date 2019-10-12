package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditWordViewModel(application: Application) : AndroidViewModel(application){

    /**
     * Job needed by the coroutine scope.
     */
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val repository = AppRepository(application)
    private val defaultCategory = repository.defaultCategory


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

    var categories = repository.categories

    fun setCurrentCategory(name: String?) {
        val selectedCategory = name ?: defaultCategory
        _currentCategory.value = selectedCategory
        AppPreferences.setLastSelectedCategory(getApplication(), selectedCategory)
    }

    /**
     * Updates a word in the database,
     * it returns true if the word is updated and false otherwise.
     */
    fun update(newWord: Word, oldWord: Word): Boolean {
        return if (isValidWord(newWord, oldWord)) {
            newWord.id = oldWord.id
            coroutineScope.launch {
                repository.update(newWord, oldWord)
            }
            true
        } else false
    }

    /**
     * Used to check if a word is valid to perform operations with it.
     * @param oldWord  Can be used in cases where the newWord needs to be compared with it.
     */
    private fun isValidWord(newWord: Word, oldWord: Word? = null): Boolean {
        if (newWord.word.isEmpty() && newWord.definition.isEmpty())
            return false
        else if (oldWord != null) {
            if (newWord.word != oldWord.word
                || newWord.definition != oldWord.definition
            )
                return true
        }
        return true
    }
}