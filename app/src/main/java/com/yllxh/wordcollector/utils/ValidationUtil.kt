package com.yllxh.wordcollector.utils

import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.utils.DataUtils.DEFAULT_CATEGORY_NAME


fun isValidWord(newWord: Word, oldWord: Word): Boolean {
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

private fun areContentDifferent(newWord: Word, oldWord: Word): Boolean {
    return newWord != oldWord
}

private fun isWordEmpty(newWord: Word) =
    newWord.term.isEmpty() && newWord.definition.isEmpty()

fun isValidCategory(newCategory: Category, oldCategory: Category): Boolean {

    return if (isInvalidCategory(newCategory, oldCategory)) {
        false
    } else {
        newCategory.name != oldCategory.name
    }
}

fun isValidCategory(newCategory: Category): Boolean {
    return !(isInvalidCategory(newCategory))
}


private fun isInvalidCategory(newCategory: Category, oldCategory: Category? = null): Boolean {
    return newCategory.name.isEmpty()
            || newCategory.name == DEFAULT_CATEGORY_NAME
            || oldCategory?.name == DEFAULT_CATEGORY_NAME
}
