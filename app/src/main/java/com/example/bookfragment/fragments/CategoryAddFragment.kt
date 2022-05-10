package com.example.bookfragment.fragments

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentCategoryAddBinding
import com.example.bookfragment.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddFragment : Fragment() {
    private lateinit var binding: FragmentCategoryAddBinding
    //authentification au firebase
    private lateinit var auth: FirebaseAuth
    //declare progress dialog
    private lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCategoryAddBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        // Initialize progress dialog
        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        binding.btnBack2.setOnClickListener {
            view.findNavController().navigate(R.id.action_categoryAddFragment_to_dashboardAdminFragment)
        }
        binding.btnAddCategorie.setOnClickListener {
            validation()
        }
    }
    private var category=""
    private fun validation() {
        category=binding.edTitle.text.toString().trim()
        if(category.isEmpty())
            Toast.makeText(this.context,"Enter Category ..", Toast.LENGTH_LONG).show()
        else
            addCategorieToFirebase()
    }

    private fun addCategorieToFirebase() {
        progressDialog.show()
        val timestamp=System.currentTimeMillis()
        val map=HashMap<String,Any>()
        map["id"]=timestamp.toString()
        map["category"]=category
        map["timestamp"]=timestamp
        map["uid"]=auth.uid.toString()
        //reference of root
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(timestamp.toString())
            .setValue(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.edTitle.text.clear()
                Toast.makeText(this.context,"Added successfully ...",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Failed to add due to ${e.message}",Toast.LENGTH_LONG).show()
            }
    }
}