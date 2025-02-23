package com.example.twitter.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.twitter.R
import com.example.twitter.listeners.TweetListener
import com.example.twitter.mvvm.AppwriteViewModel
import com.example.twitter.mvvm.appwriteModule
import com.example.twitter.utils.Tweet
import com.example.twitter.utils.getDate
import com.example.twitter.utils.loadUrl
import org.koin.androidx.viewmodel.ext.android.viewModel

class CustomAdapter(
    private var userId: String,
    private var items: MutableList<Tweet>
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var listener: TweetListener? = null
    fun setListener(listener: TweetListener?) {
        this.listener = listener
        Log.e("Adapter user Id", userId)
    }

    fun updateUserId(newUserId: String) {
        userId = newUserId
        notifyDataSetChanged()
    }

    // ViewHolder class to bind views
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val layout = v.findViewById<ViewGroup>(R.id.tweetLayout)
        private val username = v.findViewById<TextView>(R.id.tweetUsername)
        private val text = v.findViewById<TextView>(R.id.tweetText)
        private val image = v.findViewById<ImageView>(R.id.tweetImage)
        private val date = v.findViewById<TextView>(R.id.tweetDate)
        private val like = v.findViewById<ImageView>(R.id.tweetLike)
        private val likeCount = v.findViewById<TextView>(R.id.tweetLikeCount)
        private val retweet = v.findViewById<ImageView>(R.id.tweetRetweet)
        private val retweetCount = v.findViewById<TextView>(R.id.tweetRetweetCount)
        private val delete = v.findViewById<ImageView>(R.id.delete_tweet)

        fun bind(
            userId: String,
            tweet: Tweet,
            listener: TweetListener?,
        ) {
            username.text = tweet.username
            text.text = tweet.text
//            Log.e("bind", tweet.likes.contentToString())
            if (tweet.imageUrl.isNullOrEmpty()) image.visibility = View.GONE
            else {
                image.visibility = View.VISIBLE
                image.loadUrl(tweet.imageUrl)
//                Log.e("bind", tweet.imageUrl.toString())
            }
            date.text = getDate(tweet.timestamp?.toLong())
//            Log.e("bind", tweet.timestamp.toString())
            likeCount.text = tweet.likes?.size.toString()
            retweetCount.text = tweet.userIds?.size?.minus(1).toString()

            layout.setOnClickListener { listener?.onLayoutClick(tweet) }
            like.setOnClickListener { listener?.onLike(tweet, tweet.tweetId) }
            retweet.setOnClickListener { listener?.onRetweet(tweet, tweet.tweetId) }
            delete.setOnClickListener { listener?.onDelete(tweet, tweet.tweetId) }

            Log.e("bind", "Retweets" + tweet.userIds.contentToString())
            Log.e("bind", "User Id: " + userId)

            if (tweet.likes?.contains(userId) == true) {
                like.setImageResource(R.drawable.like)
            } else {
                like.setImageResource(R.drawable.like_inactive)
            }
            if (tweet.userIds?.get(0).equals(userId)) {
                retweet.setImageResource(R.drawable.original)
                retweet.isClickable = false
                delete.visibility = View.VISIBLE
            } else if (tweet.userIds?.contains(userId) == true) {
                retweet.setImageResource(R.drawable.retweet)
                delete.visibility = View.GONE
            } else {
                retweet.setImageResource(R.drawable.retweet_inactive)
                delete.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tweet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userId, items[position], /*onItemClicked*/ listener)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Tweet>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

}