package com.example.bookfragment.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.bookfragment.PdfModelData
import com.example.bookfragment.databinding.FragmentBooksUserBinding
import com.example.bookfragment.AdapterPdfUser
import com.example.bookfragment.CategoryData
import com.example.bookfragment.PdfAdminAdapter
import com.example.myproject.CategoryAdapter

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception


class BooksUserFragment : Fragment {



    //view binding fragment books.user.xmt > FragnentBooksUserBinding
    private lateinit var binding: FragmentBooksUserBinding
    public companion object{
        private const val TAG =  "B0OKS_USER_TAG"
        //receive date fron activity to leod books e.g. categoryId, category, uid
        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment{
            val fragment = BooksUserFragment ()
            //put data to bundle intent
            val args =  Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }}
    private var categoryId =""
    private var category = ""
    private var uid = ""
    //arraylist to hold pdfs
    private lateinit var pdfArrayList: ArrayList<PdfModelData>
    private lateinit var adapterPdfUser: AdapterPdfUser
    var arrayData=ArrayList<PdfModelData>()
    //data filtrer
    var tempArrayData=ArrayList<PdfModelData>()
    lateinit var adapter: AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        //get arguments that we passed in newInstance method
        val args = arguments
        if (args != null)
        {
            categoryId = args.getString("categoryId")!!
            category  = args.getString ("category")!!
            uid = args.getString("uid")!!
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)
        //load pdf according to category, this fragnent witl have new instance to tead each category pafs
        Log.d(TAG, "onCreateView: Category: $category")
        if (category == "All") {
            //load all books
            loadAllBooks()
        }
        else if (category ==  "Most Viewed") {
            //load  most viewed books
            loadMostViewedDownloadedBooks("viewsCount")
        }
        else if (category == "Most Downloaded") {
            //load most downLoaded books
            loadMostViewedDownloadedBooks("downloadsCount")
        }
        else {
            //load selected category books
            loadCategorizedBooks()
        }

        //search
        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    tempArrayData.clear()
                    val searchText=s.toString().uppercase()
                    if(searchText!=null && searchText.isNotEmpty()){
                        arrayData.forEach {
                            if(it.title.uppercase().contains(searchText)){
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


       /* binding.searchEt.addTextChangedListener { object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterPdfUser.filter.filter(s)
                }
                catch (e : Exception)
                {
                    Log.d(TAG , "onTextChanged : SEARCH EXCEPTION :${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        }
        }*/

        return binding.root
    }
    private fun loadAllBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear List before starting adding data into it
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(PdfModelData::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                //set adapter to recycterview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)//load 10 most viewd or downloadded
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear List before starting adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(PdfModelData::class.java)
                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    //set adapter to recycterview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }




    private fun loadCategorizedBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear List before starting adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(PdfModelData::class.java)
                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterPdfUser = AdapterPdfUser(context!!, pdfArrayList)
                    //set adapter to recycterview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}

