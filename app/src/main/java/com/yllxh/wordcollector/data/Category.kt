package com.yllxh.wordcollector.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey
    var name: String = "All",
    var isSelected: Int = 0
){

}