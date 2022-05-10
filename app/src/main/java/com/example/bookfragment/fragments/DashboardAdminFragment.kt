package com.example.bookfragment.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentDashboardAdminBinding
import com.example.myproject.CategoryAdapter
import com.example.bookfragment.CategoryData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminFragment : Fragment() {
    //init arraylist of categoryData to use it in adapter
    var arrayData=ArrayList<CategoryData>()
    //data filtrer
    var tempArrayData=ArrayList<CategoryData>()
    private lateinit var binding: FragmentDashboardAdminBinding
    private lateinit var auth: FirebaseAuth
    //adapter
    lateinit var adapter:CategoryAdapter

     override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding= FragmentDashboardAdminBinding.inflate(inflater,container,false)
         return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        checkUser()
        loadCategories(this.context)
        binding.btnLogoutAdmin.setOnClickListener {
            auth.signOut()
            checkUser()
        }
        //more to add category activity
        binding.btnMoveToAddCategory.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_dashboardAdminFragment_to_categoryAddFragment)
        }
        //start fragment which used to add book
        binding.pdfAddFrag.setOnClickListener {
            view.findNavController().navigate(R.id.action_dashboardAdminFragment_to_bookAddFragment)
        }
        //search
        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    tempArrayData.clear()
                    val searchText=s.toString().uppercase()
                    if(searchText!=null && searchText.isNotEmpty()){
                        arrayData.forEach {
                            if(it.category.uppercase().contains(searchText)){
                                tempArrayData.add(it)
                            }
                        }
                    }
                    else{
                        tempArrayData.clear()
                        tempArrayData.addAll(arrayData)
                    }
                    adapter.notifyDataSetChanged()
                }catch(e:Exception){

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun loadCategories(context: Context?) {
//acces to root
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        //we call a listener each time the data changed
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear arrayData befor remplir
                arrayData.clear()
                snapshot.children.forEach {
                    val id=it.child("id").getValue(String::class.java)!!
                    val category=it.child("category").getValue(String::class.java)!!
                    val timestamp=it.child("timestamp").getValue(Long::class.java)!!
                    val uid=it.child("uid").getValue(String::class.java)!!
                    val categoryData= CategoryData(id,category,timestamp,uid)
                    arrayData.add(categoryData)
                }
                tempArrayData.clear()
                tempArrayData.addAll(arrayData)
                binding.rv.layoutManager= LinearLayoutManager(context)
                adapter= CategoryAdapter(tempArrayData)
                binding.rv.adapter=adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkUser() {
        val user=auth.currentUser
        if(user==null){
            view?.findNavController()?.navigate(R.id.action_dashboardAdminFragment_to_betweenFragment)
        }
        else{
            val email=user.email
            binding.adminEmail.text=email
        }
    }
}