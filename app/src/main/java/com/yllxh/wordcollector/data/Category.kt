package com.yllxh.wordcollector.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "category_table")
@Parcelize
data class Category(
    @PrimaryKey
    var name: String = DEFAULT_CATEGORY_NAME,
    var wordCount: Int = 0
) : Parcelable