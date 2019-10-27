package com.yllxh.wordcollector

import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.Word

class AppUtils {

    companion object {
        const val DATABASE_NAME = "app_database"


        fun isValidWord(newWord: Word, oldWord: Word): Boolean {
            return if (isWordEmpty(newWord))
                false
            else {
                areContentDifferent(newWord, oldWord)
            }
        }

        fun isValidWord(newWord: Word):Boolean{
            return !isWordEmpty(newWord)
        }
        private fun areContentDifferent(
            newWord: Word,
            oldWord: Word
        ) = newWord.word != oldWord.word || newWord.definition != oldWord.definition

        private fun isWordEmpty(newWord: Word) =
            newWord.word.isEmpty() && newWord.definition.isEmpty()

        /**
         * Used to check if a category is valid to perform operations with it.
         * oldCategory can be used in cases where the newCategory needs to be compared
         * with it.
         */
        fun isValidCategory(defaultCategory: String, newCategory: Category, oldCategory: Category? = null): Boolean {
            if (isInvalidCategory(newCategory, defaultCategory, oldCategory))
                return false
            else if (oldCategory != null) {
                if (newCategory.name != oldCategory.name)
                    return true
            }
            return true
        }

        private fun isInvalidCategory(
            newCategory: Category,
            defaultCategory: String,
            oldCategory: Category?
        ): Boolean {
            return newCategory.name.isEmpty()
                    || newCategory.name == defaultCategory
                    || oldCategory?.name == defaultCategory
        }
    }
}