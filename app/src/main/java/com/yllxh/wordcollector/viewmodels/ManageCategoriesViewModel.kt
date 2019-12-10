package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.utils.setLastSelectedCategory


class ManageCategoriesViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AppRepository(application)

    var categories = repository.categories

    fun setCurrentCategory(name: String) {
        setLastSelectedCategory(getApplication(), name)
    }
}