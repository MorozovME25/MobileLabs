package com.example.lab1.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.lab1.Extentions.BaseFragment

import com.example.lab1.R
import com.example.lab1.databinding.ActivityOnboardBinding

class OnboardFragment : BaseFragment() {

    private var _binding: ActivityOnboardBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("binding is null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityOnboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_onboard_to_signIn)
        }
        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_onboard_to_signUp)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}