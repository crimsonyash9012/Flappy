package com.example.flappy.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flappy.R
import com.example.flappy.activity.LoginActivity
import com.example.flappy.adapters.CustomAdapter
import com.example.flappy.databinding.FragmentSearchBinding
import com.example.flappy.listeners.TweetListenerImpl
import com.example.flappy.utils.Tweet
import com.example.flappy.utils.User
import io.appwrite.models.DocumentList
import kotlinx.coroutines.launch

class SearchFragment : TwitterFragment() {

    private var tweetCache = mutableListOf<Tweet>()
    private var scrollPosition = 0

    val searchBinding: FragmentSearchBinding by lazy { FragmentSearchBinding.inflate(layoutInflater) }
    private var currentHashtag = ""
    private var hashtagFollowed = false

//    private var adapter: CustomAdapter = CustomAdapter(userId!!, mutableListOf())
    lateinit var adapter: CustomAdapter
    lateinit var tweetList: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        tweetList = view.findViewById<RecyclerView>(R.id.tweetList)

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

//            adapter = CustomAdapter(userId!!, mutableListOf())
//                Log.e("search", userId!!)
                if (::adapter.isInitialized) {
                    Log.e("search", "adapter is initialized")
                    adapter.updateUserId(userId!!)
                } else {
                    Log.e("search", "adapter is not initialized")
                    adapter = CustomAdapter(userId!!, mutableListOf())
                }

                listener =
                    TweetListenerImpl(
                        searchBinding.tweetList,
                        currentUser,
                        callback,
                        appwriteViewModel,
                        adapter!!,
                        SearchFragment()
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
                updateList()
            }
        }
        catch(e: Exception) {
            adapter = CustomAdapter(userId!!, mutableListOf())
            Log.e("search", e.message.toString())
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

        val followHashtag = view?.findViewById<ImageView>(R.id.followHashtag)
        followHashtag?.setOnClickListener {
            followHashtag?.isClickable = false

            refreshUser()
            val followed = currentUser?.followHashtags?.toMutableList() ?: mutableListOf()
            hashtagFollowed = followed.contains(currentHashtag) == true
            if (hashtagFollowed) {
                followed.remove(currentHashtag)
            } else {
                followed.add(currentHashtag)
            }

            val updatedArray = followed.toTypedArray()
            Log.e("followHashtag", "Updated hashtags: ${updatedArray.joinToString()}")
            appwriteViewModel.updateUserData(
                updatedArray,
                true,
                arrayOf(),
                false,
                arrayOf(),
                false,
                "",
                onSuccess = { it ->
                    callback?.onUserUpdated()
                    hashtagFollowed = !hashtagFollowed
                    refreshUser()
                    updateFollowDrawable()
                    followHashtag?.isClickable = true
                },
                onError = { it ->
                    Log.e("Search Fragment", it.message.toString())
                    followHashtag?.isClickable = true
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        val layoutManager = tweetList.layoutManager as? LinearLayoutManager
        scrollPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
    }


    fun newHashtag(term: String) {
        if (currentHashtag == term && tweetCache.isNotEmpty()) {
            adapter.updateItems(tweetCache)
            tweetList.scrollToPosition(scrollPosition)
            return
        }

        currentHashtag = term
        Log.e("newHashtag", term)
        val followHashtag = view?.findViewById<ImageView>(R.id.followHashtag)
        followHashtag?.visibility = View.VISIBLE
        updateList()
    }

    private fun onSwipe() {
        refreshUser()
        updateFollowDrawable()
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
        tweetList.visibility = View.GONE
        val followHashtag = view?.findViewById<ImageView>(R.id.followHashtag)

        if (currentHashtag.isBlank()) {
            followHashtag?.visibility = View.GONE
            return
        }

        appwriteViewModel.getHashtag.removeObservers(viewLifecycleOwner)

        // Remove any previous observers before adding a new one

        val observer = androidx.lifecycle.Observer { docList: DocumentList<Map<String, Any>>? ->
            if (docList != null) {
                val newTweets = docList.documents.mapNotNull { document ->
                    val data = document.data
                    Tweet(
                        tweetId = data["\$id"] as String,
                        userIds = (data["userIds"] as? List<String>)?.toTypedArray() ?: arrayOf(),
                        username = data["username"] as? String,
                        text = data["text"] as? String,
                        imageUrl = data["imageUrl"] as? String,
                        timestamp = data["timestamp"] as? String,
                        hashtags = (data["hashtags"] as? List<String>)?.toTypedArray() ?: arrayOf(),
                        likes = (data["likes"] as? List<String>)?.toTypedArray() ?: arrayOf()
                    )
                }

                val sortedTweets = newTweets.sortedByDescending { it.timestamp?.toLong() }
                tweetCache.clear()
                tweetCache.addAll(sortedTweets)

                val tweetIds = sortedTweets.mapNotNull { it.tweetId }
                lifecycleScope.launch {
                    appwriteViewModel.fetchCommentCountsForTweets(tweetIds)
                }

                adapter?.updateItems(sortedTweets)
                tweetList.scrollToPosition(scrollPosition)
                tweetList.visibility = View.VISIBLE

                appwriteViewModel.userDoc.observe(viewLifecycleOwner) { doc ->
                    if (doc != null) {
                        val followed = (doc.data["followHashtags"] as? List<String>) ?: listOf()
                        if (followed.contains(currentHashtag)) {
                            followHashtag?.visibility = View.VISIBLE
                            followHashtag?.setImageResource(R.drawable.follow)
                        } else {
                            followHashtag?.visibility = View.VISIBLE
                            followHashtag?.setImageResource(R.drawable.follow_inactive)
                        }
                    } else {
                        Log.e("updateList", "Fetched user document is null.")
                    }
                }
            } else {
                Log.e("SearchFragment", "No tweets found for hashtag: $currentHashtag")
            }
        }

        appwriteViewModel.getHashtag.observe(viewLifecycleOwner, observer)

        appwriteViewModel.getSearchedHashtag(
            currentHashtag,
            onSuccess = { /* no-op */ },
            onError = { Log.e("SearchFragment", "Hashtag fetch failed: ${it.message}") },
            false
        )
    }


    private fun updateFollowDrawable() {
        hashtagFollowed =
            currentUser?.followHashtags?.toMutableList()?.contains(currentHashtag) == true
        val string = currentUser?.followHashtags?.contentToString()
        if (string == null) Log.e("updateFollowDrawable", "string is null")
        else Log.e("updateFollowDrawable", string)

        val followHashtag = view?.findViewById<ImageView>(R.id.followHashtag)
        if (hashtagFollowed) {
            Log.e("updateFollowDrawable", "Hashtag is Followed")
            followHashtag?.setImageResource(R.drawable.follow_inactive)
        } else {
            Log.e("updateFollowDrawable", "Hashtag is Unfollowed")
            followHashtag?.setImageResource(R.drawable.follow)
        }
    }

}
