package com.example.bookfragment.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentBetweenBinding
import com.example.bookfragment.databinding.FragmentLoginBinding
import com.example.bookfragment.databinding.FragmentRegisterBinding

class BetweenFragment : Fragment() {
    private lateinit var binding: FragmentBetweenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBetweenBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            view.findNavController().navigate(R.id.action_betweenFragment_to_loginFragment)
        }
        binding.btnNext.setOnClickListener {
            view.findNavController().navigate(R.id.action_betweenFragment_to_dashboardUserFragment)
        }
    }
}