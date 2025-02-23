package com.example.twitter.utils

data class User(
    val user_Id: String? = "",
    val email:String? = "",
    val name:String? = "",
    val imageUrl: String? = "",
    val followHashtags: Array<String>? = arrayOf(),
    val followUsers: Array<String>? = arrayOf()
)

data class Tweet(
    val tweetId: String? = "",
    val userIds: Array<String>? = arrayOf(),
    val username: String? = "",
    val text: String? = "",
    val imageUrl: String? = "",
    val timestamp: String? = "0",
    val hashtags: Array<String>? = arrayOf(),
    val likes: Array<String>? = arrayOf()
)

data class Retweet(
    val tweet_id: String? = "",
    val user_id: String? = "",
    val timestamp: String? = "0"
)
