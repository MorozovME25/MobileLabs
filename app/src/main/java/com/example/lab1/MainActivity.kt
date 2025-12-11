package com.example.lab1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lab1.Extentions.User
import com.example.lab1.Extentions.UserRepository
import com.example.lab1.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (UserRepository.isEmailRegistered("test@example.com").not()) {
            UserRepository.register(User("Тест", "test@example.com", "password"))
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragmentContainer, OnboardFragment())
//                .commit()
//        }
//
//        onBackPressedDispatcher.addCallback(this) {
//            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
//
//            when (currentFragment) {
//                is HomeFragment -> {
//                    navigateToOnboard()
//                }
//                is OnboardFragment -> {
//                    finish()
//                }
//                else -> {
//                    if (supportFragmentManager.backStackEntryCount > 0) {
//                        supportFragmentManager.popBackStack()
//                    } else {
//                        navigateToOnboard()
//                    }
//                }
//            }
//        }
//    }
//
//    fun navigateTo(fragment: Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, fragment)
//            .commit()
//    }
//
//    fun navigateToOnboard() {
//        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, OnboardFragment())
//            .commit()
//    }
//
//    fun navigateToSignInFromOnboard() {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, SignInFragment())
//            .addToBackStack(null)
//            .commit()
//    }
//
//    fun navigateToSignUpFromOnboard() {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, SignUpFragment())
//            .addToBackStack(null)
//            .commit()
//    }
//
//    fun navigateToSignIn() {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, SignInFragment())
//            .commit()
//    }
//
//    fun navigateToSignUp() {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, SignUpFragment())
//            .commit()
//    }
//
//    fun navigateToHome() {
//        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, HomeFragment())
//            .addToBackStack(null)
//            .commit()
//    }
}