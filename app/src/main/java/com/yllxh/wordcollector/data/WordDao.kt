package com.yllxh.wordcollector.data

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: Word)

    @Update
    fun update(word: Word)

    @Query("UPDATE word_table SET category = :newCategory WHERE category = :oldCategory")
    fun updateCategory(newCategory: String, oldCategory: String)

    @Query("SELECT * FROM word_table ORDER BY id DESC")
    fun getAll(): LiveData<List<Word>>

    @Query("SELECT * FROM word_table WHERE id = :id")
    fun get(id: Long): Word

    @Query("SELECT * FROM word_table WHERE category = :category ORDER BY id DESC")
    fun getWordsOfCategory(category: String): LiveData<List<Word>>

    @Query("DELETE FROM word_table WHERE category = :name")
    fun deleteAllOfCategory(name: String)

    @Delete
    fun delete(vararg words: Word): Int

    @Query("DELETE FROM word_table")
    fun deleteAll(): Int

}