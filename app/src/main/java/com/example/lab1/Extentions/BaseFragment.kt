package com.example.lab1.Extentions

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    private val teg: String
        get() = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(teg, "onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d(teg, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(teg, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(teg, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(teg, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(teg, "onDestroy")
    }
}