package com.example.bookfragment.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.example.bookfragment.R
import com.example.bookfragment.databinding.FragmentDashboardUserBinding
import com.example.bookfragment.CategoryData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardUserFragment : Fragment() {
    private lateinit var binding: FragmentDashboardUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var categoryArrayList : ArrayList<CategoryData>
    private lateinit var viewPagerAdapter : ViewPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentDashboardUserBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth= FirebaseAuth.getInstance()
        checkUser()
        setupWithViewPagerAdapter (binding.ViewPager)
        binding.tablayout.setupWithViewPager(binding.ViewPager)
        binding.btnLogoutUser.setOnClickListener {
            auth.signOut()
            checkUser()
        }
        //handle elick, open profile
        binding.profileBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_dashboardUserFragment_to_profileFragment)

        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = this.context?.let {
            ViewPagerAdapter(
                parentFragmentManager,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, it
            )
        }!!
        //init list
        categoryArrayList = ArrayList()
        //load categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear List
                categoryArrayList.clear()
                //load some static categories e.g. ALt, Most Viewed, Most Downloadede
                //Add data to models
                val modelAll = CategoryData("01", "All", 1, "")
                val modelMostViewed = CategoryData("01", "Most Viewed", 1, "")
                val modelMostDownloaded = CategoryData("01", "Most Downloaded", 1, "")

                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                //add to vienPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",

                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",

                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )


                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",

                        "${modelMostDownloaded.uid}"
                    ), modelMostDownloaded.category
                )
                //refresh List
                viewPagerAdapter.notifyDataSetChanged()
                //NOW Load from firebase db
                for (ds in snapshot.children) {
                    //get ata in model
                    val model = ds.getValue(CategoryData::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewPagerAdapter
                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    //refresh List
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }


            override fun onCancelled(error: DatabaseError) {

            }
        })
        //setup adapter to viewpager
        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fn: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fn, behavior) {
        //holds list of fragments 1.e. new instances of sane fragment for each category
        private val fragmentsList: ArrayList<BooksUserFragment> = ArrayList()

        //lists of titles of categories, for tabs
        private val fragmentTitleList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BooksUserFragment, title: String) {
            //add fragment that will be passed as paraneter in fragmentlist
            fragmentsList.add(fragment)
            //add titte that wilt be passed as parameter
            fragmentTitleList.add(title)
        }
    }



    private fun checkUser() {
        val user=auth.currentUser
        if(user==null){
            binding.userEmail.text="None"
            binding.profileBtn.visibility = View.GONE
            binding.btnLogoutUser.visibility = View.GONE
            view?.findNavController()?.navigate(R.id.action_dashboardUserFragment_to_betweenFragment)
        }
        else{
            val email=user.email
            binding.userEmail.text=email
            binding.profileBtn.visibility = View.VISIBLE
            binding.btnLogoutUser.visibility = View.VISIBLE
        }
    }
}