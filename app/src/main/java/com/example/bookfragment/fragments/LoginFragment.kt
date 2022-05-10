package com.example.bookfragment.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentLoginBinding
import com.example.bookfragment.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    //authentification au firebase
    private lateinit var auth: FirebaseAuth
    //declare progress dialog
    private lateinit var progressDialog: ProgressDialog
    private var email=""
    private var password=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        // Initialize progress dialog
        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.tvWithoutAcc.setOnClickListener {
            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment2)
        }
        binding.btnLog.setOnClickListener {
            validation()
        }
        binding.forget.setOnClickListener {
            view.findNavController().navigate(R.id.action_loginFragment_to_forgotFragment)
        }
    }

    private fun validation() {
        email=binding.edMail.text.toString().trim()
        password=binding.edPassword.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            Toast.makeText(this.context,"Email Format Invalide ..", Toast.LENGTH_LONG).show()
        else if(password.isEmpty())
            Toast.makeText(this.context,"Enter votre Password ..", Toast.LENGTH_LONG).show()
        else
            loginUser()
    }

    private fun loginUser() {
        progressDialog.setTitle("logging In...")
        progressDialog.show()
        auth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"login failed due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkUser() {
        progressDialog.setTitle("Checking user ...")
        val user=auth.currentUser!!
        val ref= FirebaseDatabase.getInstance().getReference("Users")
        ref.child(user.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()
                    val userType=snapshot.child("userType").value
                    if(userType=="user"){
                        view?.findNavController()?.navigate(R.id.action_loginFragment_to_dashboardUserFragment)
                    }
                    else if(userType=="admin"){
                        view?.findNavController()?.navigate(R.id.action_loginFragment_to_dashboardAdminFragment)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }

            )
    }
}