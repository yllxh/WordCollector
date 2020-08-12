package com.yllxh.wordcollector.screens.managecategories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yllxh.wordcollector.AppRepository
import com.yllxh.wordcollector.utils.setLastSelectedCategory


class ManageCategoriesViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AppRepository(application)

    var categories = repository.categories
    val defaultCategory: String = repository.defaultCategory

    fun setCurrentCategory(name: String) {
        setLastSelectedCategory(getApplication(), name)
    }
}