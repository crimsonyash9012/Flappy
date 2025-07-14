package com.example.flappy.utils

import java.io.Serializable

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
    var userIds: Array<String>? = arrayOf(),
    val username: String? = "",
    val text: String? = "",
    val imageUrl: String? = "",
    val timestamp: String? = "0",
    val hashtags: Array<String>? = arrayOf(),
    var likes: Array<String>? = arrayOf()
) : Serializable

data class Retweet(
    val tweet_id: String? = "",
    val user_id: String? = "",
    val timestamp: String? = "0"
)


data class Comment(
    val tweetId: String?,
    val userId: String,
    val username: String,
    val text: String,
    val timestamp: String,
    val replyingTo: String?
)

data class Reply(
    val username: String,
    val profileImageUrl: String,
    val timeAgo: String,
    val replyText: String,
    val timestamp: Long
)
