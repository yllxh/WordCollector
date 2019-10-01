package com.yllxh.wordcollector

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.yllxh.wordcollector.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(application: Application) {
    val defaultCategory: String = application.getString(R.string.default_category_name)

    private val wordDao: WordDao
    private val categoryDao: CategoryDao

    init {
        val db = AppDatabase.getInstance(application)
        wordDao = db.wordDao
        categoryDao = db.categoryDao
    }

    /* Gets all the words as a List of LiveData */
    var words = wordDao.getAll()

    /* Gets all the categories as a List of LiveData */
    var categories = categoryDao.getAll()


    /**
     * Suspend function used to updateCategory a word.
     */
    suspend fun update(word: Word){
        withContext(Dispatchers.IO){
            wordDao.update(word)

        }
    }

    /**
     * Suspend function used to insert a word.
     */
    suspend fun insert(word: Word){
        withContext(Dispatchers.IO){
            wordDao.insert(word)
        }
    }

    /**
     * Suspend function used to delete a word.
     */
    suspend fun delete(word: Word) {
        withContext(Dispatchers.IO) {
            wordDao.delete(word)
        }
    }

    /**
     * Suspend function used to insert a category.
     */
    suspend fun insert(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }

    /**
     * Suspend function used to updateCategory a category, and it updates
     * all the words of the category to with the newName of the category.
     */
    suspend fun update(newName: String, oldName: String) {
        withContext(Dispatchers.IO) {
            categoryDao.update(newName, oldName)
            wordDao.updateCategory(newName, oldName)
        }
    }

    /**
     * Suspend function used to delete a category.
     */
    suspend fun delete(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.delete(category)
        }
    }

    suspend fun deleteAllOfCategory(category: Category) {
        withContext(Dispatchers.IO){
            wordDao.deleteAllOfCategory(category.name)
        }
    }

    suspend fun deleteAllWords() {
        withContext(Dispatchers.IO){
            wordDao.deleteAll()
        }
    }


}