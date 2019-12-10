package com.yllxh.wordcollector.utils

import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.utils.DataUtils.DEFAULT_CATEGORY_NAME


fun isValidNewWord(newWord: Word, oldWord: Word): Boolean {
    return if (isWordEmpty(newWord))
        false
    else {
        areContentDifferent(
            newWord,
            oldWord
        )
    }
}

fun isValidWord(newWord: Word): Boolean {
    return !isWordEmpty(newWord)
}

private fun isWordEmpty(newWord: Word) =
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
