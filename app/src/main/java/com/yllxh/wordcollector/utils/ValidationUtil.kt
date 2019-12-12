package com.yllxh.wordcollector.utils

import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.DEFAULT_CATEGORY_NAME
import com.yllxh.wordcollector.data.Word


fun isValidNewWord(newWord: Word, oldWord: Word): Boolean {
    return if (isContentEmpty(newWord))
        false
    else {
        areContentDifferent(
            newWord,
            oldWord
        )
    }
}

fun isValidWord(newWord: Word): Boolean {
    return !isContentEmpty(newWord)
}

private fun isContentEmpty(newWord: Word) =
    newWord.word.isEmpty() && newWord.definition.isEmpty()

private fun areContentDifferent(newWord: Word, oldWord: Word): Boolean {
    return newWord != oldWord
}

fun isValidNewCategory(newCategory: Category, oldCategory: Category): Boolean {

    return if (isInvalidNewCategory(newCategory, oldCategory)) {
        false
    } else {
        newCategory.name != oldCategory.name
    }
}

fun isValidCategory(newCategory: Category): Boolean {
    return !(isInvalidNewCategory(newCategory))
}


private fun isInvalidNewCategory(newCategory: Category, oldCategory: Category? = null): Boolean {
    return newCategory.name.isEmpty()
            || newCategory.name == DEFAULT_CATEGORY_NAME
            || oldCategory?.name == DEFAULT_CATEGORY_NAME
}
