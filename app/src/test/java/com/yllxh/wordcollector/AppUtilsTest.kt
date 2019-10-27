package com.yllxh.wordcollector

import com.yllxh.wordcollector.AppUtils.Companion.isValidWord
import com.yllxh.wordcollector.data.Word
import org.junit.Assert.*
import org.junit.Test

class AppUtilsTest{
    private lateinit var wordWithEmptyTerm: Word
    private lateinit var wordWithEmptyDefinition: Word


    @Test
    fun wordIsValidWhenWordHasEmptyDefinitionOnly(){
        wordWithEmptyDefinition = Word("word", "")
        assertTrue(isValidWord(wordWithEmptyDefinition))
    }

    @Test
    fun wordIsValidWhenWordHasEmptyTermOnly(){
        wordWithEmptyTerm = Word("","definition")
        assertTrue(isValidWord(wordWithEmptyTerm))
    }

    @Test
    fun returnsFalseWhenWordIsEmpty(){
        assertFalse(isValidWord(Word()))
    }
}