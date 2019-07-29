package com.yllxh.wordcollector.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "word_table")
data class Word(
    var word: String = "",
    var definition: String = "",
    var category: String = "All",
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L){

    @Ignore
    constructor(
        word: String = "",
        definition: String = "",
        category: String = "All"
    ):this(word, definition, category, 0)
}