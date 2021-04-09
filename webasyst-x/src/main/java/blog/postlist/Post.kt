package com.webasyst.x.blog.postlist

import android.os.Parcel
import android.os.Parcelable
import com.webasyst.api.blog.User
import com.webasyst.api.util.threadLocal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

data class Post(
    val id: String,
    val title: String,
    val text: String,
    val user: User,
    val dateTime: Calendar,
) : Parcelable {
    constructor(post: com.webasyst.api.blog.Post) : this(
        id = post.id,
        title = post.title,
        text = post.text,
        user = post.user,
        dateTime = post.dateTime
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringArray(arrayOf(id, title, text, user.name, dateFormat.format(dateTime)))
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-DD HH:mm:ss"
        private val dateFormat by threadLocal {
            SimpleDateFormat(DATE_FORMAT)
        }

        @JvmStatic
        val CREATOR = object : Parcelable.Creator<Post> {
            override fun createFromParcel(source: Parcel): Post {
                val data = Array(4) { "" }
                source.readStringArray(data)
                return Post(
                    id = data[0],
                    title = data[1],
                    text = data[2],
                    user = User(id = "", name = data[3], photoUrl20 = ""),
                    dateTime = Calendar.getInstance().apply { time = dateFormat.parse(data[4]) ?: Date() }
                )
            }

            override fun newArray(size: Int): Array<Post> = Array(size) { Post("", "", "", User("", "", ""), Calendar.getInstance()) }
        }
    }
}
