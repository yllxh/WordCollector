package com.yllxh.wordcollector

import android.app.Application
import com.yllxh.wordcollector.data.*
import com.yllxh.wordcollector.utils.DataUtils.DEFAULT_CATEGORY_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(application: Application) {
    val defaultCategory: String = DEFAULT_CATEGORY_NAME

    private val wordDao: WordDao
    private val categoryDao: CategoryDao

    init {
        val db = AppDatabase.getInstance(application)
        wordDao = db.wordDao
        categoryDao = db.categoryDao
    }

    var words = wordDao.getAll()
    var categories = categoryDao.getAll()


    suspend fun update(new: Word, old: Word) {
        withContext(Dispatchers.IO) {
            wordDao.update(new)

            val isCategoryUpdated = new.category != old.category
            if (isCategoryUpdated) {
                updateCategoryWordCount(new, old)
            }

        }
    }


    /**
     * Handlers the updating of the word count of new and old category.
     *
     * @param new   The new word updated word.
     * @param old   The old version of the new word.
     */
    private fun updateCategoryWordCount(new: Word, old: Word) {
        when {
            new.category == defaultCategory -> {
                categoryDao.decrementWordCount(old.category)
            }
            new.category != defaultCategory -> {
                categoryDao.incrementWordCount(new.category)

                val wasDefaultCategory = old.category == defaultCategory
                if (!wasDefaultCategory)
                    categoryDao.decrementWordCount(old.category)
            }
        }
    }

    suspend fun insert(word: Word) {
        withContext(Dispatchers.IO) {
            wordDao.insert(word)
            categoryDao.incrementWordCount(word.category)
            if (word.category != defaultCategory) {
                categoryDao.incrementTotalWordCount()
            }
        }
    }

    suspend fun delete(word: Word) {
        withContext(Dispatchers.IO) {
            wordDao.delete(word)
            categoryDao.decrementWordCount(word.category)
            if (word.category != defaultCategory) {
                categoryDao.decreaseTotalWordCount()
            }
        }
    }

    suspend fun insert(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.insert(category)
        }
    }

    /**
     * Function used to updateCategory a category, and it updates
     * all the words of the category to with the newName of the category.
     */
    suspend fun update(newCategory: Category, oldCategory: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.update(newCategory.name, oldCategory.name)
            wordDao.updateCategory(newCategory.name, oldCategory.name)
        }
    }

    suspend fun delete(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.delete(category)
        }
    }

    /**
     * Function used to delete All the word of a category, and decreases total word count
     * by se number of words in that category.
     *
     * @param   category The category of the words that need to be deleted.
     */
    suspend fun deleteAllOfCategory(category: Category) {
        withContext(Dispatchers.IO) {
            wordDao.deleteAllOfCategory(category.name)
            categoryDao.decreaseTotalWordCount(category.wordCount)
        }
    }

    suspend fun deleteAllWords() {
        withContext(Dispatchers.IO) {
            wordDao.deleteAll()
            categoryDao.setAllWordCountToZero()
        }
    }


}