package com.example.flappy.activity


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import com.example.flappy.R
import com.example.flappy.items.CommentList
import com.example.flappy.mvvm.AppwriteRepository
import com.example.flappy.mvvm.AppwriteViewModel
import com.example.flappy.utils.Comment
import com.example.flappy.utils.Tweet
import com.example.flappy.utils.getDate
import com.example.flappy.utils.loadUrl

class CommentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val appwriteRepository = AppwriteRepository(context = this)
        val appwriteViewModel = AppwriteViewModel(appwriteRepository)

        val tweetId = intent.getStringExtra("tweetId")
        val tweet = intent.getSerializableExtra("tweet") as? Tweet
        val currentUserId = intent.getStringExtra("userId")
        var currentUsername: String? = intent.getStringExtra("userName")

        val root = findViewById<View>(R.id.tweetLayout)
        val addComment = findViewById<ImageView>(R.id.addComment)
        val composeView = findViewById<ComposeView>(R.id.commentComposeView)

        appwriteViewModel.getUser(null)
        appwriteViewModel.loadComments(tweetId!!)

        // Set tweet content
        val username = root.findViewById<TextView>(R.id.tweetUsername)
        val text = root.findViewById<TextView>(R.id.tweetText)
        val date = root.findViewById<TextView>(R.id.tweetDate)
        val image = root.findViewById<ImageView>(R.id.tweetImage)
        val likeCount = root.findViewById<TextView>(R.id.tweetLikeCount)
        val retweetCount = root.findViewById<TextView>(R.id.tweetRetweetCount)
        val commentCount = root.findViewById<TextView>(R.id.tweetCommentCount)

        tweet?.let {
            username.text = it.username
            text.text = it.text
            date.text = getDate(it.timestamp?.toLong() ?: 0L)
            likeCount.text = it.likes?.size.toString()
            retweetCount.text = it.userIds?.size?.minus(1).toString()
            commentCount.text = "0"
            if (it.imageUrl.isNullOrEmpty()) {
                image.visibility = View.GONE
            } else {
                image.visibility = View.VISIBLE
                image.loadUrl(it.imageUrl)
            }
        }

        // Show comment dialog
        addComment.setOnClickListener {
            if (currentUserId != null && !currentUsername.isNullOrEmpty()) {
                showCommentDialog(tweetId, currentUserId, currentUsername!!, appwriteViewModel)
            } else {
                Log.e("comment", "User not loaded yet")
            }
        }

        // Set ComposeView content
        composeView.setContent {
            MaterialTheme {
                val commentList by appwriteViewModel.comments.collectAsState()
                CommentList(
                    comments = commentList
                )
            }
        }
    }

    private fun showCommentDialog(
        tweetId: String?,
        currentUserId: String,
        username: String?,
        viewModel: AppwriteViewModel
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.comment_dialog, null)
        val commentEditText = dialogView.findViewById<EditText>(R.id.commentEditText)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val postButton = dialogView.findViewById<Button>(R.id.postButton)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Comment")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        postButton.setOnClickListener {
            val commentText = commentEditText.text.toString().trim()
            if (commentText.isEmpty()) {
                commentEditText.error = "Can't be empty"
            } else {
                val comment = Comment(
                    tweetId = tweetId,
                    userId = currentUserId,
                    username = username!!,
                    text = commentText,
                    timestamp = System.currentTimeMillis().toString(),
                    replyingTo = null
                )
                viewModel.postComment(comment)
                viewModel.loadComments(tweetId!!)

                dialog.dismiss()
            }
        }
        dialog.show()
    }
}
