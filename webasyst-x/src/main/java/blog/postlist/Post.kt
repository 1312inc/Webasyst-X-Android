package com.webasyst.x.blog.postlist

import android.os.Parcel
import android.os.Parcelable
import com.webasyst.api.blog.User
import java.util.Calendar

data class Post(
    val id: String,
    val title: String,
    val text: String,
    val user: User?,
    val dateTime: Calendar?
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
        dest.writeStringArray(arrayOf(id, title, text))
    }

    companion object {
        val CREATOR = object : Parcelable.Creator<Post> {
            override fun createFromParcel(source: Parcel): Post {
                val data = Array(2) { "" }
                source.readStringArray(data)
                return Post(
                    id = data[0],
                    title = data[1],
                    text = data[2],
                    user = null,
                    dateTime = null
                )
            }

            override fun newArray(size: Int): Array<Post> = Array(size) { Post("", "", "", null, null) }
        }
    }
}
