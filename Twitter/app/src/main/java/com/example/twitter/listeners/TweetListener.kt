package com.example.twitter.listeners

import com.example.twitter.utils.Tweet

interface TweetListener {
    fun onLayoutClick(tweet: Tweet?)
    fun onLike(tweet: Tweet?, tweetId: String?)
    fun onRetweet(tweet: Tweet?, tweetId: String?)
    fun onDelete(tweet: Tweet?, tweetId: String?)
}