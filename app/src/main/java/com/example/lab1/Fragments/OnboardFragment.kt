package com.example.lab1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.MainActivity

import com.example.lab1.R

class OnboardFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_onboard, container, false)

        view.findViewById<Button>(R.id.btnSignIn).setOnClickListener {
            (activity as MainActivity).navigateToSignInFromOnboard()
        }

        view.findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            (activity as MainActivity).navigateToSignUpFromOnboard()
        }

        return view
    }
}