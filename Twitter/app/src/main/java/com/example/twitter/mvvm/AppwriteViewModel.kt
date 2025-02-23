package com.example.twitter.mvvm

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twitter.databinding.ActivityProfileBinding
import com.example.twitter.utils.SingleLiveEvent
import com.example.twitter.utils.Tweet
import com.example.twitter.utils.User
import io.appwrite.models.Document
import kotlinx.coroutines.launch

class AppwriteViewModel(private val appwriteRepository: AppwriteRepository) : ViewModel() {

    val user = appwriteRepository.user
    val userDoc = appwriteRepository.userDoc
    val session = appwriteRepository.session
    val imageUrl = appwriteRepository.imageUrl
    val imageUrlTweet = appwriteRepository.imageUrlTweet
    val userId = appwriteRepository.userId
    val profileImageFileId = appwriteRepository.profileImageFileId
    val userDocumentId = appwriteRepository.userDocumentId
    val getHashtag = appwriteRepository.getHashtag
    val getHashtagHome = appwriteRepository.getHashtagHome
    val getUserTweet = appwriteRepository.getUserTweet
    val getCurUserTweet = appwriteRepository.getCurUserTweet
    val curUserId = appwriteRepository.curUserId
    val getRetweet = appwriteRepository.getRetweet
    val getCurUserFollowers = appwriteRepository.getCurUserFollowers
    val selectedUserDoc = appwriteRepository.selectedUserDoc
    val getSelectedUserTweet = appwriteRepository.getSelectedUserTweet
    val getSelectedUserFollowers = appwriteRepository.getSelectedUserFollowers
    val login_error = appwriteRepository.login_error
    val updateProfileSuccess = appwriteRepository.updateProfileSuccess


    fun createAccount(email: String, password: String, name: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            appwriteRepository.createAccount(email, password, name, onSuccess, onError)
        }
    }

    fun login(email: String, password: String, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            appwriteRepository.login(email, password, onError)
        }
    }

    fun getAccount() {
        viewModelScope.launch {
            appwriteRepository.getAccount()
        }
    }

    fun getSession() {
        viewModelScope.launch {
            appwriteRepository.getSession()
        }
    }

    fun logout(onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            appwriteRepository.logout(onSuccess, onError)
        }
    }

    fun getCurrentUserId(): String {
        var userId = ""
        viewModelScope.launch {
            userId = appwriteRepository.getCurrentUserId()!!
        }
        return userId
    }

    fun storeImage(imageUri: Uri?) {
        viewModelScope.launch {
            appwriteRepository.storeImage(imageUri)
        }
    }

    /*
    fun getImage(bucketId: String, fileId: String, imageView: ImageView){
        viewModelScope.launch {
            appwriteRepository.getImage(bucketId, fileId, imageView)
        }
    }
     */

    fun postTweet(tweet: Tweet) {
        viewModelScope.launch {
            appwriteRepository.postTweet(tweet)
        }
    }

    fun getTweet() {
        viewModelScope.launch {
            appwriteRepository.getTweet()
        }
    }


    fun storeImageTweet(uri: Uri?) {
        viewModelScope.launch {
            appwriteRepository.storeImageTweet(uri)
        }
    }

    fun addUser(user: com.example.twitter.utils.User) {
        viewModelScope.launch {
            appwriteRepository.addUser(user)
        }
    }

    fun getUser(curUser: String?) {
        viewModelScope.launch {
            appwriteRepository.getUser(curUser)
        }
    }

    fun getSearchedHashtag(
        hashtag: String?,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit,
        home: Boolean
    ) {
        viewModelScope.launch {
            appwriteRepository.getSearchedHashtag(hashtag, onSuccess, onError, home)
        }
    }

    fun updateUserData(
        followedHashtags: Array<String>,
        hashtagsIsEmpty: Boolean,
        likes: Array<String>,
        likeIsEmpty: Boolean,
        retweets: Array<String>,
        retweetIsEmpty: Boolean,
        tweetId: String?,
        onSuccess: (String?) -> Unit, onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            appwriteRepository.updateUserData(
                followedHashtags,
                hashtagsIsEmpty,
                likes,
                likeIsEmpty,
                retweets,
                likeIsEmpty,
                tweetId,
                onSuccess,
                onError
            )
        }
    }

    fun updateFollowing(
        user_id: String,
        followUsers: Array<String>,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            appwriteRepository.updateFollowing(user_id, followUsers, onSuccess, onError)
        }
    }

    fun getSearchedUserTweet(
        curUserId: String?,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            appwriteRepository.getSearchedUserTweet(curUserId, onSuccess, onError)
        }
    }

    fun getCurrentUserTweet(
        curUserId: String?,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            appwriteRepository.getCurrentUserTweet(curUserId, onSuccess, onError)
        }
    }

    fun addRetweet(tweetId: String?, userId: String?) {
        viewModelScope.launch {
            appwriteRepository.addRetweet(tweetId, userId)
        }
    }

    suspend fun getRetweet(
        tweetId: String?,
        userId: String?,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit
    ): String?{
        var timestamp: String? = null
        timestamp= appwriteRepository.getRetweet(tweetId, userId, onSuccess, onError)
        Log.e("getRetweet vm", timestamp.toString())
        return timestamp
//        return timestamp
    }

    fun deleteRetweet(tweetId: String?, userId: String?){
        viewModelScope.launch {
            appwriteRepository.deleteRetweet(tweetId, userId)
        }
    }

    fun updateProfileInfo(
        username: String?,
        mobile: String?,
        about: String?,
        onSuccess: (String?) -> Unit, onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            appwriteRepository.updateProfileInfo(username,mobile,about,onSuccess,onError)
        }
    }


    fun getCurrentUserFollowers(
        onSuccess: () -> Unit, onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            appwriteRepository.getCurrentUserFollowers(onSuccess, onError)
        }
    }

    fun verifyUser(isVerified: Boolean) {
        viewModelScope.launch {
            appwriteRepository.verifyUser(isVerified)
        }
    }

    fun deleteTweet(tweetId: String?, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch {
            appwriteRepository.deleteTweet(tweetId,onSuccess,onError)
        }
    }

    fun getSelectedUser(curUser: String?) {
        viewModelScope.launch {
            appwriteRepository.getSelectedUser(curUser)
        }
    }

    fun getSelectedUserTweet(
        curUserId: String?,
        onSuccess: () -> Unit, onError: (Exception) -> Unit
    ){
        viewModelScope.launch {
            appwriteRepository.getSelectedUserTweet(curUserId, onSuccess, onError)
        }
    }
    fun getSelectedUserFollowers(
        curUserId: String?,
        onSuccess: () -> Unit, onError: (Exception) -> Unit
    ){
        viewModelScope.launch {
            appwriteRepository.getSelectedUserFollowers(curUserId, onSuccess, onError)
        }
    }
}