package com.example.bookfragment

import android.app.Application
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MyApplication: Application(){
    override fun onCreate() {
        super.onCreate()
    }
    companion object{
        //function to format timestamp to dd/mm/yyyy
        fun formatTimeStamp(timestamp: Long):String{
            val calendar= Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis=timestamp
            return DateFormat.format("dd/mm/yyyy",calendar).toString()
        }
        //function to get size of pdf
        fun loadPdfSize(pdfUrl:String, pdfTitle: String, sizeTv: TextView){
            //get file from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {
                    val bytes= it.sizeBytes.toDouble()
                    //convert to KB and MB
                    val kb=bytes/1024
                    val mb=kb/1024
                    if(mb>1)
                        sizeTv.text= String.format("%.2f MB",mb)
                    else if(kb>1)
                        sizeTv.text= String.format("%.2f KB",kb)
                    else
                        sizeTv.text= String.format("%.2f bytes",bytes)
                }
                .addOnFailureListener {

                }
        }
        fun loadPdfSinglePage(pdfUrl:String, pdfTitle:String,  pdfView: PDFView, progressBar: ProgressBar, pagesTv: TextView?){
            val TAG="PDF_Sigle_Page"
            //get file and its metadata from firebase storage
            val ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes->
                    //set to pdfVeiw
                    pdfView.fromBytes(bytes)
                        .pages(0)//show only the first page
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { e->
                            progressBar.visibility= View.INVISIBLE
                            Log.d(TAG,"Error: to load due to ${e.message}")
                            Toast.makeText(null, "Error", Toast.LENGTH_LONG).show()
                        }
                        .onPageError { page, t ->
                            progressBar.visibility= View.INVISIBLE
                            Log.d(TAG,"Error: to load page due to ${t.message}")
                            Toast.makeText(null, "Error page", Toast.LENGTH_LONG).show()
                        }
                        .onLoad{nbPages->
                            progressBar.visibility= View.INVISIBLE
                            if(pagesTv!=null)
                                pagesTv.text=nbPages.toString()
                        }
                        .load()
                }
                .addOnFailureListener {
                    Log.d(TAG,"Failed to get bytes due to ${it.message}")
                }
        }

        fun loadCategory(categoryId:String,categoryTv: TextView){
            val ref= FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category=snapshot.child("category").value.toString()
                        categoryTv.text=category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        fun deleteBook(context: Context, bookId:String, bookUrl:String, bookTitle:String){
            //progree dialog
            val progressDialog= ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
            //firt we should delete the book from firebase storage
            val storageRef= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageRef.delete()
                .addOnSuccessListener {
                    //delete book info from Realtime database
                    val ref= FirebaseDatabase.getInstance().getReference("Books")
                        .child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"deleting book successfuly", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(context,"Failed deleting book from database due to ${it.message}",
                                Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context,"Failed deleting book from storage due to ${it.message}",
                        Toast.LENGTH_LONG).show()
                }
        }
        fun incrementBookView(bookId:String){
            val ref= FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        var viewsCount=snapshot.child("viewCount").value.toString()
                        if(viewsCount=="")
                            viewsCount="0"
                        //incremented val
                        var newViewCount=viewsCount.toLong()+1
                        val map= HashMap<String,Any>()
                        map["viewCount"]=newViewCount
                        val dbRef= FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(map)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        fun incrementDownloadCount(bookId:String){
            val ref= FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        var downloadsCount=snapshot.child("downloadCount").value.toString()
                        //incremented val
                        var newDownloadsCount=downloadsCount.toLong()+1
                        val map= HashMap<String,Any>()
                        map["downloadCount"]=newDownloadsCount
                        val dbRef= FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(map)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        public fun removeFromReserved (context: Context, bookId: String){
            val TAG = "UnReserved"
            val auth = FirebaseAuth.getInstance()



            Log.d(TAG, "remove from Reserved")
            //database ref
            val ref = FirebaseDatabase.getInstance().getReference(  "Users")
            ref.child(auth.uid!!).child("Reserved").child(bookId)
                .removeValue()
                .addOnSuccessListener {

                    Log.d(ContentValues.TAG, "Unreserved")
                    Toast.makeText(context, "Unreserved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.d(ContentValues.TAG, "Failed to remove from reserved books due to ${e.message}")
                    Toast.makeText(
                        context,
                        "Failed to remove from reserved books due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}