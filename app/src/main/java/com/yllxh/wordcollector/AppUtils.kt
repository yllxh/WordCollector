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

        private fun areContentDifferent(newWord: Word, oldWord: Word): Boolean {
            return newWord.word != oldWord.word
                    || newWord.definition != oldWord.definition
        }

        private fun isWordEmpty(newWord: Word) =
            newWord.word.isEmpty() && newWord.definition.isEmpty()

        fun isValidCategory(defaultCategory: String, newCategory: Category, oldCategory: Category): Boolean {
            return if (isInvalidCategory(defaultCategory, newCategory, oldCategory))
                false
            else {
                newCategory.name != oldCategory.name
            }
        }

        fun isValidCategory(defaultCategory: String, newCategory: Category): Boolean{
            return !(isInvalidCategory(defaultCategory, newCategory))
        }


        private fun isInvalidCategory(
            defaultCategory: String,
            newCategory: Category,
            oldCategory: Category? = null
        ): Boolean {
            return newCategory.name.isEmpty()
                    || newCategory.name == defaultCategory
                    || oldCategory?.name == defaultCategory
        }
    }
}