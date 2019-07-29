package com.yllxh.wordcollector.data

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface WordDao{
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: Word)

    @Update
    fun update(word: Word)

    @Query("SELECT * FROM word_table ORDER BY id DESC")
    fun getAll(): LiveData<List<Word>>

    @Query("SELECT * FROM word_table WHERE id = :id")
    fun get(id: Long): Word

    @Query("DELETE FROM word_table")
    fun deleteAll()

    @Delete
    fun delete(vararg words: Word): Int

}