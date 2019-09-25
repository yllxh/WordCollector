package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Category
import kotlinx.coroutines.*


class ManageCategoriesViewModel(application: Application):
        AndroidViewModel(application){
    private val repository = AppRepository(application)

    val defaultCategory = repository.defaultCategory
    var newItemInserted = false

    /**
     * Used to keep track of the current category
     */
    private val _currentCategory: MutableLiveData<String> by lazy {
        MutableLiveData<String>().apply {
            value = AppPreferences.getLastSelectedCategory(application)
        }

    }
    val currentCategory: LiveData<String>
        get() = _currentCategory

    // Job needed by the coroutine scope.
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // Gets all the categories as a List of LiveData
    var categories = repository.categories

    /**
     * Used to check if a category is valid to perform operations with it.
     * oldCategory can be used in cases where the newCategory needs to be compared
     * with it.
     */
    private fun isValidCategory(newCategory: Category, oldCategory: Category? = null): Boolean {
        if (newCategory.name.isEmpty()
                || newCategory.name == defaultCategory
                    || oldCategory?.name == defaultCategory)
            return false

        else if (oldCategory != null) {
            if (newCategory.name != oldCategory.name)
                return true
        }
        return true
    }

    /**
     * Inserts a category in the database,
     * if the category is inserted it returns true and false otherwise.
     */
    fun insertCategory(category: Category): Boolean {
        return if (isValidCategory(category)) {
            coroutineScope.launch {
                repository.insert(category)
                newItemInserted = true
            }
            true
        }
        else false
    }

    /**
     * Updates a category in the database if it is a valid category,
     * it returns true if the category is updated and false otherwise.
     */
    fun updateCategory(newCategory: Category, oldCategory: Category): Boolean {
        return if (isValidCategory(newCategory, oldCategory)) {
            coroutineScope.launch {
                repository.update(newCategory.name, oldCategory.name)
            }
            true
        }
        else false
    }

    /**
     * Used to delete the Category, unless it is the default category.
     */
    fun deleteCategory(category: Category): Boolean{
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
        }
        else false
    }
}