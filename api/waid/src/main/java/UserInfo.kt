package com.webasyst.waid

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: List<Email>,
    @SerializedName("userpic")
    val userpic: String,
    @SerializedName("userpic_uploaded")
    val usrpicUploaded: Boolean
) {
    fun getEmail(): String = email.firstOrNull()?.value ?: ""

    data class Email(
        @SerializedName("value")
        val value: String
    )
}
