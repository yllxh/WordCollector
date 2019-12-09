package com.yllxh.wordcollector

import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.Word
import com.yllxh.wordcollector.utils.DataUtils.DEFAULT_CATEGORY_NAME
import com.yllxh.wordcollector.utils.isValidCategory
import com.yllxh.wordcollector.utils.isValidWord
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ValidationUtilTest{
    private lateinit var nonEmptyWord: Word
    private lateinit var wordWithEmptyTerm: Word
    private lateinit var wordWithEmptyDefinition: Word
    private lateinit var defaultCategory: Category
    private lateinit var validCategory: Category



    @Before
    fun initCategories(){
        defaultCategory = Category(DEFAULT_CATEGORY_NAME)
        validCategory = Category("anyName")
    }

    @Before
    fun initWords(){
        wordWithEmptyDefinition = Word("word", "")
        wordWithEmptyTerm = Word("","definition")
        nonEmptyWord = Word("word","definition")
    }

    @Test
    fun validWord_WhenOnlyDefinitionIsEmpty(){
        assertTrue(isValidWord(wordWithEmptyDefinition))
    }

    @Test
    fun validWord_WhenOnlyTermIsEmpty(){
        assertTrue(isValidWord(wordWithEmptyTerm))
    }

    @Test
    fun notValidWord_WhenTermAndDefinitionAreEmpty(){
        assertFalse(isValidWord(Word()))
    }

    @Test
    fun notValidWord_WhenComparingWithEmptyWord(){
        assertFalse(isValidWord(Word(), nonEmptyWord))
    }

    @Test
    fun validWord_WhenComparingDifferentWords(){
        assertTrue(isValidWord(wordWithEmptyTerm, wordWithEmptyDefinition))
    }

    @Test
    fun notValidWord_WhenComparingWordWithSameContent(){
        assertFalse(isValidWord(wordWithEmptyTerm, wordWithEmptyTerm))
    }

    @Test
    fun notValidCategory_WhenNameIsEmpty(){
        assertFalse(isValidCategory(Category("")))
    }

    @Test
    fun notValidCategory_WhenNameEqualsDefaultCategoryName(){
        assertFalse(isValidCategory(defaultCategory))
    }

    @Test
    fun validCategory_WhenNameNotEqualToDefaultCategoryName(){
        assertTrue(isValidCategory(validCategory))
    }

    @Test
    fun notValidCategory_WhenOneHasNameEqualDefaultCategoryName(){
        assertFalse(isValidCategory(defaultCategory, defaultCategory))
    }

}