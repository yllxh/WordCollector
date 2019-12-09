package com.yllxh.wordcollector.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yllxh.wordcollector.utils.DataUtils.DEFAULT_CATEGORY_NAME

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(category: Category)

    @Query("UPDATE category_table SET name = :newName WHERE name = :oldName")
    fun update(newName: String, oldName: String)

    @Query("UPDATE category_table SET wordCount = wordCount + 1 WHERE name = :name")
    fun incrementWordCount(name: String)

    @Query("UPDATE category_table SET wordCount = wordCount - 1 WHERE name = :name")
    fun decrementWordCount(name: String)

    @Query("UPDATE category_table SET wordCount = wordCount + 1 WHERE name = '$DEFAULT_CATEGORY_NAME'")
    fun incrementTotalWordCount()

    @Query("UPDATE category_table SET wordCount = wordCount - :count WHERE name = '$DEFAULT_CATEGORY_NAME'")
    fun decreaseTotalWordCount(count: Int = 1)

    @Query("UPDATE category_table SET wordCount = 0")
    fun setAllWordCountToZero()

    @Query("SELECT * FROM category_table")
    fun getAll(): LiveData<List<Category>>

    @Delete
    fun delete(vararg category: Category): Int

    @Query("SELECT * FROM category_table LIMIT 1")
    fun getAnyCategory(): IntArray
}