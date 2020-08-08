package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeleteCategoryViewModel(application: Application) : AndroidViewModel(application){

    private val repository = AppRepository(application)

    val defaultCategory = repository.defaultCategory
    lateinit var passedCategory: Category

    /**
     * Deletes the specified category, including all the words of this category.
     */
    fun deleteAllOfCategory(category: Category): Boolean {
        return if (category.name != defaultCategory) {
            viewModelScope.launch {
                repository.deleteAllOfCategory(category)
            }
            deleteCategory(category)
            true
        } else false
    }

    /**
     * Used to deleteWord the Category, unless it is the default category.
     */
    fun deleteCategory(category: Category): Boolean {
        if (category.name != defaultCategory) {
            viewModelScope.launch {
                repository.delete(category)
            }
            return true
        }
        return false
    }

    fun deleteAllWords() {
        viewModelScope.launch {
            repository.deleteAllWords()
        }
    }
}