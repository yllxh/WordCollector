package com.yllxh.wordcollector

import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.Word

class AppUtils {

    companion object {
        const val DATABASE_NAME = "app_database"

        /**
         * Used to check if a word is valid to perform operations with it.
         * @param oldWord  Can be used in cases where the newWord needs to be compared with it.
         */
        fun isValidWord(newWord: Word, oldWord: Word? = null): Boolean {
            if (newWord.word.isEmpty() && newWord.definition.isEmpty())
                return false
            else if (oldWord != null) {
                if (newWord.word != oldWord.word
                    || newWord.definition != oldWord.definition
                )
                    return true
            }
            return true
        }

        /**
         * Used to check if a category is valid to perform operations with it.
         * oldCategory can be used in cases where the newCategory needs to be compared
         * with it.
         */
        fun isValidCategory(defaultCategory: String, newCategory: Category, oldCategory: Category? = null): Boolean {
            if (newCategory.name.isEmpty()
                || newCategory.name == defaultCategory
                || oldCategory?.name == defaultCategory
            )
                return false
            else if (oldCategory != null) {
                if (newCategory.name != oldCategory.name)
                    return true
            }
            return true
        }
    }
}