package com.yllxh.wordcollector.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.yllxh.wordcollector.utils.DataUtils.Companion.DEFAULT_CATEGORY_NAME
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "word_table")
@Parcelize
data class Word(
    var word: String = "",
    var definition: String = "",
    var category: String = DEFAULT_CATEGORY_NAME,
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L): Parcelable {

    @Ignore
    constructor(
        word: String = "",
        definition: String = "",
        category: String = DEFAULT_CATEGORY_NAME
    ):this(word, definition, category, 0)
}