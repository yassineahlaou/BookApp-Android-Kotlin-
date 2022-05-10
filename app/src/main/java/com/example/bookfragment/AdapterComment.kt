package com.example.bookfragment

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookfragment.databinding.RowCommentBinding
import com.example.bookfragment.fragments.MoreDetailsPdfFragment
import com.example.myproject.ModelComment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComment>{

    val context: Context
    //arraylist to hold comments
    val comnentArrayList: ArrayList<ModelComment>
    //view binding row_comment.xml > RowCommentBinding
    private lateinit var binding: RowCommentBinding
    private lateinit var auth: FirebaseAuth
    //constructor
    constructor(context: Context, comnentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.comnentArrayList = comnentArrayList

        auth= FirebaseAuth.getInstance()
    }
        //viewholder class for row_comment.xml/
        inner class HolderComment (itemView: View): RecyclerView.ViewHolder(itemView) {

            //init ui views of row connent.xml
            val profileIv = binding.profileIv
            val nameTv =  binding.nameTv
            val dateTv = binding.dateTv
            val commentTv =  binding.commentTv


        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context),parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        //get data, set data, handle click etc
        //get data
        val model = comnentArrayList[position]
        val  id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp
        //fornat timestaMp
        val date =  MyApplication.formatTimeStamp(timestamp.toLong())
        //set data
                holder.dateTv.text =  date
        holder.commentTv.text =  comment
//ve don't have user name, profile picture but we have user uid, so we will load using uid
                loadUserDetails (model, holder)
        //handle click, show dialog to delete comment
        holder.itemView.setOnClickListener {
            //Requirenents to delete a comment
            // User must be logged in
            // uid in comnent (to be deleted) must be same as uid of current user 1.e. user can delete onty his own connente/
            if (auth.currentUser != null && auth.uid==uid ) {
                deleteCommentDialog(model, holder)
            }}


    }

    private fun deleteCommentDialog(model: ModelComment, holder: HolderComment) {
        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
            .setMessage("You really want to delete this comment?")
            .setPositiveButton( "DELETE") { d, e ->
                val bookId = model.bookId
                val commentId = model.id
                //delete comment
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted!!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()


                    }
            }
                .setNegativeButton( "CANCEL") { d, e ->
                    d.dismiss()
                }

            .show()

    }

    private fun loadUserDetails(model: ModelComment, holder: HolderComment) {

        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference( "Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange (snapshot: DataSnapshot) {
                    //get name, profile image
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    //set data
                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)
                    } catch (e: Exception) {
                        //in case of exception due to image is enpty or null or other reason, set default image
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }
                }
                        override fun onCancelled(error: DatabaseError) {

                        }
                        })

    }

    override fun getItemCount(): Int {
        return comnentArrayList.size //list size
    }

}