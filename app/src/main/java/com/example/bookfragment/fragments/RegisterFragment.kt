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
import com.example.bookfragment.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    //authentification au firebase
    private lateinit var auth: FirebaseAuth
    //declare progress dialog
    private lateinit var progressDialog:ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Firebase Auth
        auth= FirebaseAuth.getInstance()
        // Initialize progress dialog
        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        //listener
        binding.btnBack.setOnClickListener {
            view.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        binding.btnRegister.setOnClickListener {
            validation()
        }
    }

    var name:String=""
    var email:String=""
    var password:String=""
    var confPassword:String=""
    private fun validation() {
        name=binding.edName.text.toString().trim()
        email=binding.edEmail.text.toString().trim()
        password=binding.edPass.text.toString().trim()
        confPassword=binding.edCpass.text.toString().trim()
        if(name.isEmpty())
            Toast.makeText(this.context,"Enter votre nom ..", Toast.LENGTH_LONG).show()
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            Toast.makeText(this.context,"Email Format Invalide ..", Toast.LENGTH_LONG).show()
        else if(password.isEmpty())
            Toast.makeText(this.context,"Enter votre Password ..", Toast.LENGTH_LONG).show()
        else if(confPassword.isEmpty())
            Toast.makeText(this.context,"Confirmer Password ..", Toast.LENGTH_LONG).show()
        else if(!password.equals(confPassword))
            Toast.makeText(this.context,"Incorrect Password ..", Toast.LENGTH_LONG).show()
        else{
            createAccount()
        }
    }

    private fun createAccount() {
        progressDialog.setTitle("Creating Account")
        progressDialog.show()
        auth.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Failed creating account due to ${e.message}",Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUserInfo() {
        progressDialog.setTitle("Saving user Info ...")
        //createAt & updateAt
        val timestamp=System.currentTimeMillis()
        //get current user via uid
        val uid=auth.uid
        val map:HashMap<String,Any?> = HashMap()
        map["uid"]=uid
        map["name"]=name
        map["email"]=email
        map["image"]=""
        map["userType"]="user"
        map["timestamp"]=timestamp

        //reference of root
        val ref=FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)//we use !! return nullpointerException if uid==null
            .setValue(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context,"Account created",Toast.LENGTH_LONG).show()
                view?.findNavController()?.navigate(R.id.action_registerFragment_to_loginFragment)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Faled saving user info due to ${e.message}",Toast.LENGTH_LONG).show()
            }
    }
}