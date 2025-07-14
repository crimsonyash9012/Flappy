package com.example.flappy.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.flappy.databinding.ActivityTweetBinding
import com.example.flappy.mvvm.AppwriteViewModel
import com.example.flappy.utils.Constant
import com.example.flappy.utils.Tweet
import com.example.flappy.utils.isImageSizeValid
import io.appwrite.ID
import io.appwrite.models.Document
import org.koin.androidx.viewmodel.ext.android.viewModel

class TweetActivity : AppCompatActivity() {
    private val tweetBinding: ActivityTweetBinding by lazy {
        ActivityTweetBinding.inflate(
            layoutInflater
        )
    }
    private var imageUrl: String? = null
    private var userId: String? = null
    private var userName: String? = null
    private var tweetId: String? = null
    private val appwriteViewModel: AppwriteViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(tweetBinding.root)

        if (intent.hasExtra(PARAM_USER_ID) && intent.hasExtra(PARAM_USER_NAME)) {
            userId = intent.getStringExtra(PARAM_USER_ID)
            userName = intent.getStringExtra(PARAM_USER_NAME)
        } else {
            Toast.makeText(
                this,
                "Error creating Tweet. Please Check your user settings.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
        tweetBinding.tweetProgressLayout.setOnTouchListener { view, motionEvent -> true }
    }

    fun addImage(v: View) {
        try {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, Constant.REQUEST_CODE_PHOTO)
        } catch (e: Exception) {
            Log.e("tweet", e.message.toString())
        }
    }

    fun postTweet(v: View) {
        try {
            appwriteViewModel.userDoc.observe(this, object : Observer<Document<Map<String, Any>>?> {
                override fun onChanged(it: Document<Map<String, Any>>?) {
                    appwriteViewModel.userDoc.removeObserver(this) // Observe only once

                    if (it == null) return

                    Toast.makeText(this@TweetActivity, "Posting Tweet", Toast.LENGTH_SHORT).show()
                    val text = tweetBinding.tweetText.text.toString()
                    val hashtags = getHashtags(text)
                    tweetId = ID.unique()

                    val url = appwriteViewModel.imageUrlTweet.value
                    if (!url.isNullOrEmpty()) imageUrl = url
                    else imageUrl = null

                    tweetBinding.tweetProgressLayout.visibility = View.VISIBLE

                    val tweet = Tweet(
                        tweetId,
                        arrayOf(userId!!),
                        it.data["name"].toString(),
                        text,
                        imageUrl,
                        System.currentTimeMillis().toString(),
                        hashtags,
                        arrayOf()
                    )

                    appwriteViewModel.postTweet(tweet)

                    // 🧹 Clear LiveData AFTER posting and closing
                    appwriteViewModel.imageUrlTweet.value = null
                    imageUrl = null

                    finish()
                }
            })
        } catch (e: Exception) {
            Log.e("TweetActivity", e.toString())
        }
    }


    fun getHashtags(source: String): Array<String> {
        val hashtags = mutableListOf<String>()
        var text: String = source

        while (text.contains("#")) {
            var hashtag = ""
            val hash = text.indexOf("#")
            text = text.substring(hash + 1)

            val firstSpace = text.indexOf(" ")
            val firstHash = text.indexOf("#")

            if (firstSpace == -1 && firstHash == -1) {
                hashtag = text.substring(0)
            } else if (firstSpace != -1 && firstSpace < firstHash) {
                hashtag = text.substring(0, firstSpace)
                text = text.substring(firstSpace + 1)
            } else {
                hashtag = text.substring(0, firstHash)
                text = text.substring(firstHash)
            }
            if (hashtag.isNotEmpty()) {
                hashtags.add(hashtag)
            }
        }
        return hashtags.toTypedArray()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constant.REQUEST_CODE_PHOTO) {
            val imageUri = data?.data
            if (isImageSizeValid(imageUri!!, tweetBinding.tweetImage)) {
                appwriteViewModel.storeImageTweet(data?.data)
                appwriteViewModel.imageUrlTweet.observe(this) {
                    if (it != null) {
                        Log.e("tweet", "Image URL: $it")
                        tweetBinding.tweetProgressLayout.visibility = View.VISIBLE
                        Glide.with(this)
                            .load(it)
                            .into(tweetBinding.tweetImage)
                        tweetBinding.tweetProgressLayout.visibility = View.GONE
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Please select the image size less than 3MB",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    companion object {
        var PARAM_USER_ID = "UserId"
        var PARAM_USER_NAME = "UserName"

        fun newIntent(context: Context, userId: String?, userName: String?): Intent {
            val intent = Intent(context, TweetActivity::class.java)
            intent.putExtra(PARAM_USER_ID, userId)
            intent.putExtra(PARAM_USER_NAME, userName)
            return intent
        }
    }
}