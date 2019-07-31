package com.yllxh.wordcollector

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yllxh.wordcollector.data.AppDatabase
import com.yllxh.wordcollector.data.Category
import kotlinx.coroutines.*


class ManageCategoriesViewModel(application: Application):
        AndroidViewModel(application){
    /**
    Used to indicate if a new word was added to the list, only used by the recycleView to check,
    when the list of words is changes, if a new item was inserted, if it is true the recycleView
    scroll to the top of the list.
     */
    var newItemInserted = false

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
    private val db = AppDatabase.getInstance(application)
    var categories = db.categoryDao.getAll()

    private fun checkCategory(newCategory: Category, oldCategory: Category? = null): Boolean {
        if (newCategory.name.isEmpty() || newCategory.name == "All" || oldCategory?.name == "All")
            return false
        else if (oldCategory != null) {
            if (newCategory.name != oldCategory.name)
                return true
        }
        return true
    }

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
    private suspend fun insert(category: Category) {
        withContext(Dispatchers.IO) {
            db.categoryDao.insert(category)
        }
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

    private suspend fun update(newName: String, oldName: String) {
        withContext(Dispatchers.IO) {
            db.categoryDao.update(newName, oldName)
        }
    }

    fun deleteCategory(category: Category) {
        if (category.name != "All")
            coroutineScope.launch {
                delete(category)
            }
    }

    private suspend fun delete(category: Category) {
        withContext(Dispatchers.IO) {
            db.categoryDao.delete(category)
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}