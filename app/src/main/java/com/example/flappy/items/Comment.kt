package com.example.flappy.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flappy.utils.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun CommentList(comments: List<Comment>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        comments.forEach { comment ->
            CommentCard(comment = comment)
        }
    }
}



@Composable
fun CommentCard(
    comment: Comment
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .padding(end = 4.dp),
        verticalAlignment = Alignment.Top
    ) {

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = comment.username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(comment.timestamp.toLongOrNull()),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.text,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(6.dp))

        }
    }
}

fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


@Preview
@Composable
fun preview(){
    val comment  = Comment(
        "Yash",
        "https://cloud.appwrite.io/v1/storage/buckets/6782cb51003801987023/files/6872a83d003113ab8b12/view?project=67738b94001e97d033c2",
        "hello",
        "",
        System.currentTimeMillis().toString(),
        ""
    )
    CommentCard(comment)
}
