package com.example.bookfragment.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookfragment.PdfAdminAdapter
import com.example.bookfragment.PdfModelData
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminFragment : Fragment() {
    var categoryId=""
    var category=""
    //init arraylist of PdfModeleData to use it in adapter
    var arrayPdfData=ArrayList<PdfModelData>()
    //data filtrer
    var tempArrayPdfData=ArrayList<PdfModelData>()
    //adapter
    lateinit var adapter: PdfAdminAdapter
    //TAG
    val TAG="SEARCH_BOOK"
    private lateinit var binding: FragmentPdfListAdminBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentPdfListAdminBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //get data
        categoryId= arguments?.getString("categoryId").toString()
        category= arguments?.getString("category").toString()
        //set category title
        binding.Bcategory.text=category
        //back
        binding.btnBack2.setOnClickListener {
            view.findNavController().navigate(R.id.action_pdfListAdminFragment_to_dashboardAdminFragment)
        }
//listner to search editText
        binding.edSearchBook.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    tempArrayPdfData.clear()
                    val searchText=s.toString().uppercase()
                    if(searchText!=null && searchText.isNotEmpty()){
                        arrayPdfData.forEach {
                            if(it.title.uppercase().contains(searchText)){
                                tempArrayPdfData.add(it)
                            }
                        }
                    }
                    else{
                        tempArrayPdfData.clear()
                        tempArrayPdfData.addAll(arrayPdfData)
                    }
                    adapter.notifyDataSetChanged()
                }catch(e:Exception){
                    Log.d(TAG,"onTextChanged:Error due to ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }


        })
        //load pdf list
        loadPdf(this.context)
    }

    private fun loadPdf(context: Context?) {
        //acces to root
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        //we call a listener each time the data changed
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear arrayData befor remplir
                    arrayPdfData.clear()
                    snapshot.children.forEach {
                        val id=it.child("id").getValue(String::class.java)!!
                        val description=it.child("description").getValue(String::class.java)!!
                        val title=it.child("title").getValue(String::class.java)!!
                        val url=it.child("url").getValue(String::class.java)!!
                        val timestamp=it.child("timestamp").getValue(Long::class.java)!!
                        val uid=it.child("uid").getValue(String::class.java)!!
                        val viewCount=it.child("viewCount").getValue(Long::class.java)!!
                        val downloadCount=it.child("downloadCount").getValue(Long::class.java)!!

                        val pdfData=PdfModelData(uid,id,title,description,categoryId,url,timestamp,viewCount,downloadCount,false)
                        arrayPdfData.add(pdfData)
                    }
                    tempArrayPdfData.clear()
                    tempArrayPdfData.addAll(arrayPdfData)
                    binding.rvBooks.layoutManager= LinearLayoutManager(context)
                    adapter= PdfAdminAdapter(tempArrayPdfData)
                    binding.rvBooks.adapter=adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}