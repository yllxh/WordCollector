package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.utils.getLastSelectedCategory
import com.yllxh.wordcollector.utils.isValidNewWord
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
            value = getLastSelectedCategory(application)
        }
    }
    val currentCategory: LiveData<String>
        get() = _currentCategory

    var categories = repository.categories

    fun setCurrentCategory(name: String?) {
        val selectedCategory = name ?: defaultCategory
        _currentCategory.value = selectedCategory
    }

    /**
     * Updates a word in the database,
     * it returns true if the word is updated and false otherwise.
     */
    fun update(newWord: Word, oldWord: Word): Boolean {
        return if (isValidNewWord(newWord, oldWord)) {
            newWord.id = oldWord.id
            coroutineScope.launch {
                repository.update(newWord, oldWord)
            }
            true
        } else false
    }


}