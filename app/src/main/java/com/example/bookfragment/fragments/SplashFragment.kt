package com.example.bookfragment.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.bookfragment.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashFragment : Fragment() {
    //authentification au firebase
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_splash, container, false)
        auth= FirebaseAuth.getInstance()
        Handler().postDelayed(Runnable {
            checkUser()
        }, 2000)
        return view
    }

    private fun checkUser(){
        val user=auth.currentUser
        if(user==null){
            view?.findNavController()?.navigate(R.id.action_splashFragment_to_betweenFragment)
        }
        else{
            val ref= FirebaseDatabase.getInstance().getReference("Users")
            ref.child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userType=snapshot.child("userType").value
                        if(userType=="user"){
                            view?.findNavController()?.navigate(R.id.action_splashFragment_to_dashboardUserFragment)

                        }
                        else if(userType=="admin"){
                            view?.findNavController()?.navigate(R.id.action_splashFragment_to_dashboardAdminFragment)

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}