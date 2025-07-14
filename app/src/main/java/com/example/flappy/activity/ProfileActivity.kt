package com.example.flappy.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.flappy.R
import com.example.flappy.utils.Constant
import com.example.flappy.databinding.ActivityProfileBinding
import com.example.flappy.mvvm.AppwriteViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileActivity : AppCompatActivity() {

    private val profileBinding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(
            layoutInflater
        )
    }
    private val appwriteViewModel: AppwriteViewModel by viewModel()
    private var userId: String? = null
    private val homeActivity = HomeActivity()
//    private val itemImageBinding : ItemImageBinding by lazy { ItemImageBinding.inflate(layoutInflater)}
//    private val itemEmailBinding : ItemEmailBinding by lazy { ItemEmailBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(profileBinding.root)

        appwriteViewModel.user.observe(this){
//            if(it!=null){
//                userId = it.id
//            }
            if(it==null){
                onResume()
            }
        }

        profileBinding.profileProgressLayout.setOnTouchListener { view, motionEvent -> true }

        populateInfo()

        profileBinding.itemImage.profileImgProf.setOnClickListener {
            try {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(intent, Constant.REQUEST_CODE_PHOTO)
            } catch (e: Exception) {
                Log.e("profile", e.message.toString())
            }
        }

        profileBinding.imgBack.setOnClickListener {
            finish()
        }

        profileBinding.imgLogout.setOnClickListener {
            try {
                appwriteViewModel.logout(
                    onSuccess = {user_id ->
                        navigateToLogin()
                    },
                    onError = {e ->
                        Log.e("profile logout", e.message.toString())
                    }

                )
            } catch (e: Exception) {
                Log.e("profile logout", e.message.toString())
            }
        }

        profileBinding.imgEdit.setOnClickListener {
            val userData = appwriteViewModel.userDoc.value

            userData?.let {
                val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
                intent.putExtra("username", it.data["username"].toString())
                intent.putExtra("mobile", it.data["mobile"].toString())
                intent.putExtra("about", it.data["about"].toString())
                startActivity(intent)
            }
        }


        /*
        appwriteViewModel.imageUrl.observe(this) {
            if (it != null) {
                Glide.with(this)
                    .load(it)
                    .into(profileBinding.itemImage.profileImgProf)
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                profileBinding.profileProgressLayout.visibility = View.GONE
            }
        }
         */
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
        profileBinding.profileProgressLayout.visibility = View.VISIBLE
        val user = appwriteViewModel.getUser(userId)

        appwriteViewModel.getCurrentUserFollowers(
            onSuccess = {
                appwriteViewModel.getCurUserFollowers.observe(this){
                    val size = it?.documents?.size
                    profileBinding.itemImage.tvFollowers.text = size.toString()
                    if(size!=null && size>=10){
                        appwriteViewModel.verifyUser(true)
                    }
                    else{
                        appwriteViewModel.verifyUser(false)
                    }
                }
            },
            onError = {e ->
                Log.e("populate profile", e.message.toString())
            }
        )

        appwriteViewModel.userDoc.observe(this) {
            if (it != null) {
                try {
                    profileBinding.itemImage.name.text = it.data["name"].toString()
                    profileBinding.itemEmail.profEmail.text = it.data["email"].toString()
                    Log.e("url profile", it.data["imageUrl"].toString())
                    Glide.with(this)
                        .load(it.data["imageUrl"].toString())
                        .into(profileBinding.itemImage.profileImgProf)


                    val about = it.data["about"].toString()
                    val phone = it.data["mobile"].toString()
                    val username = it.data["username"].toString()
                    profileBinding.itemAdd.addBio.text =
                        if (about.isBlank()) getString(R.string.default_about) else about
                    profileBinding.itemInfo.infoPhone.text =
                        if (phone.isBlank()) getString(R.string.default_about) else phone

                    profileBinding.itemImage.username.text =
                        if (username.startsWith("@")) username else "@$username"

                    profileBinding.itemImage.tvFollowing.text =
                        (it.data["followUsers"] as? List<*>)?.size?.toString() ?: "0"


                    appwriteViewModel.getCurrentUserTweet(
                        userId,
                        onSuccess = {
                            appwriteViewModel.getCurUserTweet.observe(this){
                                val size = it?.documents?.size
                                profileBinding.itemImage.tvPosts.text = size.toString()
                            }
                        },
                        onError = {e ->
                            Log.e("populate profile", e.message.toString())
                        }
                    )

                    val isVerified = it.data["isVerified"] as Boolean
                    if(isVerified){
                        profileBinding.itemAdd.tvVerify.text = "SKY"
                        profileBinding.itemAdd.verifyTick.setImageResource(R.drawable.verified_badge)
                    }
                    else{
                        profileBinding.itemAdd.tvVerify.text = "SURFACE"
                        profileBinding.itemAdd.verifyTick.setImageResource(R.drawable.unverified_badge_)
                    }

                    profileBinding.profileProgressLayout.visibility = View.GONE
                }
                catch (e:Exception){
                    profileBinding.profileProgressLayout.visibility = View.GONE
                    Log.e("populate", e.message.toString())
                }
            } else finish()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constant.REQUEST_CODE_PHOTO) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                // Show preview instantly
                Glide.with(this)
                    .load(selectedImageUri)
                    .into(profileBinding.itemImage.profileImgProf)

                appwriteViewModel.storeImage(data?.data)
//                appwriteViewModel.imageUrl.observe(this) {
//                    if (it != null) {
//                        Log.e("profile", "Image URL: $it")
//                        Glide.with(this)
//                            .load(it)
//                            .into(profileBinding.itemImage.profileImgProf)
//                    }
//                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}