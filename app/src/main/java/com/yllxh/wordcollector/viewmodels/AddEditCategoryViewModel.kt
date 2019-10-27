package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.utils.AppUtils.Companion.isValidCategory
import com.yllxh.wordcollector.data.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddEditCategoryViewModel(application: Application) : AndroidViewModel(application) {
    // Job needed by the coroutine scope.
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    private val repository = AppRepository(application)

    private val defaultCategory = repository.defaultCategory

    /**
     * Used to keep track of the current category
     */
    private val currentCategory: String by lazy {
            AppPreferences.getLastSelectedCategory(application)
    }

    /**
     * Inserts a category in the database,
     * if the category is inserted it returns true and false otherwise.
     */
    fun insertCategory(category: Category): Boolean {
        return if (isValidCategory(defaultCategory, category)) {
            coroutineScope.launch {
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
        return if (isValidCategory(defaultCategory, newCategory, oldCategory)) {
            coroutineScope.launch {
                repository.update(newCategory, oldCategory)
            }
            if (oldCategory.name == currentCategory) {
                AppPreferences.setLastSelectedCategory(getApplication(), newCategory.name)
            }
            true
        } else false
    }
}