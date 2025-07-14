package com.example.flappy.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.flappy.R
import com.example.flappy.databinding.ActivityHomeBinding
import com.example.flappy.fragments.HomeFragment
import com.example.flappy.fragments.MyActivityFragment
import com.example.flappy.fragments.SearchFragment
import com.example.flappy.fragments.TwitterFragment
import com.example.flappy.listeners.HomeCallback
import com.example.flappy.mvvm.AppwriteViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.android.material.tabs.TabLayoutMediator


class HomeActivity : AppCompatActivity(), HomeCallback {

    private var sectionPagerAdapter: SectionPageAdapter? = null

    private val homeBinding : ActivityHomeBinding by lazy {ActivityHomeBinding.inflate(layoutInflater)}
    private val appwriteViewModel: AppwriteViewModel by viewModel()

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val myActivityFragment = MyActivityFragment()
    private var currentFragment: TwitterFragment = homeFragment

    var userName : String = ""

    private var backPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(homeBinding.root)

        populate()
        Log.e("home activity", "userName: $userName")
        var titleBar = findViewById<TextView>(R.id.titleBar)

        sectionPagerAdapter = SectionPageAdapter(this)
        homeBinding.container.adapter = sectionPagerAdapter
        TabLayoutMediator(homeBinding.tabs, homeBinding.container) { tab, position ->
            tab.text = when (position) {
                0 -> "Home"
                1 -> "Search"
                else -> "Activity"
            }
        }.attach()

        homeBinding.container.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        titleBar.visibility = View.VISIBLE
                        titleBar.text = "Home"
                        homeBinding.searchBar.visibility = View.GONE
                        currentFragment = homeFragment
                    }
                    1 -> {
                        titleBar.visibility = View.GONE
                        homeBinding.searchBar.visibility = View.VISIBLE
                        currentFragment = searchFragment
                    }
                    2 -> {
                        titleBar.visibility = View.VISIBLE
                        titleBar.text = "My Activity"
                        homeBinding.searchBar.visibility = View.GONE
                        currentFragment = myActivityFragment
                    }
                }
            }
        })

        homeBinding.logo.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        homeBinding.fab.setOnClickListener {
            appwriteViewModel.user.observe(this){
                if(it!=null){
                    startActivity(TweetActivity.newIntent(this, it.id, userName))
                }
            }
        }
        homeBinding.homeProgressLayout.setOnTouchListener { view, motionEvent ->  true}

        homeBinding.search.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH){
                searchFragment.newHashtag(homeBinding.search.text.toString())
            }
            true
        }

    }


    inner class SectionPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        private val fragments = listOf(homeFragment, searchFragment, myActivityFragment)

        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }


    fun populate(){
        homeBinding.homeProgressLayout.visibility = View.VISIBLE
        appwriteViewModel.getUser("")
        appwriteViewModel.user.observe(this){
            if(it!=null){
                Log.e("populate home", "user found")
                homeBinding.homeProgressLayout.visibility = View.GONE
            }
            else{
                Log.e("populate home", "user not found")
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                Toast.makeText(this,
                    "Can't open up. Either Login properly or SignUp for a new user.",
                    Toast.LENGTH_LONG).show()
                homeBinding.homeProgressLayout.visibility = View.GONE
                finish()
            }
        }
        appwriteViewModel.userDoc.observe(this){
            if(it!=null){
                userName = it.data["name"].toString()
                Log.e("populate home prof", it.data["imageUrl"].toString())
                Glide.with(this)
                    .load(it.data["imageUrl"].toString())
                    .into(homeBinding.logo)
//                updateFragmentUser()
            }
        }
    }

    override fun onUserUpdated() {
        populate()
    }

    override fun onRefresh() {
        currentFragment.updateList()
    }

    override fun onResume() {
        super.onResume()
        appwriteViewModel.getUser("")

        appwriteViewModel.userDoc.observe(this) {
            if (it != null) {
                userName = it.data["name"].toString()
                Glide.with(this)
                    .load(it.data["imageUrl"].toString())
                    .into(homeBinding.logo)
            }
        }
    }

    override fun onBackPressed() {
        if(backPressedTime+2000 > System.currentTimeMillis()){
            super.onBackPressed()
        }
        else{
            Toast.makeText(applicationContext, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}