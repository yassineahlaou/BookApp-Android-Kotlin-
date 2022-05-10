package com.example.bookfragment.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.bookfragment.MyApplication
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentProfileEditBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(this.context)
        progressDialog.setTitle("Please Hold On")
        progressDialog.setCanceledOnTouchOutside(false)

        //firebase

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_profileEditFragment_to_profileFragment)
        }
        binding.updateBtn.setOnClickListener {
            validateData()

        }
        binding.profileIv.setOnClickListener {
            showImageAttachMenu()

        }



    }



    private var name = ""
    private fun validateData() {
        //get data
        name = binding.nameEt.text.toString().trim()
        //validate data
        if (name.isEmpty()) {
            //name not entered
            Toast.makeText(this.context, "The name !!", Toast.LENGTH_SHORT).show()
        } else {
            //name is entered
            if (imageUri == null) {
                //need to update without image
                updateProfile("")
            } else {
                //need to update with image
                uploadImage()
            }
        }
    }

    private fun loadUserInfo() {
        //db reference to l oad user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info

                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"


                    //convert timestanp to peroper date format
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())
                    //set data
                    binding.nameEt.setText(name)

                    //set image


                    try {
                        Glide.with(this@ProfileEditFragment).load(profileImage)
                            .placeholder(R.drawable.ic_person_gray).into(binding.profileIv)
                    } catch (e: Exception) {


                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }


            })

    }

    private fun showImageAttachMenu() {
        //Show popup menu with options Camera, Gallery to pick image/
        //setup popup menu
        val popupMenu = this.context?.let { PopupMenu(it, binding.profileIv) }
        popupMenu?.menu?.add(Menu.NONE, 0, 0, "Camera")
        popupMenu?.menu?.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu?.show()
        //handte popup menu item click
        popupMenu?.setOnMenuItemClickListener { item ->
            //get id of clicked item
            val id = item.itemId
            if (id == 0) {
                //Canera clicked
                pickImageCamera()
            } else if (id == 1) {
                // Gallery cticked
                pickImageGallery()
            }
            true
        }

    }

    private fun pickImageCamera() {
        //intent to pick image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        //intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    //used to handle result of camera intent (new way in reptacenent of stactectivituferresukts)
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            //get uri of inage
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                //imageUri = data!!.data  no need we have imageuri in this case
                //set to imageview
                binding.profileIv.setImageURI(imageUri)
            } else {
                //cancelled
                Toast.makeText(this.context, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    //used to handle result of gallery intent (new way in replacement of startactivityforresults)
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            //get uri of image
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                //set to imageview
                binding.profileIv.setImageURI(imageUri)
            } else {
                //cancelled
                Toast.makeText(this.context, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )


    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating profile...")
        //setup info to update to db
        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["name"] = "$name"
        if (imageUri != null) {
            hashmap["profileImage"] = uploadedImageUrl
        }
        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                //profile updated
                progressDialog.dismiss()
                Toast.makeText(this.context, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //failed to update profile
                progressDialog.dismiss()
                Toast.makeText(
                    this.context,
                    "Failed to update profile due to ${e.message})",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }


    private fun uploadImage() {
        progressDialog.setMessage("UpLoading profile image")
        progressDialog.show()


        //nage paht and nane, use vid to replace previous
        val filePathAndName = "Profileimages/" + firebaseAuth.uid
        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // image uploaded, get url of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener { e ->
                // falled to upload image
                progressDialog.dismiss()
                Toast.makeText(
                    this.context,
                    "Failed to upload inage due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}