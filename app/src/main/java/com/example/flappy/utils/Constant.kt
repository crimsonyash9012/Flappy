package com.example.flappy.utils

class Constant {
    companion object{
        const val END_POINT = "https://cloud.appwrite.io/v1"
        const val PROJECT_ID = "67738b94001e97d033c2"
        const val DATABASE_ID = "677cc8a4003d6593311a"
        const val COLLECTION_TWEET_ID = "677e9701001f4ca8dcc3"
        const val COLLECTION_USER_ID = "678200db003d035d0bc0"
        const val COLLECTION_RETWEET_ID = "6790f90c00068874723c"
        const val COLLECTION_COMMENTS_ID = "6873ac1a0015fb3ffc4e"

        const val REQUEST_CODE_PHOTO = 345
        const val BUCKET_ID_PROF_IMAGE = "677d079500174e5e5973"
        const val BUCKET_ID_TWEET_IMAGE = "6782cb51003801987023"
        const val DEFAULT_IMAGE_URL = "https://cloud.appwrite.io/v1/storage/buckets/677d079500174e5e5973/files/67a728ad00370e3c41c5/view?project=67738b94001e97d033c2&mode=admin"


        const val DATA_IMAGES = "ProfileImages"
        const val DATA_TWEETS = "Tweets"
        const val DATA_TWEET_USER_IDS = "userIds"
        const val DATA_FOLLOW_USERS = "followUsers"
        const val DATA_TWEET_HASHTAGS = "hashtags"
        const val DATA_TWEET_USERNAME = "name"
        const val DATA_TWEET_LIKES = "likes"
        const val DATA_TWEET_IMAGES = "TweetImages"
//        const val DATA_TWEET_TEXT = "text"
        const val DATA_TWEET_TIMESTAMP = "timestamp"
        const val DATA_TWEET_ID = "tweet_id"
        const val DATA_TWEET_ID_DEL = "tweetId"
        const val DATA_USER_ID = "user_id"

        const val ERROR_INVALID_EMAIL = "Value must be a valid email address"
        const val ERROR_INVALID_PASS = "Password must be between 8 and 256 characters long"
        const val ERROR_INVALID_CRED = "Invalid credentials"
        const val ERROR_SAME_CRED = "A user with the same id"
        const val ERROR_TRY_AGAIN = "Please try again after some time"

    }
}