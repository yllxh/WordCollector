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
    suspend fun update(new: Word, old: Word){
        withContext(Dispatchers.IO){
            wordDao.update(new)

            if (new.category != old.category) {
                when {
                    new.category == defaultCategory -> {
                        categoryDao.decrementWordCount(old.category)
                    }
                    new.category != defaultCategory -> {
                        categoryDao.incrementWordCount(new.category)
                        if (old.category != defaultCategory)
                            categoryDao.decrementWordCount(old.category)
                    }
                }
            }

        }
    }

    /**
     * Suspend function used to insert a word.
     */
    suspend fun insert(word: Word){
        withContext(Dispatchers.IO){
            wordDao.insert(word)
            categoryDao.incrementWordCount(word.category)
            if (word.category != defaultCategory) {
                categoryDao.incrementTotalWordCount()
            }
        }
    }

    /**
     * Suspend function used to delete a word.
     */
    suspend fun delete(word: Word) {
        withContext(Dispatchers.IO) {
            wordDao.delete(word)
            categoryDao.decrementWordCount(word.category)
            if (word.category != defaultCategory) {
                categoryDao.decrementTotalWordCount()
            }
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
    suspend fun update(newCategory: Category, oldCategory: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.update(newCategory.name, oldCategory.name)
            wordDao.updateCategory(newCategory.name, oldCategory.name )
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
            categoryDao.setAllWordCountToZero()
        }
    }


}