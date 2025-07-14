package com.example.flappy.listeners

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.flappy.activity.CommentActivity
import com.example.flappy.activity.OtherProfileActivity
import com.example.flappy.adapters.CustomAdapter
import com.example.flappy.fragments.TwitterFragment
import com.example.flappy.mvvm.AppwriteViewModel
import com.example.flappy.utils.Tweet
import com.example.flappy.utils.User

class TweetListenerImpl(
    val tweetList: RecyclerView,
    var user: User?,
    val callback: HomeCallback?,
    private val appwriteViewModel: AppwriteViewModel,
    val adapter: CustomAdapter,
    val twitterFragment: TwitterFragment
) :
    TweetListener {

    //    protected

    var retweetTiming : String? = null

    fun getUserId(): String? {
        var curUserId: String? = null
        appwriteViewModel.user.observe(tweetList.context as LifecycleOwner) {
            if (it != null) {
                curUserId = it.id
            }
        }
        return curUserId
    }

    fun getUserName(): String? {
        var username: String? = null
        appwriteViewModel.user.observe(tweetList.context as LifecycleOwner) {
            if (it != null) {
                username = it.name
            }
        }
        return username
    }

    fun refreshUser() {
        val curUserId = getUserId()
        appwriteViewModel.getUser(curUserId)
        appwriteViewModel.userDoc.observe(tweetList.context as LifecycleOwner) { doc ->
            if (doc != null) {
                Log.e("refreshUser", "User document fetched: ${doc.data}")
                user = User(
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

    override fun onLayoutClick(tweet: Tweet?) {
        tweet?.let {
            tweetList.isClickable = false
            val users = it.userIds?.toMutableList()
            val selectedUserid = users?.get(0)
            val intent = Intent(tweetList.context, OtherProfileActivity::class.java)
            intent.putExtra("selectedUserid", selectedUserid)
            tweetList.context.startActivity(intent)
        }
    }

    override fun onLike(tweet: Tweet?, tweetId: String?, position: Int) {
        val curUserId = getUserId()
        if (curUserId == null || tweet == null) return

        val likesList = tweet.likes?.toMutableList() ?: mutableListOf()
        val isLiked = likesList.contains(curUserId)

        // Optimistic update
        if (isLiked) likesList.remove(curUserId) else likesList.add(curUserId)
        tweet.likes = likesList.toTypedArray()

        adapter.notifyItemChanged(position)

        // Backend update
        appwriteViewModel.updateUserData(
            arrayOf(),
            false,
            tweet.likes!!,
            true,
            arrayOf(),
            false,
            tweetId,
            onSuccess = {
                // No need to reload unless needed
            },
            onError = { error ->
                Log.e("like listener", "Error: ${error.message}")

                // Rollback optimistic update
                if (!isLiked) likesList.remove(curUserId) else likesList.add(curUserId)
                tweet.likes = likesList.toTypedArray()
                adapter.notifyItemChanged(position)
            }
        )
    }



    override fun onRetweet(tweet: Tweet?, tweetId: String?, position: Int) {

        val curUserId = getUserId()
        if (curUserId == null || tweet == null) return

        val retweetList = tweet.userIds?.toMutableList() ?: mutableListOf()
        val isRetweeted = retweetList.contains(curUserId)

        // Optimistic update
        if (isRetweeted) {
            retweetList.remove(curUserId)
        } else {
            retweetList.add(curUserId)
        }
        tweet.userIds = retweetList.toTypedArray()
        adapter.notifyItemChanged(position)

        appwriteViewModel.updateUserData(
            arrayOf(),
            false,
            arrayOf(),
            false,
            tweet.userIds!!,
            true,
            tweetId,
            onSuccess = { it ->
                tweetList.isClickable = true
                callback?.onRefresh()
            },
            onError = { error ->
                Log.e("retweet error", error.message ?: "Unknown error")

                // 🔁 Rollback
                if (!isRetweeted) retweetList.remove(curUserId) else retweetList.add(curUserId)
                tweet.userIds = retweetList.toTypedArray()
                adapter.notifyItemChanged(position)
            }
        )
    }

    override fun onDelete(tweet: Tweet?, tweetId: String?, position: Int) {
        if(tweet == null) return


            AlertDialog.Builder(tweetList.context)
                .setTitle("Delete Tweet")
                .setMessage("Are you sure you want to delete this tweet?")
                .setPositiveButton("Yes") { dialog, _ ->
                    adapter.removeItem(tweet)
                    appwriteViewModel.deleteTweet(
                        tweetId,
                        onSuccess = {
                            tweetList.isClickable = true
                            twitterFragment.updateList()
                            adapter.removeItem(tweet)
                            callback?.onRefresh()
                        },
                        onError = {})
                }
                .setNegativeButton("Cancel", null)
                .show()

    }

    override fun onComment(tweet: Tweet?, tweetId: String?) {
        val userId = getUserId()
        val userName = getUserName()
        val intent = Intent(tweetList.context, CommentActivity::class.java).apply {
            putExtra("tweet", tweet)
            putExtra("tweetId", tweetId)
            putExtra("userId", userId)
            putExtra("userName", userName)
        }
        tweetList.context.startActivity(intent)
    }

}