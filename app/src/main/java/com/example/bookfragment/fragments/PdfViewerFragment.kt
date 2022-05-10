package com.example.bookfragment.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.bookfragment.Constants
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentMoreDetailsPdfBinding
import com.example.bookfragment.databinding.FragmentPdfViewerBinding
import com.google.firebase.storage.FirebaseStorage

class PdfViewerFragment : Fragment() {
    private lateinit var binding: FragmentPdfViewerBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentPdfViewerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var url=arguments?.getString("bookUrl").toString()
        loadBookFromStorage(url)
    }

    private fun loadBookFromStorage(bookUrl: String) {
        val TAG="LOAD_PDF"
        val ref= FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        ref.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                binding.pdfViewer.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange{page,pageCount->
                        val currentPage=page+1//because page start with 0
                        binding.Pages.text="$currentPage/$pageCount"
                        Log.d(TAG,"Loaded successfuly page $currentPage/$pageCount :)")
                    }
                    .onError {
                        Log.d(TAG,"Error due to ${it.message} :(")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG,"Error to load ${page} due to  ${t.message} :(")
                    }
                    .load()
                binding.progressBarViewer.visibility= View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this.context,"blala", Toast.LENGTH_LONG).show()
                Log.d(TAG,"Error to get Book due to  ${it.message} :(")
                binding.progressBarViewer.visibility= View.GONE
            }
    }
}