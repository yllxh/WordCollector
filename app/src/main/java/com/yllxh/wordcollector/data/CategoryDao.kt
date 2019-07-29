package com.yllxh.wordcollector.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(category: Category)

    @Update
    fun update(category: Category)

    @Query("SELECT * FROM category_table")
    fun getAll(): LiveData<List<Category>>

    @Query("SELECT * FROM category_table WHERE name = :name")
    fun get(name: String): Category

    @Query("DELETE FROM category_table")
    fun deleteAll()

    @Delete
    fun delete(vararg category: Category): Int

}