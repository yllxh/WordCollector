package com.yllxh.wordcollector.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yllxh.wordcollector.R
import com.yllxh.wordcollector.fragments.WordDisplayFragmentDirections

fun Fragment.lookUpWord(wordStr: String){
    val url = getString(R.string.google_translate_site) + wordStr
    findNavController().navigate(
        WordDisplayFragmentDirections.actionWordDisplayFragmentToLookUpFragment(url)
    )

}