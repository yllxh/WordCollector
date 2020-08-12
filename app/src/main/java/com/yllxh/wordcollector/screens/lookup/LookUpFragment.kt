package com.yllxh.wordcollector.screens.lookup


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.yllxh.wordcollector.databinding.FragmentLookUpBinding

class LookUpFragment : Fragment() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentLookUpBinding.inflate(inflater, container, false)

        val url = LookUpFragmentArgs.fromBundle(
            requireArguments()
        ).url
        binding.webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl(url)
        }
        return binding.root
    }
}
