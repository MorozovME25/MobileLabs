//package com.example.lab1
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//
//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragmentContainer, OnboardFragment())
//                .commit()
//        }
//    }
//
//    // Методы навигации
//    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
//        val transaction = supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, fragment)
//        if (addToBackStack) transaction.addToBackStack(null)
//        transaction.commit()
//    }
//
//    fun navigateToOnboard() = navigateTo(OnboardFragment())
//    fun navigateToSignIn() = navigateTo(SignInFragment())
//    fun navigateToSignUp() = navigateTo(SignUpFragment())
//    fun navigateToHome() = navigateTo(HomeFragment(), addToBackStack = false) // не возвращаться в Home
//}