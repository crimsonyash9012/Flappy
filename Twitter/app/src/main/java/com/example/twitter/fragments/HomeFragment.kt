package com.example.twitter.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.twitter.R
import com.example.twitter.activity.LoginActivity
import com.example.twitter.adapters.CustomAdapter
import com.example.twitter.listeners.TweetListenerImpl
import com.example.twitter.utils.Tweet
import com.example.twitter.utils.User
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : TwitterFragment() {

    lateinit var adapter: CustomAdapter
    lateinit var tweetList: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

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
                        HomeFragment()
                    )

                tweetList.layoutManager = LinearLayoutManager(context)
                tweetList.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )

                adapter?.setListener(listener)
                tweetList.adapter = adapter
//                updateList()
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
            val allTweets = mutableListOf<Tweet>()
            currentUser?.let { user ->
                val hashtags = user.followHashtags ?: return
                val hashtagsList = hashtags.toMutableList() // Accumulate all tweets here
                var pendingHashtags = hashtags.size

                while (pendingHashtags > 0) {
                    Log.e("home frag", "pending hashtags: ${--pendingHashtags}")
                    val hashtag = hashtagsList[pendingHashtags]
                    appwriteViewModel.getSearchedHashtag(
                        hashtag,
                        onSuccess = {
                            val hashtagTweet = appwriteViewModel.getHashtagHome.value
                            if (hashtagTweet != null) {
                                val newTweets = hashtagTweet.documents.map { document ->
                                    val data = document.data
                                    Tweet(
                                        tweetId = data["\$id"] as String,
                                        userIds = (data["userIds"] as? List<String>)?.toTypedArray()
                                            ?: arrayOf(),
                                        username = data["username"] as? String,
                                        text = data["text"] as? String,
                                        imageUrl = data["imageUrl"] as? String,
                                        timestamp = data["timestamp"] as? String,
                                        hashtags = (data["hashtags"] as? List<String>)?.toTypedArray()
                                            ?: arrayOf(),
                                        likes = (data["likes"] as? List<String>)?.toTypedArray()
                                            ?: arrayOf()
                                    )
                                }
                                Log.e("Home frag", "$pendingHashtags: ${newTweets}")
                                allTweets.addAll(newTweets)
                            } else {
                                Log.e("Home frag", "No tweets with such hashtag exist")
                            }

                            if (pendingHashtags == 0) {
                                currentUser?.let { user ->
                                    val users = user.followUsers!!
                                    val followUsers = users.toMutableList()
                                    var pendingUsers = users.size

                                    if (pendingUsers == 0) {
                                        updateAdapter(allTweets)
                                        tweetList?.visibility = View.VISIBLE
                                    }

                                    while (pendingUsers > 0) {
                                        Log.e("home frag", "pending users: ${--pendingUsers}")
                                        val curUser = followUsers[pendingUsers]
                                        appwriteViewModel.getSearchedUserTweet(
                                            curUser,
                                            onSuccess = {
                                                val userTweet = appwriteViewModel.getUserTweet.value
                                                if (userTweet != null) {
                                                    val newUsers = userTweet.documents.map { document ->
                                                        val data = document.data
                                                        Tweet(
                                                            tweetId = data["\$id"] as String,
                                                            userIds = (data["userIds"] as? List<String>)?.toTypedArray()
                                                                ?: arrayOf(),
                                                            username = data["username"] as? String,
                                                            text = data["text"] as? String,
                                                            imageUrl = data["imageUrl"] as? String,
                                                            timestamp = data["timestamp"] as? String,
                                                            hashtags = (data["hashtags"] as? List<String>)?.toTypedArray()
                                                                ?: arrayOf(),
                                                            likes = (data["likes"] as? List<String>)?.toTypedArray()
                                                                ?: arrayOf()
                                                        )
                                                    }
                                                    Log.e("Home frag", "$pendingUsers: ${newUsers}")
                                                    allTweets.addAll(newUsers)
                                                } else {
                                                    Log.e(
                                                        "Home frag",
                                                        "No tweets with such hashtag exist"
                                                    )
                                                }

                                                // Update the adapter when all hashtags are processed
                                                if (pendingUsers == 0) {
                                                    updateAdapter(allTweets)
                                                    tweetList?.visibility = View.VISIBLE
                                                }

                                            },
                                            onError = { e ->
                                                Log.e("home frag", e.message.toString())
                                            })
                                    }
                                }
                            }

                        },
                        onError = { e ->
                            Log.e("home frag", e.message.toString())
                        },
                        true
                    )
                }
            }

            Log.e("home frag", "all tweets: ${allTweets.toTypedArray().contentToString()}")

            updateAdapter(allTweets)
            tweetList?.visibility = View.VISIBLE
        }


    private fun updateAdapter(tweets: List<Tweet>) {
        val sortedTweets = tweets.sortedWith(compareByDescending { it.timestamp })
        adapter?.updateItems(removeDuplicates(sortedTweets))
    }

    private fun removeDuplicates(originalList: List<Tweet>) = originalList.distinctBy { it.tweetId }
}