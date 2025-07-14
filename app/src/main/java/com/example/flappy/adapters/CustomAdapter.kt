package com.example.flappy.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flappy.R
import com.example.flappy.listeners.TweetListener
import com.example.flappy.utils.Tweet
import com.example.flappy.utils.getDate
import com.example.flappy.utils.loadUrl

class CustomAdapter(
    private var userId: String,
    private var items: MutableList<Tweet>
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var listener: TweetListener? = null
    private var commentCountMap: Map<String, Int> = emptyMap()

    fun setListener(listener: TweetListener?) {
        this.listener = listener
        Log.e("Adapter user Id", userId)
    }

    fun updateUserId(newUserId: String) {
        userId = newUserId
        notifyDataSetChanged()
    }

    fun setCommentCounts(counts: Map<String, Int>) {
        commentCountMap = counts
        notifyDataSetChanged()
    }

    fun getTweetPosition(tweet: Tweet): Int {
        return items.indexOfFirst { it.tweetId == tweet.tweetId }
    }


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
        private val comment = v.findViewById<ImageView>(R.id.tweetComment)
        private val commentCount = v.findViewById<TextView>(R.id.tweetCommentCount)
        private val delete = v.findViewById<ImageView>(R.id.delete_tweet)

        fun bind(
            userId: String,
            tweet: Tweet,
            listener: TweetListener?,
            commentCounts: Map<String, Int>
        ) {
            username.text = tweet.username
            text.text = tweet.text

            if (tweet.imageUrl.isNullOrEmpty()) {
                image.visibility = View.GONE
            } else {
                image.visibility = View.VISIBLE
                image.loadUrl(tweet.imageUrl)

                val scaleGestureDetector = ScaleGestureDetector(image.context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val scaleFactor = detector.scaleFactor
                        image.scaleX *= scaleFactor
                        image.scaleY *= scaleFactor
                        return true
                    }

                    override fun onScaleEnd(detector: ScaleGestureDetector) {
                        image.animate()
                            .scaleX(15f)
                            .scaleY(15f)
                            .setDuration(250)
                            .start()
                    }
                })

                image.setOnTouchListener { _, event ->
                    scaleGestureDetector.onTouchEvent(event)
                    true
                }
            }

            date.text = getDate(tweet.timestamp?.toLong())
            likeCount.text = tweet.likes?.size.toString()
            retweetCount.text = tweet.userIds?.size?.minus(1).toString()

            commentCount.text = (commentCounts[tweet.tweetId] ?: 0).toString()

            layout.setOnClickListener { listener?.onLayoutClick(tweet) }
            like.setOnClickListener { listener?.onLike(tweet, tweet.tweetId, adapterPosition) }
            retweet.setOnClickListener { listener?.onRetweet(tweet, tweet.tweetId, adapterPosition) }
            delete.setOnClickListener { listener?.onDelete(tweet, tweet.tweetId,adapterPosition) }
            comment.setOnClickListener { listener?.onComment(tweet, tweet.tweetId) }


            if (tweet.likes?.contains(userId) == true) {
                like.setImageResource(R.drawable.like)
            } else {
                like.setImageResource(R.drawable.like_inactive)
            }

            if (tweet.userIds?.get(0) == userId) {
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
        holder.bind(userId, items[position], listener, commentCountMap)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Tweet>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItems(more: List<Tweet>) {
        val positionStart = items.size
        items.addAll(more)
        notifyItemRangeInserted(positionStart, more.size)
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun removeItem(tweet: Tweet) {
        val index = items.indexOfFirst { it.tweetId == tweet.tweetId }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

}
