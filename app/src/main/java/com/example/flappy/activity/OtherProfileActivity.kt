package com.example.flappy.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.flappy.R
import com.example.flappy.databinding.ActivityOtherProfileBinding
import androidx.appcompat.app.AlertDialog
import com.example.flappy.mvvm.AppwriteViewModel
import com.example.flappy.utils.Constant
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtherProfileActivity : AppCompatActivity() {

    lateinit var userId: String
    private val appwriteViewModel: AppwriteViewModel by viewModel()
    private val otherBinding: ActivityOtherProfileBinding by lazy {
        ActivityOtherProfileBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(otherBinding.root)

        userId = intent.getStringExtra("selectedUserid").toString()
        otherBinding.otherProfileProgressLayout.setOnTouchListener { view, motionEvent -> true }

        populateInfo()

        otherBinding.otherImgBack.setOnClickListener {
            finish()
        }

        otherBinding.otherImgFollow.setOnClickListener {
            val owner = userId
            Log.e("curUserId", "owner: " + owner.toString())

            val userDoc = appwriteViewModel.userDoc.value
            var followedUsers =
                (userDoc!!.data[Constant.DATA_FOLLOW_USERS] as? List<String>)?.toTypedArray()
            Log.e("other bind dialog", userDoc.data.toString())
            if (followedUsers?.contains(owner) == true) {
                otherBinding.otherImgFollow.setImageResource(R.drawable.baseline_remove_circle_outline_24)
                appwriteViewModel.selectedUserDoc.observe(this) {
                    AlertDialog.Builder(this)
                        .setTitle("Unfollow ${it!!.data[Constant.DATA_TWEET_USERNAME]}")
                        .setPositiveButton("Yes") { dialog, _ ->
                            var list = followedUsers?.toMutableList()
                            if (list == null) list = mutableListOf()
                            list.remove(owner)
                            followedUsers = list.toTypedArray()
                            appwriteViewModel.updateFollowing(
                                userId!!,
                                followedUsers!!,
                                onSuccess = {
                                    appwriteViewModel.getUser("") // Refresh logged-in user info
                                    appwriteViewModel.getSelectedUser(userId) // Refresh logged-in user document
                                    updateFollowIcon() // Optional: update icon immediately
                                },
                                onError = { e ->
                                    Log.e("Follow Error", e.message.toString())
                                }
                            )

                        }
                        .setNegativeButton("Cancel") { dialog, _ -> }
                        .show()
                }
            }
            else{
                otherBinding.otherImgFollow.setImageResource(R.drawable.add_circle)
                appwriteViewModel.selectedUserDoc.observe(this) {
                    AlertDialog.Builder(this)
                        .setTitle("Follow ${it!!.data[Constant.DATA_TWEET_USERNAME]}")
                        .setPositiveButton("Yes") { dialog, _ ->
                            var list = followedUsers?.toMutableList()
                            if (list == null) list = mutableListOf()
                            owner?.let {
                                list?.add(owner)
                                followedUsers = list?.toTypedArray()
                                appwriteViewModel.updateFollowing(
                                    userId!!,
                                    followedUsers!!,
                                    onSuccess = {
                                        appwriteViewModel.getUser("") // Refresh logged-in user info
                                        appwriteViewModel.getSelectedUser(userId) // Refresh logged-in user document
                                        updateFollowIcon() // Optional: update icon immediately
                                    },
                                    onError = { e ->
                                        Log.e("Follow Error", e.message.toString())
                                    }
                                )
                            }

                        }
                        .setNegativeButton("Cancel") { dialog, _ -> }
                        .show()
                }
            }
        }
    }

    private fun updateFollowIcon() {
        val owner = userId
        val userDoc = appwriteViewModel.userDoc.value
        val followedUsers = (userDoc?.data?.get(Constant.DATA_FOLLOW_USERS) as? List<String>)?.toTypedArray()

        if (followedUsers?.contains(owner) == true) {
            otherBinding.otherImgFollow.setImageResource(R.drawable.baseline_remove_circle_outline_24)
        } else {
            otherBinding.otherImgFollow.setImageResource(R.drawable.add_circle)
        }
    }


    override fun onResume() {
        super.onResume()
        appwriteViewModel.user.observe(this) {
            if (it == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun populateInfo() {
        otherBinding.otherProfileProgressLayout.visibility = View.VISIBLE
        val user = appwriteViewModel.getSelectedUser(userId)
        appwriteViewModel.getUser(userId)

        appwriteViewModel.userDoc.removeObservers(this)
        appwriteViewModel.selectedUserDoc.observe(this) {
            if (it != null) {
                try {
//                    val followHashtags = (it.data["followHashtags"] as? ArrayList<String>)?.toTypedArray()
//                    val followUsers = (it.data["followUsers"] as? ArrayList<String>)?.toTypedArray()
                    Log.e("profile", it.data.toString())
                    otherBinding.otherItemImage.name.text = it.data["name"].toString()
                    otherBinding.otherItemEmail.profEmail.text = it.data["email"].toString()
                    Log.e("url profile", it.data["imageUrl"].toString())
                    Glide.with(this)
                        .load(it.data["imageUrl"].toString())
                        .into(otherBinding.otherItemImage.profileImgProf)


                    val about = it.data["about"].toString()
                    val phone = it.data["mobile"].toString()
                    val username = it.data["username"].toString()
                    if (about == null || about == "") {
                        otherBinding.otherItemAdd.addBio.text = getString(R.string.default_about)
                    } else {
                        otherBinding.otherItemAdd.addBio.text = about
                    }
                    if (phone == null || phone == "") {
                        otherBinding.otherItemInfo.infoPhone.text =
                            getString(R.string.default_about)
                    } else {
                        otherBinding.otherItemInfo.infoPhone.text = phone
                    }
                    if (username.toCharArray()[0] == '@') {
                        otherBinding.otherItemImage.username.text = username
                    } else {
                        otherBinding.otherItemImage.username.text = "@${username}"
                    }

                    otherBinding.otherItemImage.tvFollowing.text =
                        (it.data["followUsers"] as List<String>)?.toTypedArray()?.size.toString()

                    appwriteViewModel.getSelectedUserTweet(
                        userId,
                        onSuccess = {
                            Log.e("post size", "on success for post")
                            appwriteViewModel.getSelectedUserTweet.observe(this) {
                                val size = it?.documents?.size
                                otherBinding.otherItemImage.tvPosts.text = size.toString()
                                Log.e("post size", size.toString())
                            }
                        },
                        onError = { e ->
                            Log.e("populate profile", e.message.toString())
                        }
                    )

                    appwriteViewModel.getSelectedUserFollowers(
                        userId,
                        onSuccess = {
                            appwriteViewModel.getSelectedUserFollowers.observe(this) {
                                val size = it?.documents?.size
                                otherBinding.otherItemImage.tvFollowers.text = size.toString()
                            }
                        },
                        onError = { e ->
                            Log.e("populate profile", e.message.toString())
                        }
                    )

                    val isVerified = it.data["isVerified"] as Boolean
                    if (isVerified) {
                        otherBinding.otherItemAdd.tvVerify.text = "SKY"
                        otherBinding.otherItemAdd.verifyTick.setImageResource(R.drawable.verified_badge)
                    } else {
                        otherBinding.otherItemAdd.tvVerify.text = "SURFACE"
                        otherBinding.otherItemAdd.verifyTick.setImageResource(R.drawable.unverified_badge_)
                    }
                    otherBinding.otherProfileProgressLayout.visibility = View.GONE
                }
                catch (e: Exception) {
                    otherBinding.otherProfileProgressLayout.visibility = View.GONE
                    Log.e("populate", e.message.toString())
                }
            } else finish()
        }

        appwriteViewModel.userDoc.observe(this) { user ->
            var followedUsers =
                (user!!.data[Constant.DATA_FOLLOW_USERS] as? List<String>)?.toTypedArray()
            Log.e("other bind dialog", user.data.toString())
            if (followedUsers?.contains(userId) == true) {
                otherBinding.otherImgFollow.setImageResource(R.drawable.baseline_remove_circle_outline_24)
            } else {
                otherBinding.otherImgFollow.setImageResource(R.drawable.add_circle)
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}