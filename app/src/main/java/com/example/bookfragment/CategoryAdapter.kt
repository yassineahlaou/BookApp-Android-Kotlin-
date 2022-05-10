package com.example.myproject
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.bookfragment.CategoryData
import com.example.bookfragment.R
import com.example.bookfragment.databinding.CategoryLayoutBinding
import com.google.firebase.database.FirebaseDatabase

class CategoryAdapter(var dataList:ArrayList<CategoryData>): RecyclerView.Adapter<CategoryAdapter.myViewHolder>(){
    private lateinit var binding: CategoryLayoutBinding
    class myViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var category=itemView.findViewById<TextView>(R.id.tv_category)
        var delete=itemView.findViewById<ImageButton>(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        var view:View=LayoutInflater.from(parent.context).inflate(R.layout.category_layout,parent,false)
        return myViewHolder(view)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        //get and set data
        var p=dataList.get(position)
        holder.category.text=p.category
        holder.itemView.setOnClickListener {
            //send data to PdfListAdminFragment by using fragment
            val bundle = bundleOf( "categoryId" to p.id,
                                          "category" to p.category)
            holder.itemView.findNavController().navigate(R.id.action_dashboardAdminFragment_to_pdfListAdminFragment,bundle)
        }
        holder.delete.setOnClickListener {
            //confirm message : user has two choices : ->delete
            //                                         ->return
            val builder=AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Delete")
                .setMessage("You are in deleting process")
                .setPositiveButton("Confirm"){_,_->
                       //we use _,_ because none of these args used
                        Toast.makeText(holder.itemView.context,"Deleting ...",Toast.LENGTH_LONG).show()
                        deleteCategory(p.id,holder)
                   }
                   .setNegativeButton("Cancel"){a,d->
                        a.dismiss()// a it means DialogInterface
                   }
                .show()
        }
    }

    private fun deleteCategory(id: String,holder: myViewHolder) {
        //acces to root
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
             ref.child(id)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context,"Deleted successfuly",Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {e->
                    Toast.makeText(holder.itemView.context,"Deleted interumpt due to ${e.message}",Toast.LENGTH_LONG).show()
                }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}