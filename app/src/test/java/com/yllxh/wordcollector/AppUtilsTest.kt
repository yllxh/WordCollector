package com.yllxh.wordcollector

import com.yllxh.wordcollector.AppUtils.Companion.isValidWord
import com.yllxh.wordcollector.data.Word
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AppUtilsTest{
    private lateinit var wordWithEmptyTerm: Word
    private lateinit var wordWithEmptyDefinition: Word

    @Before
    fun initWords(){
        wordWithEmptyDefinition = Word("word", "")
        wordWithEmptyTerm = Word("","definition")
    }

    @Test
    fun wordIsValidWhenWordHasEmptyDefinitionOnly(){
        assertTrue(isValidWord(wordWithEmptyDefinition))
    }

    @Test
    fun wordIsValidWhenWordHasEmptyTermOnly(){
        assertTrue(isValidWord(wordWithEmptyTerm))
    }

    @Test
    fun returnsFalseWhenWordIsEmpty(){
        assertFalse(isValidWord(Word()))
    }

    @Test
    fun returnsFalseWhenComparingEmptyWordWithAnotherWord(){
        assertFalse(isValidWord(Word(), wordWithEmptyTerm))
    }

    @Test
    fun trueWhenComparingWordsWithDifferentContent(){
        assertTrue(isValidWord(wordWithEmptyTerm, wordWithEmptyDefinition))
    }

    @Test
    fun falseWhenComparingWordsThatAreTheSame(){
        assertFalse(isValidWord(wordWithEmptyTerm, wordWithEmptyTerm))
    }

}