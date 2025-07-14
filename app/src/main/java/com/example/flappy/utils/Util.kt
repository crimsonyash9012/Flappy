package com.example.flappy.utils

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.flappy.R
import java.text.DateFormat
import java.util.Date

fun ImageView.loadUrl(url: String?, errorDrawable: Int = R.drawable.empty) {
    val options = RequestOptions()
        .error(errorDrawable)

    Glide.with(this.context)
        .load(url)
        .apply(options)
        .into(this)
}


fun getDate(s: Long?): String{
    s?.let{
        val df = DateFormat.getDateInstance()
        return df.format(Date(it))
    }
    return "Unknown"
}

fun generateRandomString(): String {
    val alphabet = ('a'..'z') + ('A'..'Z')
    val alphanumeric = alphabet + ('0'..'9')

    val firstChar = alphabet.random()
    val remainingChars = (1..9).map { alphanumeric.random() }.joinToString("")

    return "@$firstChar$remainingChars"
}

fun isImageSizeValid(uri: Uri, image: ImageView): Boolean {
    val fileSizeLimit = 3 * 1024 * 1024 // 3MB in bytes

    val cursor = image.context.contentResolver.query(uri, null, null, null, null)
    val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
    cursor?.moveToFirst()

    val size = if (sizeIndex != null && sizeIndex != -1) {
        cursor.getLong(sizeIndex)
    } else {
        0L
    }

    cursor?.close()
    return size <= fileSizeLimit
}

fun mapToTweet(data: Map<String, Any>?): Tweet? {
    if (data == null) return null
    return Tweet(
        tweetId = data["\$id"] as? String ?: return null,
        userIds = (data["userIds"] as? List<String>)?.toTypedArray() ?: arrayOf(),
        username = data["username"] as? String,
        text = data["text"] as? String,
        imageUrl = data["imageUrl"] as? String,
        timestamp = data["timestamp"] as? String,
        hashtags = (data["hashtags"] as? List<String>)?.toTypedArray() ?: arrayOf(),
        likes = (data["likes"] as? List<String>)?.toTypedArray() ?: arrayOf()
    )
}


