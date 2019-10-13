package com.yllxh.wordcollector.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yllxh.wordcollector.AppPreferences
import com.yllxh.wordcollector.AppRepository


class ManageCategoriesViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = AppRepository(application)

    var categories = repository.categories

    fun setCurrentCategory(name: String) {
        AppPreferences.setLastSelectedCategory(getApplication(), name)
    }
}