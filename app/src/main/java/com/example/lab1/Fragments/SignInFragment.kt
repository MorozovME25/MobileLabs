package com.example.lab1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.lab1.Extentions.BaseFragment
import com.example.lab1.Extentions.TempAuthData
import com.example.lab1.Extentions.UserRepository
import com.example.lab1.R
import com.example.lab1.databinding.ActivitySigninBinding

class SignInFragment : BaseFragment() {

    private var _binding: ActivitySigninBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("binding is null")

    private val args: SignInFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.user?.let { user ->
            Toast.makeText(
                requireContext(),
                "Регистрация успешна! Добро пожаловать, ${user.name} (${user.email})",
                Toast.LENGTH_LONG
            ).show()
            binding.login.setText(user.email)

            TempAuthData.lastRegisteredUser = null
        }

        if (binding.login.text.isNullOrBlank()) {
            TempAuthData.lastRegisteredUser?.let { user ->
                binding.login.setText(user.email)
            }
        }

        binding.tvGoToSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_signIn_to_signUp)
        }

        binding.buttonAuth.setOnClickListener {
            val email = binding.login.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (UserRepository.isValidCredentials(email, password)) {
//                TempAuthData.lastRegisteredUser = null
                findNavController().navigate(R.id.action_signIn_to_home)
            } else {
                if (!UserRepository.isEmailRegistered(email)) {
                    Toast.makeText(
                        requireContext(),
                        "Пользователь с таким email не зарегистрирован",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "Неверный пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//    lateinit var userLogin: TextInputEditText
//    lateinit var userPassword: TextInputEditText
//    private lateinit var tvGoToSignUp: TextView
//    private lateinit var tvRegisteredUser: TextView
//    private lateinit var authButton: Button
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        val view = inflater.inflate(R.layout.activity_signin, container, false)
//
//        userLogin = view.findViewById(R.id.login)
//        userPassword = view.findViewById(R.id.password)
//        tvGoToSignUp = view.findViewById(R.id.tvGoToSignUp)
//        authButton = view.findViewById(R.id.button_auth)
//        tvRegisteredUser = view.findViewById(R.id.tvRegisteredUser)
//
//        arguments?.let { bundle ->
//            bundle.getString("EXTRA_NAME")?.let { name ->
//                bundle.getString("EXTRA_EMAIL")?.let { email ->
//                    Toast.makeText(
//                        context,
//                        "Регистрация успешна! Добро пожаловать, $name ($email)",
//                        Toast.LENGTH_LONG
//                    ).show()
//
//                    userLogin.setText(email)
//                }
//            }
//        }
//
//        tvGoToSignUp.setOnClickListener {
//            (activity as MainActivity).navigateToSignUp()
//        }
//
//        authButton.setOnClickListener {
//            val email = userLogin.text.toString().trim()
//            val password = userPassword.text.toString().trim()
//
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (UserRepository.isValidCredentials(email, password)) {
//                (activity as MainActivity).navigateToHome()
//            } else {
//                if (!UserRepository.isEmailRegistered(email)) {
//                    Toast.makeText(
//                        context,
//                        "Пользователь с таким email не зарегистрирован",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    Toast.makeText(context, "Неверный пароль", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        return view
//    }
//}