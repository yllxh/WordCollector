package com.yllxh.wordcollector.data

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey
    var name: String = "All"
){

}