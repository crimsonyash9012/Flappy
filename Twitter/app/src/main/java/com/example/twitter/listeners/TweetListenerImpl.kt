package com.example.twitter.listeners

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.twitter.activity.OtherProfileActivity
import com.example.twitter.adapters.CustomAdapter
import com.example.twitter.fragments.SearchFragment
import com.example.twitter.fragments.TwitterFragment
import com.example.twitter.mvvm.AppwriteViewModel
import com.example.twitter.utils.Tweet
import com.example.twitter.utils.User
import org.koin.androidx.viewmodel.ext.android.viewModel

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

        /*
        val curUserId = getUserId()
        Log.e("curUserId: ", "curUserId: "  + curUserId.toString())
        refreshUser()
        tweet?.let {
            val owner = tweet.userIds?.get(0)
            Log.e("curUserId", "owner: " + owner.toString())
            if (owner != curUserId) {
                Log.e("curUserId", "list: " + user?.followUsers?.contentToString())
                if (user?.followUsers?.toMutableList()?.contains(owner) == true) {
                    AlertDialog.Builder(tweetList.context)
                        .setTitle("Unfollow ${tweet.username}")
                        .setPositiveButton("Yes") { dialog, _ ->
                            tweetList.isClickable = false
                            var followedUsers = user?.followUsers
                            var list = followedUsers?.toMutableList()
                            if (list == null) list = mutableListOf()
                            list.remove(owner)
                            followedUsers = list.toTypedArray()
                            appwriteViewModel.updateFollowing(
                                curUserId!!,
                                followedUsers,
                                onSuccess = { it ->
                                    tweetList.isClickable = true
                                    callback?.onUserUpdated()
                                },
                                onError = { it ->
                                    tweetList.isClickable = true
                                }
                            )
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> }
                        .show()
                } else {
                    AlertDialog.Builder(tweetList.context)
                        .setTitle("Follow ${tweet.username}")
                        .setPositiveButton("Yes") { dialog, _ ->
                            tweetList.isClickable = false
                            var followedUsers = user?.followUsers
                            var list = followedUsers?.toMutableList()
                            if (list == null) list = mutableListOf()
                            owner?.let{
                                list?.add(owner)
                                followedUsers = list?.toTypedArray()
                                appwriteViewModel.updateFollowing(
                                    curUserId!!,
                                    followedUsers!!,
                                    onSuccess = { it ->
                                        tweetList.isClickable = true
                                        callback?.onUserUpdated()
                                    },
                                    onError = { it ->
                                        tweetList.isClickable = true
                                    }
                                )
                            }

                        }
                        .setNegativeButton("Cancel") { dialog, _ -> }
                        .show()
                }
            }
        }
         */
    }

    override fun onLike(tweet: Tweet?, tweetId: String?) {
        val curUserId = getUserId()
        tweet?.let {
            tweetList.isClickable = false
            val liked = it.likes?.toMutableList()
            if (liked!!.contains(curUserId.toString())) {
                liked.remove(curUserId.toString())
            } else {
                liked.add(curUserId!!)
            }
            val likes = liked.toTypedArray()
            appwriteViewModel.updateUserData(
                arrayOf(),
                false,
                likes,
                true,
                arrayOf(),
                false,
                tweetId,
                onSuccess = { it ->
                    tweetList.isClickable = true
                    callback?.onRefresh()
                },
                onError = { it ->
                    Log.e("like listener", it.message.toString())
                }
            )
        }
    }

    override fun onRetweet(tweet: Tweet?, tweetId: String?) {
        val curUserId = getUserId()
        tweet?.let {
            tweetList.isClickable = false
            val retweet = it.userIds?.toMutableList()
            if (retweet!!.contains(curUserId.toString())) {
                retweet.remove(curUserId.toString())
                appwriteViewModel.deleteRetweet(tweet.tweetId, curUserId)
            } else {
                retweet.add(curUserId!!)
                appwriteViewModel.addRetweet(tweet.tweetId, curUserId)
            }
            val retweets = retweet.toTypedArray()
            Log.e("retweets listener: ", retweets.contentToString())
            appwriteViewModel.updateUserData(
                arrayOf(),
                false,
                arrayOf(),
                false,
                retweets,
                true,
                tweetId,
                onSuccess = { it ->
                    tweetList.isClickable = true
                    callback?.onRefresh()
                },
                onError = { it ->
                    Log.e("listener impl", it.message.toString())
                }
            )
        }
    }

    override fun onDelete(tweet: Tweet?, tweetId: String?) {
        tweet?.let{
            tweetList.isClickable = false

            AlertDialog.Builder(tweetList.context)
                .setTitle("Delete Tweet")
                .setPositiveButton("Yes") { dialog, _ ->
                    appwriteViewModel.deleteTweet(
                        tweetId,
                        onSuccess = {
                            tweetList.isClickable = true
                            twitterFragment.updateList()
                            callback?.onRefresh()
                        },
                        onError = {})
                }
                .setNegativeButton("Cancel") { dialog, _ -> }
                .show()
        }
    }
}