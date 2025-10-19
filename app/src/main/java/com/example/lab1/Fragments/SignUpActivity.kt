package com.example.lab1.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab1.Extentions.BaseActivity
import com.example.lab1.Extentions.User
import com.example.lab1.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : BaseActivity() {

    lateinit var userNickname: TextInputEditText
    lateinit var userLogin: TextInputEditText
    lateinit var userPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userNickname = findViewById(R.id.name)
        userLogin = findViewById(R.id.login)
        userPassword = findViewById(R.id.password)


        val regButton: Button = findViewById(R.id.button_reg)

        regButton.setOnClickListener {
            if (validateForm()) {
                val name = userNickname.text.toString().trim()
                val email = userLogin.text.toString().trim()
                val password = userPassword.text.toString().trim()

                val resultIntent = Intent().apply {
                    putExtra("EXTRA_NAME", name)
                    putExtra("EXTRA_EMAIL", email)
                    putExtra("EXTRA_PASSWORD", password)
                    putExtra("EXTRA_USER", User(name, email, password))
                }

                setResult(RESULT_OK, resultIntent)
                finish()
            }
//            val nickname = userNickname.text.toString().trim()
//            val login = userLogin.text.toString().trim()
//            val password = userPassword.text.toString().trim()
//
//            if (nickname == "" || login == "" || password == "")
//                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
//            else {
//
//            }
//            returnToActivity()
        }
    }

    private fun validateForm(): Boolean {
        val name = userNickname.text.toString().trim()
        val email = userLogin.text.toString().trim()
        val password = userPassword.text.toString().trim()

        if (name.isEmpty()) {
            (findViewById<TextInputLayout>(R.id.user_nickname)).error = "Обязательное поле"
            return false
        } else {
            (findViewById<TextInputLayout>(R.id.user_nickname)).error = null
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            (findViewById<TextInputLayout>(R.id.user_login)).error = "Некорректный email"
            return false
        } else {
            (findViewById<TextInputLayout>(R.id.user_login)).error = null
        }

        if (password.length < 6) {
            (findViewById<TextInputLayout>(R.id.user_password)).error = "Минимум 6 символов"
            return false
        } else {
            (findViewById<TextInputLayout>(R.id.user_password)).error = null
        }

        return true

    }
//    private fun returnToActivity(){
//        val intent = Intent()
//
//        if (!userNickname.text.isNullOrBlank()) {
//            intent.putExtra(StartActivity.LOGIN, userNickname.text.toString())
//        } else {
//            Snackbar.make(findViewById(R.id.main), "Enter name", Snackbar.LENGTH_LONG).show()
//            return
//        }
//
//        if (userLogin.text.isNullOrBlank()) {
//            intent.putExtra(StartActivity.GENDER, userLogin.text.toString())
//        } else {
//            Snackbar.make(findViewById(R.id.main), "Choose gender", Snackbar.LENGTH_LONG).show()
//            return
//        }
//
//        if (!userPassword.text.isNullOrBlank()) {
//            intent.putExtra(StartActivity.LOGIN, userPassword.text.toString())
//        } else {
//            Snackbar.make(findViewById(R.id.main), "Enter name", Snackbar.LENGTH_LONG).show()
//            return
//        }
//
//        setResult(RESULT_OK, intent)
//        finish()
//    }
}