package com.example.twitter.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.twitter.listeners.HomeCallback
import com.example.twitter.listeners.TweetListener
import com.example.twitter.listeners.TweetListenerImpl
import com.example.twitter.mvvm.AppwriteRepository
import com.example.twitter.mvvm.AppwriteViewModel
import com.example.twitter.utils.User
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class TwitterFragment: Fragment() {

    protected var currentUser: User?  = null
    protected val appwriteViewModel: AppwriteViewModel by viewModel()
    protected var userId: String? = ""
    protected var listener : TweetListenerImpl? = null
    protected var callback: HomeCallback? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is HomeCallback){
            callback = context
        }
        else throw RuntimeException(context.toString() + " must implement HomeCallback")

        appwriteViewModel.user.observe(this) { user ->
            if (user != null) {
                userId = user.id // If TweetListener depends on currentUser
            } else {
                userId = null
            }
        }
    }

    fun setUser(user: User?){
        this.currentUser = user
        listener?.user = user
    }

    abstract fun updateList()

    override fun onResume() {
        super.onResume()
        updateList()
    }
}