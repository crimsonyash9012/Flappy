package com.example.flappy.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flappy.R
import com.example.flappy.activity.LoginActivity
import com.example.flappy.adapters.CustomAdapter
import com.example.flappy.listeners.TweetListenerImpl
import com.example.flappy.utils.Tweet
import com.example.flappy.utils.User
import kotlinx.coroutines.launch

class MyActivityFragment : TwitterFragment() {
    lateinit var adapter: CustomAdapter
    lateinit var tweetList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_activity, container, false)

        tweetList = view.findViewById(R.id.tweetList)

        try {
            appwriteViewModel.user.observe(viewLifecycleOwner) {
                if (it == null) {
                    startActivity(Intent(context, LoginActivity::class.java))
                    Toast.makeText(
                        context,
                        "Can't open up. Either Login properly or SignUp for a new user.",
                        Toast.LENGTH_LONG
                    ).show()
//                    adapter = CustomAdapter(userId!!, mutableListOf())
                    activity?.finish()
                }
                if (it != null) {
                    userId = it.id
                }

                if (::adapter.isInitialized) {
                    Log.e("home", "adapter is initialized")
                    adapter.updateUserId(userId!!)
                } else {
                    Log.e("home", "adapter is not initialized")
                    adapter = CustomAdapter(userId!!, mutableListOf())
                }

                listener =
                    TweetListenerImpl(
                        tweetList!!,
                        currentUser,
                        callback,
                        appwriteViewModel,
                        adapter!!,
                        MyActivityFragment()
                    )

                tweetList.layoutManager = LinearLayoutManager(context)
                tweetList.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )
                appwriteViewModel.commentCounts.observe(viewLifecycleOwner) { map ->
                    adapter.setCommentCounts(map)
                }

                adapter?.setListener(listener)
                tweetList.adapter = adapter

                onSwipe()
            }
        } catch (e: Exception) {
            adapter = CustomAdapter(userId!!, mutableListOf())
            Log.e("home", e.message.toString())
        }
        adapter = CustomAdapter(userId!!, mutableListOf())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            onSwipe()
        }
    }

    private fun onSwipe() {
        refreshUser()
        updateList()
    }

    private fun refreshUser() {
        appwriteViewModel.getUser(userId)
        appwriteViewModel.userDoc.observe(viewLifecycleOwner) { doc ->
            if (doc != null) {
                Log.e("refreshUser", "User document fetched: ${doc.data}")
                currentUser = User(
                    user_Id = doc.data["user_Id"].toString(),
                    name = doc.data["name"].toString(),
                    email = doc.data["email"].toString(),
                    imageUrl = doc.data["imageUrl"].toString(),
                    followHashtags = (doc.data["followHashtags"] as? List<String>)?.toTypedArray()
                        ?: arrayOf(),
                    followUsers = (doc.data["followUsers"] as? List<String>)?.toTypedArray()
                        ?: arrayOf()
                )
            } else {
                Log.e("refreshUser", "Fetched user document is null.")
            }
        }
    }

    override fun updateList() {
        tweetList?.visibility = View.GONE
        val myTweets = mutableListOf<Tweet>()
        currentUser?.let { user ->
            val curUserId = user.user_Id!!

            if (curUserId != null || curUserId != "") {
                appwriteViewModel.getCurrentUserTweet(
                    curUserId,
                    onSuccess = {
                        val curUserTweet = appwriteViewModel.getCurUserTweet.value
                        lifecycleScope.launch {
                            if (curUserTweet != null) {
                                val newUsers = curUserTweet.documents.map { document ->
                                    val data = document.data
                                    var timestamp = data["timestamp"] as? String
                                    val userIds =
                                        (data["userIds"] as? List<String>)?.toTypedArray()
                                            ?: arrayOf()
                                    val tweetId = data["\$id"] as String
                                    if (userIds[0] != curUserId) {
                                        val gotTimestamp = getTimestamp(tweetId, curUserId)
                                        Log.e("got timestamp", "ts: $gotTimestamp")
                                        if (gotTimestamp != null) timestamp = gotTimestamp
                                    }
                                    Tweet(
                                        tweetId = tweetId,
                                        userIds = userIds,
                                        username = data["username"] as? String,
                                        text = data["text"] as? String,
                                        imageUrl = data["imageUrl"] as? String,
                                        timestamp = timestamp,
                                        hashtags = (data["hashtags"] as? List<String>)?.toTypedArray()
                                            ?: arrayOf(),
                                        likes = (data["likes"] as? List<String>)?.toTypedArray()
                                            ?: arrayOf()
                                    )
                                }
                                myTweets.addAll(newUsers)
                            } else {
                                Log.e("Home frag", "No tweets with such hashtag exist")
                            }

                            updateAdapter(myTweets)
                            tweetList?.visibility = View.VISIBLE
                        }

                    },
                    onError = { e ->
                        Log.e("home frag", e.message.toString())
                    })
            }
        }
    }

    private suspend fun getTimestamp(tweetId: String, userId: String): String? {
        return appwriteViewModel.getRetweet(tweetId, userId, onSuccess = {}, onError = {})
    }

    private fun updateAdapter(tweets: List<Tweet>) {
        val sortedTweets = tweets.sortedWith(compareByDescending { it.timestamp })
        val tweetIds = sortedTweets.mapNotNull { it.tweetId }
        lifecycleScope.launch {
            appwriteViewModel.fetchCommentCountsForTweets(tweetIds)
        }

        adapter?.updateItems(removeDuplicates(sortedTweets))
    }

    private fun removeDuplicates(originalList: List<Tweet>) = originalList.distinctBy { it.tweetId }
}