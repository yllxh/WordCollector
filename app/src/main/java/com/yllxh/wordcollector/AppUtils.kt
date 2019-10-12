package com.yllxh.wordcollector

import com.yllxh.wordcollector.data.Word

class AppUtils {

    companion object {

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
    }
}