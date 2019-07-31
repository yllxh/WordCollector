package com.yllxh.wordcollector

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.yllxh.wordcollector.data.AppDatabase
import com.yllxh.wordcollector.data.Category
import com.yllxh.wordcollector.data.Word
import kotlinx.coroutines.*


class WordDisplayViewModel(application: Application) : AndroidViewModel(application) {
    val defaultCategory = application.getString(R.string.all)
    /**
     *  Used to indicate if a new word was added to the list, only used by the recycleView to check
     *  whether the list of items was changed because a new item was Inserted, if it is true the recycleView
     *  scroll to the top of the list.
     */
    var newItemInserted = false

    // Used to keep track of the current category
    var currentCategory: MutableLiveData<String> = MutableLiveData()

    private val db = AppDatabase.getInstance(application)

    private val viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
        currentCategory.value = defaultCategory
        coroutineScope.launch {
            insert(Category(defaultCategory))
        }
    }

    // The words that are displayed will depend
    // on the currentCategory which is selected by the user
    var words: LiveData<List<Word>> = Transformations.switchMap(currentCategory, Function {
        when (it) {
            defaultCategory -> db.wordDao.getAll()
            else -> db.wordDao.getWordsOfCategory(it)
        }
    })
    var categories = db.categoryDao.getAll()


    private var _saveNewWord = MutableLiveData<Boolean>()
    val saveNewWord: LiveData<Boolean>
        get() = _saveNewWord

    fun onSaveNewWord() {
        _saveNewWord.value = true
    }

    private fun onSaveNewWordCompleted() {
        _saveNewWord.value = false
        newItemInserted = true
    }

    private var _lookUpWord = MutableLiveData<Boolean>()
    val lookUpWord: MutableLiveData<Boolean>
        get() = _lookUpWord

    fun onLookUpWord() {
        _lookUpWord.value = true
    }
    fun onLookUpWordCompleted() {
        _lookUpWord.value = false
    }

    private fun checkWord(newWord: Word, oldWord: Word? = null): Boolean {
        if (newWord.word.isEmpty() && newWord.definition.isEmpty())
            return false
        else if (oldWord != null) {
            if (newWord.word != oldWord.word || newWord.definition != oldWord.definition)
                return true
        }
        return true
    }

    fun insertWordIfValid(word: Word): Boolean {
        if (checkWord(word)) {
            coroutineScope.launch {
                insert(word)
                onSaveNewWordCompleted()
            }
            return true
        }
        return false
    }

    private suspend fun insert(word: Word) {
        withContext(Dispatchers.IO) {
            db.wordDao.insert(word)
        }
    }

    /**
     * Updates a word in the database if it is a valid word,
     * it returns true if the word is updated and false otherwise.
     */
    fun updateWordIfValid(newWord: Word, oldWord: Word): Boolean {
        return when (checkWord(newWord, oldWord)) {
            true -> {
                newWord.id = oldWord.id
                coroutineScope.launch {
                    update(newWord)
                }
                true
            }
            else -> false
        }
    }

    private suspend fun update(word: Word) {
        withContext(Dispatchers.IO) {
            db.wordDao.update(word)
        }
    }

    fun deleteWord(word: Word) {
        coroutineScope.launch {
            delete(word)
        }
    }

    private suspend fun delete(word: Word) {
        withContext(Dispatchers.IO) {
            db.wordDao.delete(word)
        }
    }

    private suspend fun insert(category: Category) {
        withContext(Dispatchers.IO) {
            db.categoryDao.insert(category)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


}
