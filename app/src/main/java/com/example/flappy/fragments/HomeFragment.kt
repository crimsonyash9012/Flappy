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
import com.example.flappy.utils.mapToTweet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : TwitterFragment() {

    lateinit var adapter: CustomAdapter
    lateinit var tweetList: RecyclerView

    private var page = 0
    private val pageSize = 10
    private var allFetchedTweets = listOf<Tweet>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        tweetList = view.findViewById(R.id.tweetList)

        appwriteViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                startActivity(Intent(context, LoginActivity::class.java))
                Toast.makeText(
                    context,
                    "Can't open up. Either Login properly or SignUp for a new user.",
                    Toast.LENGTH_LONG
                ).show()
                activity?.finish()
                return@observe
            }

            userId = user.id

            // Initialize adapter once
            if (!::adapter.isInitialized) {
                adapter = CustomAdapter(userId!!, mutableListOf())
                tweetList.layoutManager = LinearLayoutManager(context)
                tweetList.addItemDecoration(
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                )
                tweetList.adapter = adapter
            } else {
                adapter.updateUserId(userId!!)
            }

            listener = TweetListenerImpl(
                tweetList,
                currentUser,
                callback,
                appwriteViewModel,
                adapter,
                HomeFragment()
            )
            appwriteViewModel.commentCounts.observe(viewLifecycleOwner) { map ->
                adapter.setCommentCounts(map)
            }

            adapter.setListener(listener)

            onSwipe()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            onSwipe()
        }

        tweetList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItems = layoutManager.itemCount

                if (lastVisibleItem == totalItems - 1) {
                    loadMoreTweets()
                }
            }
        })
    }

    private fun onSwipe() {
        refreshUser {
            updateList()
        }
    }

    private fun refreshUser(onDone: () -> Unit) {
        appwriteViewModel.getUser(userId)
        lifecycleScope.launch {
            while (appwriteViewModel.userDoc.value == null) {
                delay(50)
            }

            val doc = appwriteViewModel.userDoc.value!!
            Log.e("refreshUser", "User document fetched: ${doc.data}")
            currentUser = User(
                user_Id = doc.data["user_Id"].toString(),
                name = doc.data["name"].toString(),
                email = doc.data["email"].toString(),
                imageUrl = doc.data["imageUrl"].toString(),
                followHashtags = (doc.data["followHashtags"] as? List<String>)?.toTypedArray() ?: arrayOf(),
                followUsers = (doc.data["followUsers"] as? List<String>)?.toTypedArray() ?: arrayOf()
            )
            onDone()
        }

    }

    override fun updateList() {
        tweetList?.visibility = View.GONE
        val allTweets = mutableListOf<Tweet>()

        currentUser?.let { user ->
            val followedHashtags = user.followHashtags ?: arrayOf()
            val followedUsers = user.followUsers ?: arrayOf()

            // Launch coroutine scope for parallel fetching
            lifecycleScope.launch {
                val jobs = mutableListOf<Deferred<List<Tweet>>>()

                // Fetch followed hashtags
                for (hashtag in followedHashtags) {
                    val job = async(Dispatchers.IO) {
                        try {
                            val success = CompletableDeferred<List<Tweet>>()
                            appwriteViewModel.getSearchedHashtag(
                                hashtag,
                                onSuccess = {
                                    val docs = appwriteViewModel.getHashtagHome.value?.documents ?: emptyList()
                                    val tweets = docs.mapNotNull { doc -> mapToTweet(doc.data) }
                                    success.complete(tweets)
                                },
                                onError = {
                                    Log.e("updateList", "Error fetching hashtag $hashtag: ${it.message}")
                                    success.complete(emptyList())
                                },
                                true
                            )
                            success.await()
                        } catch (e: Exception) {
                            Log.e("updateList", "Exception fetching hashtag $hashtag: ${e.message}")
                            emptyList()
                        }
                    }
                    jobs.add(job)
                }

                // Fetch followed users
                for (uid in followedUsers) {
                    val job = async(Dispatchers.IO) {
                        try {
                            val success = CompletableDeferred<List<Tweet>>()
                            appwriteViewModel.getSearchedUserTweet(
                                uid,
                                onSuccess = {
                                    val docs = appwriteViewModel.getUserTweet.value?.documents ?: emptyList()
                                    val tweets = docs.mapNotNull { doc -> mapToTweet(doc.data) }
                                    success.complete(tweets)
                                },
                                onError = {
                                    Log.e("updateList", "Error fetching user $uid: ${it.message}")
                                    success.complete(emptyList())
                                }
                            )
                            success.await()
                        } catch (e: Exception) {
                            Log.e("updateList", "Exception fetching user $uid: ${e.message}")
                            emptyList()
                        }
                    }
                    jobs.add(job)
                }

                // Always fetch random tweets
                val randomJob = async(Dispatchers.IO) {
                    try {
                        val docList = appwriteViewModel.getRandomTweets()
                        docList.documents.mapNotNull { mapToTweet(it.data) }
                    } catch (e: Exception) {
                        Log.e("updateList", "Error fetching random tweets: ${e.message}")
                        emptyList()
                    }
                }
                jobs.add(randomJob)

                // Wait for all jobs to finish
                val results = jobs.awaitAll()
                results.forEach { allTweets.addAll(it) }

                val oneHourAgo = System.currentTimeMillis() - 60 * 60 * 1000
                val recentTweets = allTweets.filter { it.timestamp!!.toLong() >= oneHourAgo }
                val otherTweets = allTweets.filter { it.timestamp!!.toLong() < oneHourAgo }

                val finalTweets = if (recentTweets.isNotEmpty()) {
                    val shuffledOthers = otherTweets.shuffled()
                    recentTweets + shuffledOthers
                } else {
                    allTweets.shuffled()
                }

                // Update UI
                updateAdapter(finalTweets)
                tweetList?.visibility = View.VISIBLE
            }
        }
    }


    private fun updateAdapter(tweets: List<Tweet>) {
        val sortedTweets = tweets.sortedWith(compareByDescending { it.timestamp })
        val tweetIds = sortedTweets.mapNotNull { it.tweetId }
        lifecycleScope.launch {
            appwriteViewModel.fetchCommentCountsForTweets(tweetIds)
        }

        adapter?.updateItems(removeDuplicates(sortedTweets))
    }

    private fun loadMoreTweets() {
        val start = page * pageSize
        val end = minOf((page + 1) * pageSize, allFetchedTweets.size)
        if (start >= allFetchedTweets.size) return

        val tweetsToLoad = allFetchedTweets.subList(start, end)
        adapter.addItems(tweetsToLoad)
        page++
    }

    private fun removeDuplicates(originalList: List<Tweet>) = originalList.distinctBy { it.tweetId }
}