package com.example.bookfragment

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.bookfragment.databinding.RowPdfUserBinding
import com.example.bookfragment.fragments.MoreDetailsPdfFragment


class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> , Filterable{

    //context, get using constructor

    private var context: Context

    //arraylist to hold pofs, get using constructor
    public var pdfArrayList : ArrayList<PdfModelData> //to access in filter class make public

    //arraytist to hold filtered pdfs
    public var filterList : ArrayList<PdfModelData>

    //viewBinding row.pdf_user.xml => RowPafuserBinding
    private lateinit var binding: RowPdfUserBinding

    private var filter : FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<PdfModelData>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {

        //inflate/bind Layout row_pdf_user.xml
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser (binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
/*Get data,set data , handleclick etc */
//get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //convertime
        val date = MyApplication.formatTimeStamp(timestamp)
        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date
        MyApplication.loadPdfSinglePage (url, title , holder.pdfView ,holder.progressBar, null)//no need number of pages so pass bull
        MyApplication.loadCategory(categoryId, holder.categoryTv)
        MyApplication.loadPdfSize(url  , title, holder.sizeTv)
        holder.itemView.setOnClickListener {
            /*val intent = Intent(context, MoreDetailsPdfFragment::class.java)
            intent.putExtra("bookId", bookId)
            intent.putExtra("bookUrl",url)
            intent.putExtra("bookTitle",title)
            intent.putExtra("bookdate",date)
            intent.putExtra("bookCategoryId",categoryId)
            context.startActivity(intent)*/

            val bundle = bundleOf(
                "bookId" to bookId,
                "bookTitle" to title,
                "bookDescription" to description,
                "bookdate" to date,
                "bookUrl" to url,
                "bookCategoryId" to categoryId,
            )
            holder.itemView.findNavController().navigate(R.id.action_dashboardUserFragment_to_moreDetailsPdfFragment,bundle)
        }

    }

    override fun getItemCount(): Int {

        return pdfArrayList.size  //return list size
    }
    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(filterList, this)
        }
            return filter as FilterPdfUser
    }

    /*viewtolder class row_pdf_user.xml*/
    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder (itemView) {
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }




}
