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

    lateinit var oldWord: Word

    private val _selectedCategory by lazy {
        MutableLiveData<String>().apply {
            value = getLastSelectedCategory(application)
        }
    }
    val selectedCategory: LiveData<String>
        get() = _selectedCategory

    var categories = repository.categories

    fun setSelectedCategory(name: String? = oldWord.category) {
        val selectedCategory = name ?: oldWord.category
        _selectedCategory.value = selectedCategory
    }

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