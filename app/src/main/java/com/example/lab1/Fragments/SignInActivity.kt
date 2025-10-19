package com.example.lab1.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lab1.Extentions.BaseActivity
import com.example.lab1.R
import com.google.android.material.textfield.TextInputEditText

class SignInActivity : BaseActivity() {

    lateinit var userLogin: TextInputEditText
    lateinit var userPassword: TextInputEditText
    private lateinit var tvGoToSignUp: TextView
    private lateinit var tvRegisteredUser: TextView


    private val signUpLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val name = data.getStringExtra("EXTRA_NAME") ?: ""
            val email = data.getStringExtra("EXTRA_EMAIL") ?: ""

            tvRegisteredUser.text = "Зарегистрирован: $name ($email)"
            tvRegisteredUser.visibility = View.VISIBLE

            userLogin.setText(email)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userLogin = findViewById(R.id.login)
        userPassword = findViewById(R.id.password)
        tvRegisteredUser = findViewById(R.id.tvRegisteredUser)
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp)
        val authButton: Button = findViewById(R.id.button_auth)

        tvGoToSignUp.setOnClickListener {
            signUpLauncher.launch(Intent(this, SignUpActivity::class.java))
        }


        authButton.setOnClickListener {

            val email = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, HomeActivity::class.java))
            finish()

        }
    }

//    private fun returnToActivity(){
//        val intent = Intent()
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