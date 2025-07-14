package com.example.flappy.listeners

import com.example.flappy.utils.Tweet

interface TweetListener {
    fun onLayoutClick(tweet: Tweet?)
    fun onLike(tweet: Tweet?, tweetId: String?, position: Int)
    fun onRetweet(tweet: Tweet?, tweetId: String?, position: Int)
    fun onDelete(tweet: Tweet?, tweetId: String?, position: Int)
    fun onComment(tweet: Tweet?, tweetId: String?)
}
