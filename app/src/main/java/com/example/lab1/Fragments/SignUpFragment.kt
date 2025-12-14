package com.example.lab1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.Extentions.TempAuthData
import com.example.lab1.Extentions.User
import com.example.lab1.Extentions.UserRepository
import com.example.lab1.R
import com.example.lab1.databinding.ActivitySignupBinding

class SignUpFragment : BaseFragment() {

    private var _binding: ActivitySignupBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("binding is null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGoToSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_to_signIn)
        }

        binding.buttonReg.setOnClickListener {
            if (validateForm()) {
                val name = binding.name.text.toString().trim()
                val email = binding.login.text.toString().trim()
                val password = binding.password.text.toString().trim()

                val user = User(name, email, password)
                UserRepository.register(User(name, email, password))

                TempAuthData.lastRegisteredUser = user

                val action = SignUpFragmentDirections.actionSignUpToSignIn(user = user)

                findNavController().navigate(action)
            }
        }
    }

    private fun validateForm(): Boolean {
        val name = binding.name.text.toString().trim()
        val email = binding.login.text.toString().trim()
        val password = binding.password.text.toString().trim()

//        if (name.isEmpty()) {
//            binding.userNickname.error = "Обязательное поле"
//            return false
//        }
//        binding.userNickname.error = null
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            binding.userLogin.error = "Некорректный email"
//            return false
//        }
//        binding.userLogin.error = null
//
//        if (password.length < 6) {
//            binding.userPassword.error = "Минимум 6 символов"
//            return false
//        }
//        binding.userPassword.error = null

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//    private lateinit var userNickname: TextInputEditText
//    private lateinit var userLogin: TextInputEditText
//    private lateinit var userPassword: TextInputEditText
//    private lateinit var regButton: Button
//    private lateinit var tvGoToSignIn: TextView
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.activity_signup, container, false)
//
//        userNickname = view.findViewById(R.id.name)
//        userLogin = view.findViewById(R.id.login)
//        userPassword = view.findViewById(R.id.password)
//        regButton = view.findViewById(R.id.button_reg)
//        tvGoToSignIn = view.findViewById(R.id.tvGoToSignIn)
//
//        tvGoToSignIn.setOnClickListener {
//            (activity as MainActivity).navigateToSignIn()
//        }
//
//        regButton.setOnClickListener {
//            if (validateForm(view)) {
//                val name = userNickname.text.toString().trim()
//                val email = userLogin.text.toString().trim()
//                val password = userPassword.text.toString().trim()
//
//                val newUser = User(name, email, password)
//                UserRepository.register(newUser)
//
//                val signInFragment = SignInFragment().apply {
//                    arguments = bundleOf(
//                        "EXTRA_NAME" to name,
//                        "EXTRA_EMAIL" to email,
//                        "EXTRA_PASSWORD" to password,
//                        "EXTRA_USER" to User(name, email, password)
//                    )
//                }
//
//                (activity as MainActivity).navigateTo(signInFragment)
//            }
//        }
//
//        return view
//    }
//
//    private fun validateForm(view: View): Boolean {
//        val name = userNickname.text.toString().trim()
//        val email = userLogin.text.toString().trim()
//        val password = userPassword.text.toString().trim()
//
////        if (name.isEmpty()) {
////            (view.findViewById<TextInputLayout>(R.id.user_nickname)).error = "Обязательное поле"
////            return false
////        } else {
////            (view.findViewById<TextInputLayout>(R.id.user_nickname)).error = null
////        }
////
////        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
////            (view.findViewById<TextInputLayout>(R.id.user_login)).error = "Некорректный email"
////            return false
////        } else {
////            (view.findViewById<TextInputLayout>(R.id.user_login)).error = null
////        }
////
////        if (password.length < 6) {
////            (view.findViewById<TextInputLayout>(R.id.user_password)).error = "Минимум 6 символов"
////            return false
////        } else {
////            (view.findViewById<TextInputLayout>(R.id.user_password)).error = null
////        }
//
//        return true
//    }
//}