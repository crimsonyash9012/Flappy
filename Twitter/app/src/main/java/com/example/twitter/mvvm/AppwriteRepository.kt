package com.example.twitter.mvvm

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.example.twitter.utils.Constant
import com.example.twitter.databinding.ActivityProfileBinding
import com.example.twitter.single.AppwriteClientSingleton
import com.example.twitter.utils.SingleLiveEvent
import com.example.twitter.utils.Tweet
import com.example.twitter.utils.generateRandomString
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.models.InputFile
import io.appwrite.models.Session
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.Throws

class AppwriteRepository(val context: Context) {

    private val _client = AppwriteClientSingleton.getClient(context)
    private val client: Client get() = _client

    private val _user = MutableLiveData<User<Map<String, Any>>?>()
    val user: MutableLiveData<User<Map<String, Any>>?> get() = _user

    private val _userDoc = MutableLiveData<Document<Map<String, Any>>?>()
    val userDoc: MutableLiveData<Document<Map<String, Any>>?> get() = _userDoc

    private val _tweetDoc = MutableLiveData<Document<Map<String, Any>>?>()
    val tweetDoc: MutableLiveData<Document<Map<String, Any>>?> get() = _tweetDoc

    val curTweetId = ID.unique()
    val lastTweetId: String? = null

    var userId = ID.unique()
    var curUserId: String? = null

    val updateProfileSuccess = SingleLiveEvent<Boolean>()

    private val _session = MutableLiveData<Session?>()
    val session: MutableLiveData<Session?> get() = _session

    private val storage = Storage(client)

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: MutableLiveData<String?> get() = _imageUrl

    private val _imageUrlTweet = MutableLiveData<String?>()
    val imageUrlTweet: MutableLiveData<String?> get() = _imageUrlTweet

    private val _getHashtag = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getHashtag: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getHashtag

    private val _getHashtagHome = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getHashtagHome: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getHashtagHome

    private val _getUserTweet = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getUserTweet: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getUserTweet

    private val _getCurUserTweet = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getCurUserTweet: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getCurUserTweet

    private val _getCurUserFollowers = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getCurUserFollowers: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getCurUserFollowers

    private val _retweet = MutableLiveData<String?>()
    val retweet: MutableLiveData<String?> get() = _retweet

    private val _getRetweet = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getRetweet: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getRetweet

    private val _selectedUserDoc = MutableLiveData<Document<Map<String, Any>>?>()
    val selectedUserDoc: MutableLiveData<Document<Map<String, Any>>?> get() = _selectedUserDoc

    private val _getSelectedUserTweet = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getSelectedUserTweet: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getSelectedUserTweet

    private val _getSelectedUserFollowers = MutableLiveData<DocumentList<Map<String, Any>>?>()
    val getSelectedUserFollowers: MutableLiveData<DocumentList<Map<String, Any>>?> get() = _getSelectedUserFollowers

    private val _login_error = MutableLiveData<String?>()
    val login_error: MutableLiveData<String?> get() = _login_error

    var profileImageFileId = ID.unique()
    var tweetImageFileId = ID.unique()
    var userDocumentId = ""

    suspend fun createAccount(
        email: String,
        password: String, name: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit) {
        val account = Account(client)
        try {
            userId = ID.unique()
            val user = account.create(
                userId,
                email, password, name
            )
            // when acc is created => value is posted to the user variable
            _user.postValue(user)
            login_error.postValue(null)
            onSuccess()
        } catch (e: AppwriteException) {
            Log.e("AppwriteRepository", "createAccount: ${e.message}")
            login_error.postValue(e.message.toString())
            onError(e)
            _user.postValue(null)
        }
    }

    suspend fun login(email: String, password: String, onError: (Exception) -> Unit) {
        val account = Account(client)
        try {
            val session = account.createEmailPasswordSession(email = email, password = password)
            _session.postValue(session)
            login_error.postValue(null)
            curUserId = getCurrentUserId()
        } catch (e: AppwriteException) {
            Log.e("AppwriteRepository", "login: ${e.message}")
            login_error.postValue(e.message.toString())
            onError(e)
            _session.postValue(null)
        }
    }

    suspend fun getAccount() {
        val account = Account(client)
        try {
            val user = account.get()
            _user.postValue(user)
            curUserId = getCurrentUserId()
        } catch (e: AppwriteException) {
            Log.e("AppwriteRepository", "getAccount: ${e.message}")
            _user.postValue(null)
        }
    }

    suspend fun getSession() {
        val account = Account(client)
        try {
            val session = account.getSession("current")
            _session.postValue(session)
            curUserId = getCurrentUserId()
        } catch (e: AppwriteException) {
            Log.e("AppwriteRepository", "getSession: ${e.message}")
            _session.postValue(null)
            curUserId = null
        }
    }

    suspend fun logout(onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        val account = Account(client)
        try {
            account.deleteSession("current")
            _session.postValue(null)
            _user.postValue(null)
            curUserId = null
            userId = ID.unique()
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
            onSuccess(userId)
        } catch (e: AppwriteException) {
            session.postValue(null)
            _user.postValue(null)
            userId = ID.unique()
            curUserId = null
            onError(e)
        }
    }

    suspend fun getCurrentUserId(): String? {
        try {
            val account = Account(client)
            val user = account.get()
            Log.e("Appwrite", "User ID: ${user.id}")
            return user.id
        } catch (e: Exception) {
            Log.e("Appwrite", "Error fetching user: ${e.message}")
            return null
        }
    }

    /*
    suspend fun storeImage(imageUri: Uri?, profileBinding: ActivityProfileBinding) {
        imageUri?.let { uri ->
            Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
            profileBinding.profileProgressLayout.visibility = View.VISIBLE
            val inputStream = context.contentResolver.openInputStream(imageUri)

            inputStream?.let { stream ->

                val bytes = stream.readBytes()
//                val inputFile = InputFile.fr("profile_image.jpg", bytes)
                val inputFile = InputFile.fromBytes(bytes, "profile_image.jpg", "image/jpeg")

                val fileId = try {
                    val response = storage.createFile(
                        bucketId = Constant.BUCKET_ID_PROF_IMAGE,
                        fileId = ID.unique(),
                        file = inputFile
                    )
                    response.id
                } catch (e: Exception) {
                    Log.e("AppwriteRepository", "storeImage: ${e.message}")
                    null
                }
                if (fileId != null) {
                    val url = getImageUrl(Constant.BUCKET_ID_PROF_IMAGE, fileId)
                    Log.e("AppwriteRepository", "fileID!=null")
                    _imageUrl.postValue(url)
                } else {
                    _imageUrl.postValue(null)
                    Log.e("AppwriteRepository", "fileID==null")
                }
            }
        }
    }
     */
    suspend fun storeImage(uri: Uri?) {
        try {
            curUserId = getCurrentUserId()
            if (uri == null) {
                throw Exception("Profile Image uri is null")
            }
            val databases = Databases(client)
            val inputStream = context.contentResolver.openInputStream(uri!!)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val fileBytes = inputStream?.readBytes()

            if (fileBytes != null) {
                val file = InputFile.fromBytes(fileBytes, fileName, "image/jpeg")

                try {
                    val file = storage.createFile(
                        bucketId = Constant.BUCKET_ID_PROF_IMAGE,
                        fileId = profileImageFileId,
                        file = file
                    )
                    Log.e("Appwrite", "File uploaded: $fileName")

                    try {
                        val fileUrl =
                            "${client.endpoint}/storage/buckets/${Constant.BUCKET_ID_PROF_IMAGE}/files/${profileImageFileId}/view?project=${Constant.PROJECT_ID}"
                        Log.e("Image Url", fileUrl)

                        try {
                            val curUser = getCurrentUserId()
                            databases.updateDocument(
                                databaseId = Constant.DATABASE_ID,
                                collectionId = Constant.COLLECTION_USER_ID,
                                documentId = curUser!!,
                                data = mapOf(
                                    "imageUrl" to fileUrl
                                )
                            )
                            _imageUrl.postValue(fileUrl)
                            profileImageFileId = ID.unique()
                        } catch (e: Exception) {
                            Log.e("Image Update", e.printStackTrace().toString())
                            _imageUrl.postValue(null)
                        }
                        profileImageFileId = ID.unique()
                    } catch (e: Exception) {
                        Log.e("Image Update URI in collection", e.message.toString())
                        _imageUrl.postValue(null)
                        profileImageFileId = ID.unique()
                    }
                } catch (e: Exception) {
                    Log.e("Appwrite", e.message.toString())
                    _imageUrl.postValue(null)
                    profileImageFileId = ID.unique()
                }
            } else {
                throw Exception("Unable to read file bytes")
                profileImageFileId = ID.unique()
            }
        } catch (e: Exception) {
            Log.e("Appwrite", e.message.toString())
            _imageUrl.postValue(null)
            profileImageFileId = ID.unique()
        }
    }

    suspend fun storeImageTweet(uri: Uri?) {
        try {
//            val databases = Databases(client)
            val inputStream = context.contentResolver.openInputStream(uri!!)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val fileBytes = inputStream?.readBytes()

            if (fileBytes != null) {
                val file = InputFile.fromBytes(fileBytes, fileName, "image/jpeg")

                try {
                    val file = storage.createFile(
                        bucketId = Constant.BUCKET_ID_TWEET_IMAGE,
                        fileId = tweetImageFileId,
                        file = file
                    )
                    Log.e("Appwrite", "File uploaded: $fileName")

                    try {
                        val fileUrl =
                            "${client.endpoint}/storage/buckets/${Constant.BUCKET_ID_TWEET_IMAGE}/files/${tweetImageFileId}/view?project=${Constant.PROJECT_ID}"
                        Log.e("Image Url", fileUrl)
                        tweetImageFileId = ID.unique()
                        _imageUrlTweet.postValue(fileUrl)
                    } catch (e: Exception) {
                        tweetImageFileId = ID.unique()
                        Log.e("Image Update URI in collection", e.message.toString())
                        _imageUrlTweet.postValue(null)
                    }
                } catch (e: Exception) {
                    tweetImageFileId = ID.unique()
                    Log.e("Appwrite", e.message.toString())
                    _imageUrlTweet.postValue(null)
                }
            } else {
                tweetImageFileId = ID.unique()
                throw Exception("Unable to read file bytes")
            }
        } catch (e: Exception) {
            tweetImageFileId = ID.unique()
            Log.e("Appwrite", e.message.toString())
            _imageUrlTweet.postValue(null)
        }
    }

    /*
    suspend fun getImage(bucketId: String, fileId: String, imageView: ImageView) {
//        return "${client.endpoint}/storage/buckets/$bucketId/files/$fileId/view"
        val url = storage.getFilePreview(
            bucketId = bucketId,
            fileId = fileId
        )
        Glide.with(context)
            .load(url)
            .into(imageView)
    }
     */

    suspend fun postTweet(tweet: Tweet) {
        val documentId = ID.unique()
        val databases = Databases(client)
        try {
            val document = databases.createDocument(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_TWEET_ID,
                documentId = documentId,
                data = mapOf(
                    "tweetId" to tweet.tweetId,
                    "userIds" to tweet.userIds,
                    "username" to tweet.username,
                    "text" to tweet.text,
                    "imageUrl" to tweet.imageUrl,
                    "timestamp" to tweet.timestamp,
                    "hashtags" to tweet.hashtags,
                    "likes" to tweet.likes
                )
            )
            Toast.makeText(context, "Tweet created successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating Tweet", Toast.LENGTH_SHORT).show()
            Log.e("repo", e.message.toString())
        }
    }

    suspend fun getTweet() {
        val databases = Databases(client)
        try {
//            val curUser = curUserId /*?: throw Exception("User not found")*/
            if (curUserId == null) {
                Log.e("userIssue", "Error logging in")
            } else {
                val document = databases.getDocument(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_TWEET_ID,
                    documentId = curUserId!!
                )
                _tweetDoc.postValue(document)
            }
        } catch (e: Exception) {
            _tweetDoc.postValue(null)
            Log.e("tweetIssue", e.message.toString())
            throw e
        }
    }


    suspend fun addUser(user: com.example.twitter.utils.User) {
        val databases = Databases(client)
        val documentId = ID.unique()
        try {
            val newUser = databases.createDocument(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_USER_ID,
                documentId = userId,
                data = mapOf(
                    "user_Id" to userId,
                    "email" to user.email,
                    "name" to user.name,
                    "imageUrl" to Constant.DEFAULT_IMAGE_URL,
                    "followHashtags" to user.followHashtags,
                    "followUsers" to user.followUsers,
                    "username" to generateRandomString()
                )
            )
            userDocumentId = documentId
            _userDoc.postValue(newUser)
            Log.e("userIssue", "User created docs")
        } catch (e: Exception) {
            _userDoc.postValue(null)
            Log.e("userIssue", e.message.toString())
            Toast.makeText(context, "Error adding user database ", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getUser(curUser: String?) {
        val databases = Databases(client)
        try {
//            val curUser = curUserId /*?: throw Exception("User not found")*/
            curUserId = getCurrentUserId()
            if (curUserId == null) {
                Log.e("userIssue", "Error logging in")
            } else {
                val document = databases.getDocument(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_USER_ID,
                    documentId = curUserId!!
                )
                _userDoc.postValue(document)
            }
        } catch (e: Exception) {
            _userDoc.postValue(null)
            Log.e("userIssue", e.message.toString())
            throw e
        }
    }

    suspend fun getSearchedHashtag(
        hashtag: String?,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit,
        home: Boolean
    ) {
        try {
            val databases = Databases(client)
            val documents = databases.listDocuments(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_TWEET_ID,
                queries = listOf(
                    Query.equal(Constant.DATA_TWEET_HASHTAGS, hashtag!!)
                )
            )

            if (home) _getHashtagHome.postValue(documents)
            else _getHashtag.postValue(documents)
            onSuccess(hashtag)
        } catch (e: Exception) {

            if (home) _getHashtagHome.postValue(null)
            else _getHashtag.postValue(null)
            Log.e("Search Hashtag", e.message.toString())
            onError(e)
        }
    }


    suspend fun updateUserData(
        followedHashtags: Array<String>,
        hashtagsIsEmpty: Boolean,
        likes: Array<String>,
        likeIsEmpty: Boolean,
        retweets: Array<String>,
        retweetIsEmpty: Boolean,
        tweetId: String?, onSuccess: (String?) -> Unit, onError: (Exception) -> Unit
    ) {
        val databases = Databases(client)
        Log.e("Update Data", "$curUserId")
        try {
            val curUser = curUserId
            if (hashtagsIsEmpty) {
                databases.updateDocument(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_USER_ID,
                    documentId = curUser!!,
                    data = mapOf(
                        "followHashtags" to followedHashtags
                    )
                )
                Log.e("repo", "likes updated")
            }
            if (likeIsEmpty) {
                databases.updateDocument(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_TWEET_ID,
                    documentId = tweetId!!,
                    data = mapOf(
                        "likes" to likes
                    )
                )
            }
            Log.e("repo retweets: ", retweets.contentToString())
            if (retweets.isNotEmpty()) {
                databases.updateDocument(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_TWEET_ID,
                    documentId = tweetId!!,
                    data = mapOf(
                        "userIds" to retweets
                    )
                )
            }
            getUser(curUserId)
            onSuccess(curUserId)
        } catch (e: Exception) {
            onError(e)
            Log.e("Update data", e.message.toString())
        }
    }

    suspend fun updateFollowing(
        user_id: String?,
        followUsers: Array<String>,
        onSuccess: (String?) -> Unit, onError: (Exception) -> Unit
    ) {
        val databases = Databases(client)
        Log.e("Update following", "$user_id")
        try {
            databases.updateDocument(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_USER_ID,
                documentId = getCurrentUserId()!!,
                data = mapOf(
                    "followUsers" to followUsers
                )
            )
            Log.e("repo", "following updated")
            onSuccess(user_id)
        } catch (e: Exception) {
            onError(e)
        }
    }

    suspend fun getSearchedUserTweet(
        userId: String?,
        onSuccess: (String?) -> Unit, onError: (Exception) -> Unit
    ) {
        try {
            val databases = Databases(client)
            val documents = databases.listDocuments(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_TWEET_ID,
                queries = listOf(
                    Query.equal(Constant.DATA_TWEET_USER_IDS, userId!!)
                )
            )
            _getUserTweet.postValue(documents)
            onSuccess(userId)
        } catch (e: Exception) {
            _getUserTweet.postValue(null)
            Log.e("Search Hashtag", e.message.toString())
            onError(e)
        }
    }

    suspend fun getCurrentUserTweet(
        curUserId: String?,
        onSuccess: (String?) -> Unit, onError: (Exception) -> Unit
    ) {
        try {
            val id = getCurrentUserId()
            val databases = Databases(client)
            val documents = databases.listDocuments(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_TWEET_ID,
                queries = listOf(
                    Query.equal(Constant.DATA_TWEET_USER_IDS, id!!)
                )
            )
            _getCurUserTweet.postValue(documents)
            onSuccess(curUserId)
        } catch (e: Exception) {
            _getCurUserTweet.postValue(null)
            Log.e("Search Hashtag", e.message.toString())
            onError(e)
        }
    }

    suspend fun addRetweet(
        tweetId: String?,
        userId: String?
    ) {
        val databases = Databases(client)
        val documentId = ID.unique()
        try {
            val newUser = databases.createDocument(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_RETWEET_ID,
                documentId = tweetId!!,
                data = mapOf(
                    "tweet_id" to tweetId,
                    "user_id" to userId,
                    "timestamp" to System.currentTimeMillis().toString()
                )
            )
            _retweet.postValue(tweetId)
            Log.e("userIssue", "User created docs")
        } catch (e: Exception) {
            _userDoc.postValue(null)
            Log.e("userIssue", e.message.toString())
            Toast.makeText(context, "Error adding user database ", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getRetweet(
        tweetId: String?,
        userId: String?,
        onSuccess: (String?) -> Unit,
        onError: (Exception) -> Unit
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val databases = Databases(client)
                val documents = databases.listDocuments(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_RETWEET_ID,
                    queries = listOf(
                        Query.equal(Constant.DATA_TWEET_ID, tweetId!!),
                        Query.equal(Constant.DATA_USER_ID, userId!!)
                    )
                )
                _getRetweet.postValue(documents)
                Log.e(
                    "getRetweet repo",
                    (documents.documents.firstOrNull()?.data?.get("timestamp") as? String)!!
                )
                onSuccess(tweetId)
                documents.documents.firstOrNull()?.data?.get("timestamp") as? String
            } catch (e: Exception) {
                _getRetweet.postValue(null)
                Log.e("Search Hashtag", e.message.toString())
                onError(e)
                null
            }
        }
    }

    suspend fun deleteRetweet(tweetId: String?, userId: String?) {
        try {
            val databases = Databases(client)
            val documents = databases.listDocuments(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_RETWEET_ID,
                queries = listOf(
                    Query.equal(Constant.DATA_TWEET_ID, tweetId!!),
                    Query.equal(Constant.DATA_USER_ID, userId!!)
                )
            )
            if (documents.total > 0) {
                val documentId = documents.documents[0].id
                databases.deleteDocument(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_RETWEET_ID,
                    documentId = documentId
                )
                Log.d("Appwrite deleteRetweet", "Retweet deleted successfully")
            } else {
                Log.d(
                    "Appwrite deleteRetweet",
                    "No retweet deleted - Tweet ID: $tweetId & User ID: $userId"
                )
            }
        } catch (e: Exception) {
            _getRetweet.postValue(null)
            Log.e("Search Hashtag", e.message.toString())
        }
    }

    suspend fun updateProfileInfo(
        username: String?,
        mobile: String?,
        about: String?,
        onSuccess: (String?) -> Unit, onError: (Exception) -> Unit
    ) {
        val databases = Databases(client)
        Log.e("Update Profile Data", "$curUserId")
        try {
            val curUser = curUserId
            databases.updateDocument(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_USER_ID,
                documentId = curUser!!,
                data = mapOf(
                    "username" to username,
                    "mobile" to mobile,
                    "about" to about
                )
            )
            Log.e("repo", "profile updated")
            getUser(curUserId)
            updateProfileSuccess.postValue(true)
            onSuccess(curUserId)
        } catch (e: Exception) {
            onError(e)
            Log.e("Update data", e.message.toString())
        }
    }



    suspend fun getCurrentUserFollowers(
        onSuccess: () -> Unit, onError: (Exception) -> Unit
    ) {
        try {
            val id = getCurrentUserId()
            val databases = Databases(client)
            val documents = databases.listDocuments(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_USER_ID,
                queries = listOf(
                    Query.equal(Constant.DATA_FOLLOW_USERS, id!!)
                )
            )
            getCurUserFollowers.postValue(documents)
            onSuccess()
        } catch (e: Exception) {
            getCurUserFollowers.postValue(null)
            Log.e("Search Hashtag", e.message.toString())
            onError(e)
        }
    }

    suspend fun verifyUser(isVerified: Boolean) {
        val databases = Databases(client)
        Log.e("Update Profile Data", "$curUserId")
        try {
            val curUser = curUserId
            databases.updateDocument(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_USER_ID,
                documentId = curUser!!,
                data = mapOf(
                    "isVerified" to isVerified
                )
            )
            Log.e("repo", "verification updated")
            getUser(curUserId)
        } catch (e: Exception) {
            Log.e("Verify user", e.message.toString())
        }
    }

    suspend fun deleteTweet(tweetId: String?, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        try {
            val databases = Databases(client)
            databases.deleteDocument(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_TWEET_ID,
                documentId = tweetId!!
            )
            onSuccess()
        } catch (e: Exception) {
            Log.e("Delete tweet", e.message.toString())
            onError(e)
        }
    }

    suspend fun getSelectedUser(curUser: String?) {
        val databases = Databases(client)
        try {
//            val curUser = curUserId /*?: throw Exception("User not found")*/
            if (curUserId == null) {
                Log.e("userIssue", "Error logging in")
            } else {
                val document = databases.getDocument(
                    databaseId = Constant.DATABASE_ID,
                    collectionId = Constant.COLLECTION_USER_ID,
                    documentId = curUser!!
                )
                _selectedUserDoc.postValue(document)
            }
        } catch (e: Exception) {
            _selectedUserDoc.postValue(null)
            Log.e("userIssue", e.message.toString())
            throw e
        }
    }

    suspend fun getSelectedUserTweet(
        curUserId: String?,
        onSuccess: () -> Unit, onError: (Exception) -> Unit
    ) {
        try {
            val id = curUserId
            val databases = Databases(client)
            val documents = databases.listDocuments(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_TWEET_ID,
                queries = listOf(
                    Query.equal(Constant.DATA_TWEET_USER_IDS, id!!)
                )
            )
            _getSelectedUserTweet.postValue(documents)
            onSuccess()
        } catch (e: Exception) {
            _getSelectedUserTweet.postValue(null)
            Log.e("Search Hashtag", e.message.toString())
            onError(e)
        }
    }

    suspend fun getSelectedUserFollowers(
        curUserId: String?,
        onSuccess: () -> Unit, onError: (Exception) -> Unit
    ) {
        try {
            val id = curUserId
            val databases = Databases(client)
            val documents = databases.listDocuments(
                databaseId = Constant.DATABASE_ID,
                collectionId = Constant.COLLECTION_USER_ID,
                queries = listOf(
                    Query.equal(Constant.DATA_FOLLOW_USERS, id!!)
                )
            )
            getSelectedUserFollowers.postValue(documents)
            onSuccess()
        } catch (e: Exception) {
            getSelectedUserFollowers.postValue(null)
            Log.e("Search Hashtag", e.message.toString())
            onError(e)
        }
    }
}