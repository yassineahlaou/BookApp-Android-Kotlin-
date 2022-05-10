package com.example.bookfragment.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.example.bookfragment.Constants
import com.example.bookfragment.MyApplication
import com.example.bookfragment.R
import com.example.bookfragment.databinding.DialogCommentAddBinding
import com.example.bookfragment.databinding.FragmentMoreDetailsPdfBinding
import com.example.bookfragment.AdapterComment
import com.example.myproject.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MoreDetailsPdfFragment : Fragment() {
    private companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }
    lateinit var BYTES:ByteArray
    val CREATE_FILE=0
    private lateinit var binding: FragmentMoreDetailsPdfBinding
    //declare progress dialog
    private lateinit var progressDialog: ProgressDialog
    private lateinit var commentArrayList : ArrayList<ModelComment>
    private lateinit var adapterComment : AdapterComment
    private lateinit var auth: FirebaseAuth
    //declare url and book id
    private var url=""
    private var bookId=""
    private var title = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentMoreDetailsPdfBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth =FirebaseAuth.getInstance()
        if (auth.currentUser != null)
        {
            checkIfReserved()
        }

// Initialize progress dialog to user in downloading button
        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.descriptionDet.text=arguments?.getString("bookDescription")
        binding.titleDet.text=arguments?.getString("bookTitle")
        binding.dateDet.text=arguments?.getString("bookdate")
        url=arguments?.getString("bookUrl")!!
        bookId=arguments?.getString("bookId")!!
        title = arguments?.getString("bookTitle")!!
        val categoryId=arguments?.getString("bookCategoryId")
        MyApplication.loadPdfSize(url, title , binding.sizeDet)
        MyApplication.loadPdfSinglePage(url ,title ,  binding.pdfViewer,binding.progressBar2,binding.pagesDet)
        MyApplication.loadCategory(categoryId!!,binding.categoryDet)
        //increment book views
        MyApplication.incrementBookView(bookId)
        //load views and download count
        loadRestBookInfo(bookId)
        showComments()
        //start pdfViewer activity when we click btn_read_book
        binding.btnReadBook.setOnClickListener {
            val bundle = bundleOf(
                "bookUrl" to url,
            )
            view.findNavController().navigate(R.id.action_moreDetailsPdfFragment_to_pdfViewerFragment,bundle)
        }
        //download book
        binding.btnDownload.setOnClickListener {
            //check if WRITE_EXTERNAL_STORAGE if garanted
            if(ContextCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                downloadBook()
            else
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        binding.addCommentBtn.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this.context, "Please Log in first", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }
        binding.btnReserve.setOnClickListener {
            if (auth.currentUser == null)
            {
                Toast.makeText(this.context, "Log in First", Toast.LENGTH_SHORT).show()
            }
            else{
                if (checkIfReserved() == true)
                {

                    this.context?.let { it1 -> MyApplication.removeFromReserved(it1, bookId) }
                }
                else{
                    addToReservered()
                }
            }


        }

    }



    private fun showComments() {
        //init arraylist
        commentArrayList =  ArrayList()
        //db path to load comments
        val ref = FirebaseDatabase.getInstance().getReference(  "Books")
        ref.child(bookId).child( "Comments")
            .addValueEventListener (object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        //get datas model,
                        val model = ds.getValue(ModelComment::class.java)
                        //add to List
                        commentArrayList.add(model!!)
                    }
                    //setup adapter
                    adapterComment = AdapterComment(context!!,commentArrayList)
                    //setup adapter to recyclerView
                    binding.commentsRv.adapter = adapterComment
                }

                //setup adapter
                override fun onCancelled(errer: DatabaseError) {

                }
            })

    }

    private var comment = ""
    private fun addCommentDialog() {
        //inflate/bind view for dialog dialog_comment_add.xml
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this.context))
        //setup alert dialog
        val builder = AlertDialog.Builder(this.context, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)
        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()
        //handle elick, dismiss dialog
        commentAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }
        //handte click, add comment
        commentAddBinding.submitBtn.setOnClickListener {
            //get data
            comment = commentAddBinding.commentET.text.toString().trim()
            //validate Data
            if (comment.isEmpty()) {
                Toast.makeText(this.context, "Write your comment", Toast.LENGTH_SHORT)
                    .show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGaranted->
        if(isGaranted)
            downloadBook()
        else
            Toast.makeText(this.context,"Permission denied", Toast.LENGTH_LONG).show()
    }

    private fun downloadBook() {
        progressDialog.setMessage("Starting chargement...")
        progressDialog.show()
        val storageReference= FirebaseStorage.getInstance().getReferenceFromUrl(url)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                BYTES=bytes
                progressDialog.dismiss()
                val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type="application/pdf"
                    putExtra(Intent.EXTRA_TITLE,arguments?.getString("bookTitle")+".pdf")
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI,"")
                }
                startActivityForResult(intent,CREATE_FILE)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this.context,"Failed to get book from storage due to ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CREATE_FILE && resultCode == AppCompatActivity.RESULT_OK){
            val uri=data!!.data
            try {
                val outputStream=this.requireActivity().contentResolver.openOutputStream(uri!!)
                outputStream?.write(BYTES)
                outputStream?.close()
                MyApplication.incrementDownloadCount(bookId)
                Toast.makeText(this.context,"Book downloaded successfufy",Toast.LENGTH_LONG).show()
            }
            catch(e:Exception){

            }
        }
    }
    private fun loadRestBookInfo(bookId: String) {
        val ref= FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val viewsCount=snapshot.child("viewCount").value.toString()
                    val downloadsCount=snapshot.child("downloadCount").value.toString()
                    //set data
                    binding.viewsDet.text=viewsCount
                    binding.downloadsDet.text=downloadsCount
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addComment() {
        //show progress
        progressDialog.setMessage("Adding Comment ......")
        progressDialog.show()
//timestamp for comment id, comment timestanp etc.
        val timestamp = "${System.currentTimeMillis()}"
//setup data to add in do for comment
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${auth.uid}"

//db path to add data into it
//Books > bookId > Comments > commentId > commentData
        val ref = FirebaseDatabase.getInstance().getReference("Books")

        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this.context, "Your comment is added !!!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this.context,
                    "Adding comment failed due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private var status = false

    private fun checkIfReserved() : Boolean {



        Log.d(TAG, "Checking if this book is reserved before")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!).child( "Reserved").child(bookId)
            .addValueEventListener (object : ValueEventListener{
                override fun onDataChange (snapshot: DataSnapshot) {
                    var isInReserved = snapshot.exists()
                    if (isInReserved ) {
                        //already reserved|
                        Log.d(TAG, "Availible in Reserved")
                        // set drawable top icon
                        binding.btnReserve.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_white,
                            0,
                            0
                        )
                        binding.btnReserve.text = "UnReserve"






                    } else {
                        //not availaible in reserved
                        Log.d(TAG, "Not in Reserved Books")
                        //set drawable top icon
                        binding.btnReserve.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_border_white,
                            0,
                            0
                        )
                        binding.btnReserve.text = "Reserve"




                    }
                    status = isInReserved

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        return status




    }
    private fun addToReservered(){

        Log.d(TAG, "Reserve: Adding to Reserverd")
        //val timestamp = System.currentTimeMillis()
        val timestamp = "${System.currentTimeMillis()}"
        //setup data to add in db
        val hashMap = HashMap<String, Any>()
        //hashMap["bookId"] =  bookId
        // hashMap["timestamp"] =  timestamp
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        //save to db
        val ref = FirebaseDatabase.getInstance().getReference( "Users")
        ref.child(auth.uid!!).child("Reserved" ).child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //added to Reserved
                Log.d(TAG, "Reserved!!!")
                Toast.makeText(
                    this.context,
                    "Added to reserved",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                //faited to reserve
                Log.d(
                    TAG,
                    "addToFavorite: Failed to add to fav due to ${e.message}"
                )

                Toast.makeText(
                    this.context,
                    "Failed to add to fav due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }


}