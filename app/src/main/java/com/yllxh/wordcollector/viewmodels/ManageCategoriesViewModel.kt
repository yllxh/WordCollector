package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Category
import kotlinx.coroutines.*


class ManageCategoriesViewModel(application: Application) :
    AndroidViewModel(application) {

    // Job needed by the coroutine scope.
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val repository = AppRepository(application)

    val defaultCategory = repository.defaultCategory
    var newItemInserted = false

    var categories = repository.categories

    /**
     * Used to deleteWord the Category, unless it is the default category.
     */
    fun deleteCategory(category: Category): Boolean {
        if (category.name != defaultCategory) {
            coroutineScope.launch {
                repository.delete(category)
            }
            return true
        }
        return false
    }


    override fun onCleared() {
        super.onCleared()
        // Cancels the job used by the coroutine.
        viewModelJob.cancel()
    }

    /**
     * Deletes the specified category, including all the words of this category.
     */
    fun deleteAllOfCategory(category: Category): Boolean {
        return if (category.name != defaultCategory) {
            coroutineScope.launch {
                repository.deleteAllOfCategory(category)
            }
            true
        } else false
    }

    fun deleteAllWords() {
        coroutineScope.launch {
            repository.deleteAllWords()
        }
    }

    fun setCurrentCategory(name: String) {
        AppPreferences.setLastSelectedCategory(getApplication(), name)
    }
}