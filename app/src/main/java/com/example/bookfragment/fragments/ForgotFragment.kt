package com.example.bookfragment.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentForgotBinding
import com.google.firebase.auth.FirebaseAuth


class ForgotFragment : Fragment() {
    private lateinit var binding : FragmentForgotBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var progressDialog : ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding  = FragmentForgotBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this.context)
        progressDialog.setTitle("Hold On")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.submitBtn.setOnClickListener {
            validateemail()
        }
        binding.backBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_forgotFragment_to_loginFragment)
        }

    }
    private var email = ""
    private fun validateemail() {
        //get data
        email = binding.emailEt.text.toString().trim()
        //validate data
        if (email.isEmpty()) {
            Toast.makeText(this.context, "Your email please...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches()) {
            Toast.makeText(this.context, "email false...", Toast.LENGTH_SHORT).show()
        }
        else {
            recoverPassword()
        }

    }

    private fun recoverPassword() {
        progressDialog.setMessage("find what you need to do in $email")
        progressDialog.show()
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "Instructions sent to \t$email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this.context, "Failure to send due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    }


