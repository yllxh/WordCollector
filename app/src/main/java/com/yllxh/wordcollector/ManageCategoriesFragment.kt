package com.yllxh.wordcollector


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yllxh.wordcollector.databinding.FragmentManageCategoriesBinding

class ManageCategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentManageCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }


}
