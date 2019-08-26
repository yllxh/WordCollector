package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.data.AppDatabase
import com.yllxh.wordcollector.data.Category
import kotlinx.coroutines.*


class ManageCategoriesViewModel(application: Application):
        AndroidViewModel(application){
    private val defaultCategory: String = application.getString(R.string.default_category_name)
    /**
     * Used to indicate if a new category was added to the list,
     * only used by the recycleView to check when the list of categories is changes,
     * if the change was caused because a new item was inserted it should be set to true,
     * this is done to inform you that the recycleView should scroll up to show the new item.
     */
    var newItemInserted = false

    // Database instance use to get the necessary Dao's
    private val db = AppDatabase.getInstance(application)
    private val wordDao = db.wordDao
    private val categoryDao = db.categoryDao

    // Job needed by the coroutine scope.
    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    // Gets all the categories as a List of LiveData
    var categories = categoryDao.getAll()

    /**
     * Used to check if a category is valid to perform operations with it.
     * oldCategory can be used in cases where the newCategory needs to be compared
     * with it.
     */
    private fun checkCategory(newCategory: Category, oldCategory: Category? = null): Boolean {
        if (newCategory.name.isEmpty() || newCategory.name == defaultCategory || oldCategory?.name == defaultCategory)
            return false
        else if (oldCategory != null) {
            if (newCategory.name != oldCategory.name)
                return true
        }
        return true
    }

    /**
     * Inserts a category in the database if it is a valid category,
     * if the category is inserted it returns true and false otherwise.
     */
    fun insertCategoryIfValid(category: Category): Boolean {
        if (checkCategory(category)) {
            coroutineScope.launch {
                insert(category)
                newItemInserted = true
            }
            return true
        }
        return false
    }

    /**
     * Updates a category in the database if it is a valid category,
     * it returns true if the category is updated and false otherwise.
     */
    fun updateCategoryIfValid(newCategory: Category, oldCategory: Category): Boolean {
        return when (checkCategory(newCategory, oldCategory)) {
            true -> {
                coroutineScope.launch {
                    update(newCategory.name, oldCategory.name)
                }
                true
            }
            else -> false
        }
    }

    /**
     * Used to delete the Category, unless it is the default category.
     */
    fun deleteCategory(category: Category): Boolean{
        if (category.name != defaultCategory) {
            coroutineScope.launch {
                delete(category)
            }
            return true
        }
        return false
    }

    /**
     * Suspend function used to insert a category.
     */
    private suspend fun insert(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }

    /**
     * Suspend function used to update a category, and it updates
     * all the words of the category to with the newName of the category.
     */
    private suspend fun update(newName: String, oldName: String) {
        withContext(Dispatchers.IO) {
            categoryDao.update(newName, oldName)
            wordDao.updateCategory(newName, oldName)
        }
    }

    /**
     * Suspend function used to delete a category.
     */
    private suspend fun delete(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.delete(category)
        }
    }


    override fun onCleared() {
        super.onCleared()
        // Cancels the job used by the coroutine.
        viewModelJob.cancel()
    }
}