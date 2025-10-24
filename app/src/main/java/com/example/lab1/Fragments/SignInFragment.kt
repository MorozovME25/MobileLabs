package com.example.lab1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.Extentions.UserRepository
import com.example.lab1.MainActivity
import com.example.lab1.R
import com.google.android.material.textfield.TextInputEditText

class SignInFragment : BaseFragment() {

    lateinit var userLogin: TextInputEditText
    lateinit var userPassword: TextInputEditText
    private lateinit var tvGoToSignUp: TextView
    private lateinit var tvRegisteredUser: TextView
    private lateinit var authButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_signin, container, false)

        userLogin = view.findViewById(R.id.login)
        userPassword = view.findViewById(R.id.password)
        tvGoToSignUp = view.findViewById(R.id.tvGoToSignUp)
        authButton = view.findViewById(R.id.button_auth)
        tvRegisteredUser = view.findViewById(R.id.tvRegisteredUser)

        arguments?.let { bundle ->
            bundle.getString("EXTRA_NAME")?.let { name ->
                bundle.getString("EXTRA_EMAIL")?.let { email ->
                    Toast.makeText(
                        context,
                        "Регистрация успешна! Добро пожаловать, $name ($email)",
                        Toast.LENGTH_LONG
                    ).show()

                    userLogin.setText(email)
                }
            }
        }

        tvGoToSignUp.setOnClickListener {
            (activity as MainActivity).navigateToSignUp()
        }

        authButton.setOnClickListener {
            val email = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (UserRepository.isValidCredentials(email, password)) {
                (activity as MainActivity).navigateToHome()
            } else {
                if (!UserRepository.isEmailRegistered(email)) {
                    Toast.makeText(
                        context,
                        "Пользователь с таким email не зарегистрирован",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, "Неверный пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}