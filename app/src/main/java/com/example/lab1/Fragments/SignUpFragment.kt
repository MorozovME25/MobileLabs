package com.example.lab1.Fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.Extentions.User
import com.example.lab1.Extentions.UserRepository
import com.example.lab1.MainActivity
import com.example.lab1.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpFragment : BaseFragment() {

    private lateinit var userNickname: TextInputEditText
    private lateinit var userLogin: TextInputEditText
    private lateinit var userPassword: TextInputEditText
    private lateinit var regButton: Button
    private lateinit var tvGoToSignIn: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_signup, container, false)

        userNickname = view.findViewById(R.id.name)
        userLogin = view.findViewById(R.id.login)
        userPassword = view.findViewById(R.id.password)
        regButton = view.findViewById(R.id.button_reg)
        tvGoToSignIn = view.findViewById(R.id.tvGoToSignIn)

        tvGoToSignIn.setOnClickListener {
            (activity as MainActivity).navigateToSignIn()
        }

        regButton.setOnClickListener {
            if (validateForm(view)) {
                val name = userNickname.text.toString().trim()
                val email = userLogin.text.toString().trim()
                val password = userPassword.text.toString().trim()

                val newUser = User(name, email, password)
                UserRepository.register(newUser)

                val signInFragment = SignInFragment().apply {
                    arguments = bundleOf(
                        "EXTRA_NAME" to name,
                        "EXTRA_EMAIL" to email,
                        "EXTRA_PASSWORD" to password,
                        "EXTRA_USER" to User(name, email, password)
                    )
                }

                (activity as MainActivity).navigateTo(signInFragment)
            }
        }

        return view
    }

    private fun validateForm(view: View): Boolean {
        val name = userNickname.text.toString().trim()
        val email = userLogin.text.toString().trim()
        val password = userPassword.text.toString().trim()

//        if (name.isEmpty()) {
//            (view.findViewById<TextInputLayout>(R.id.user_nickname)).error = "Обязательное поле"
//            return false
//        } else {
//            (view.findViewById<TextInputLayout>(R.id.user_nickname)).error = null
//        }
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            (view.findViewById<TextInputLayout>(R.id.user_login)).error = "Некорректный email"
//            return false
//        } else {
//            (view.findViewById<TextInputLayout>(R.id.user_login)).error = null
//        }
//
//        if (password.length < 6) {
//            (view.findViewById<TextInputLayout>(R.id.user_password)).error = "Минимум 6 символов"
//            return false
//        } else {
//            (view.findViewById<TextInputLayout>(R.id.user_password)).error = null
//        }

        return true
    }
}