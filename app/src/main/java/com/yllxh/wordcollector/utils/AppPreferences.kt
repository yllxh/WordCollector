package com.yllxh.wordcollector.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.utils.DataUtils.DEFAULT_CATEGORY_NAME


private const val DAY_NIGHT_KEY = "dayNightKey"
private const val LAST_SELECTED_CATEGORY = "lastSelectedCategory"



/*
 * Returns the nightMode which was last saved in preferences.
 *
 * If there is no mode saved, then it will save and return
 * the defaultMode, which is saved in the apps resource file.
 *
 */
fun getNightMode(context: Context): Boolean {
    val defaultMode = context.resources.getBoolean(R.bool.default_night_mode)
    PreferenceManager.getDefaultSharedPreferences(context).apply {

        return if (contains(DAY_NIGHT_KEY)) {
            getBoolean(DAY_NIGHT_KEY, defaultMode)
        } else {
            edit().apply {
                putBoolean(DAY_NIGHT_KEY, defaultMode)
                apply()
            }
            true
        }
    }
}

fun setDayNightMode(context: Context, mode: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).apply {
        edit().apply {
            putBoolean(DAY_NIGHT_KEY, mode)
            apply()
        }
    }
}


/*
 * Returns the name of the last saved selected category.
 *
 * If there is no category name is saved, then it will save and return
 * the defaultCategoryName, which is saved in the apps resource file.
 */
fun getLastSelectedCategory(context: Context): String {

    PreferenceManager.getDefaultSharedPreferences(context).apply {

        return if (contains(LAST_SELECTED_CATEGORY)) {
            getString(LAST_SELECTED_CATEGORY, DEFAULT_CATEGORY_NAME) ?: DEFAULT_CATEGORY_NAME
        } else {
            edit().apply {
                putString(LAST_SELECTED_CATEGORY, DEFAULT_CATEGORY_NAME)
                apply()
            }
            DEFAULT_CATEGORY_NAME
        }
    }
}

fun setLastSelectedCategory(context: Context, name: String) {
    PreferenceManager.getDefaultSharedPreferences(context).apply {
        edit().apply {
            putString(LAST_SELECTED_CATEGORY, name)
            apply()
        }
    }

}
