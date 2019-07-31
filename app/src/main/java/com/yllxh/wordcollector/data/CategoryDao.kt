package com.yllxh.wordcollector.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(category: Category)

    @Query("UPDATE category_table SET name = :newName WHERE name = :oldName")
    fun update(newName: String, oldName: String)

    @Query("SELECT * FROM category_table")
    fun getAll(): LiveData<List<Category>>

    @Query("SELECT * FROM category_table WHERE name = :name")
    fun get(name: String): Category

    @Query("DELETE FROM category_table")
    fun deleteAll()

    @Delete
    fun delete(vararg category: Category): Int

    @Query("UPDATE category_table SET isSelected = 0 WHERE isSelected = 1")
    fun deselectPreviousSelection()

    @Query("UPDATE category_table SET isSelected = 1 WHERE name = :selectedCategory")
    fun updateSelectedCategory(selectedCategory: String?)

}