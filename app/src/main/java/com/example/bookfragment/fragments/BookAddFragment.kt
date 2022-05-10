package com.example.bookfragment.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentBookAddBinding
import com.example.bookfragment.CategoryData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class BookAddFragment : Fragment() {
    private lateinit var binding: FragmentBookAddBinding
    //authentification au firebase
    private lateinit var auth: FirebaseAuth
    //declare progress dialog
    private lateinit var progressDialog: ProgressDialog
    //arraylist to push pdf categories
    private lateinit var arrayCategotyList:ArrayList<CategoryData>
    //uri of picked pdf
    private var pdfUri: Uri?= null
    //TAG for log
    private val TAG="PDF_ADD"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBookAddBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        loadPdfCategories()
        // Initialize progress dialog
        progressDialog= ProgressDialog(this.context)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        //edCategorie listener ->show category dialog
        binding.edCategorie.setOnClickListener {
            categoryDialog()
        }
        //pick pdf intent
        binding.attPdf.setOnClickListener {
            pdfPickIntent()
        }
        //button submit
        binding.btnAddBook.setOnClickListener {
            //validate data
            validation()
        }
        //back
        binding.btnBack1.setOnClickListener {
            view.findNavController().navigate(R.id.action_bookAddFragment_to_dashboardAdminFragment)
        }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")
        //initialize arrayCategoryList
        arrayCategotyList= ArrayList()
        //ref of root categories
        val ref=FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear arrayCategoryList and add data
                arrayCategotyList.clear()
                snapshot.children.forEach {
                    val id=it.child("id").getValue(String::class.java)!!
                    val category=it.child("category").getValue(String::class.java)!!
                    val timestamp=it.child("timestamp").getValue(Long::class.java)!!
                    val uid=it.child("uid").getValue(String::class.java)!!
                    val categoryData= CategoryData(id,category,timestamp,uid)
                    arrayCategotyList.add(categoryData)
                    Log.d(TAG,"onDataChange:${categoryData.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
    }

    private var title=""
    private var description=""
    private var category=""

    private fun validation() {
        title=binding.edTitle.text.toString().trim()
        description=binding.edDescription.text.toString().trim()
        category=binding.edCategorie.text.toString().trim()

        if(title.isEmpty())
            Toast.makeText(this.context,"Enter Title ..", Toast.LENGTH_LONG).show()
        else if(description.isEmpty())
            Toast.makeText(this.context,"Enter Description ..", Toast.LENGTH_LONG).show()
        else if(category.isEmpty())
            Toast.makeText(this.context,"Enter Category ..", Toast.LENGTH_LONG).show()
        else if(pdfUri==null)
            Toast.makeText(this.context,"Pick Pdf ..", Toast.LENGTH_LONG).show()
        else
            uploadPdfToStorage()
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG,"uploading to storage")
        //show progress dialog
        progressDialog.setMessage("Uploading pdf ...")
        progressDialog.show()

        //timestamp
        val timestamp=System.currentTimeMillis()

        //path of pdf in firebase storage
        val filePathAndName="Books/${timestamp}"

        //reference to storage
        val storageReference= FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapeShot->
                //return url of uploaded pdf
                val uriTask=taskSnapeShot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploaedPdfUrl=uriTask.result.toString()
                uploadedPdfInfoToDatabase(uploaedPdfUrl,timestamp)
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Failed to upload due to ${e.message}",Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadedPdfInfoToDatabase(uploaedPdfUrl: String, timestamp: Long) {
        progressDialog.setMessage("Uploading Pdf Info ..")
        progressDialog.show()
        val map=HashMap<String,Any>()
        map["id"]=timestamp.toString()
        map["categoryId"]=selectedCategoryId
        map["description"]=description
        map["title"]=title
        map["url"]=uploaedPdfUrl
        map["timestamp"]=timestamp
        map["uid"]=auth.uid.toString()
        map["viewCount"]=0
        map["downloadCount"]=0

        //reference of root
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(timestamp.toString())
            .setValue(map)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.edTitle.text.clear()
                Toast.makeText(this.context,"Uploaded successfully ...",Toast.LENGTH_LONG).show()
                pdfUri=null
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this.context,"Failed to add due to ${e.message}",Toast.LENGTH_LONG).show()
            }
    }

    private var selectedCategoryId=""
    private var selectedCategoryTitle=""
    private fun categoryDialog(){
        Log.d(TAG,"categoryDialog:Showing pdf category pich dialog")
        //get string array of categories
        val arrayCategories= arrayOfNulls<String>(arrayCategotyList.size)
        var i=0
        arrayCategotyList.forEach {
            arrayCategories[i]=it.category
            i++
        }
        //alert dialog show when we pressed in category EditText
        val builder= AlertDialog.Builder(this.context)
        builder.setTitle("Category")
            .setItems(arrayCategories){ dialog, which->
                //get selected category and put them in EditText
                selectedCategoryId=arrayCategotyList.get(which).id
                selectedCategoryTitle=arrayCategotyList.get(which).category
                binding.edCategorie.setText(selectedCategoryTitle)
            }
            .show()
    }
    private fun pdfPickIntent(){
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent")
        val intent= Intent()
        intent.type="application/pdf"
        intent.action= Intent.ACTION_GET_CONTENT
        pdfResultLauncher.launch(intent)
    }
    val pdfResultLauncher=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result->
            if(result.resultCode== AppCompatActivity.RESULT_OK){
                Log.d(TAG,"PDF picked")
                pdfUri=result.data?.data
            }
            else{
                Log.d(TAG,"PDF pick Cancelled")
                Toast.makeText(this.context,"cancelled",Toast.LENGTH_LONG).show()
            }
        }
    )
}