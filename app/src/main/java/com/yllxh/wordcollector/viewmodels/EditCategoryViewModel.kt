package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditCategoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val currentCategory: String by lazy {
        getLastSelectedCategory(application)
    }
    lateinit var passedCategory: Category

    /**
     * Inserts a category in the database,
     * if the category is inserted it returns true and false otherwise.
     */
    fun insertCategory(category: Category): Boolean {
        return if (isValidCategory(category)) {
            viewModelScope.launch {
                repository.insert(category)
            }
            true
        } else false
    }

    /**
     * Updates a category in the database if it is a valid category,
     * it returns true if the category is updated and false otherwise.
     */
    fun updateCategory(newCategory: Category, oldCategory: Category): Boolean {
        return if (isValidNewCategory(newCategory, oldCategory)) {
            viewModelScope.launch {
                repository.update(newCategory, oldCategory)
            }
            if (oldCategory.name == currentCategory) {
                setLastSelectedCategory(getApplication(), newCategory.name)
            }
            true
        } else false
    }
}