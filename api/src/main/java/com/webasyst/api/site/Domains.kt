package com.webasyst.api.site

import com.google.gson.annotations.SerializedName

data class Domains(
    @SerializedName("domains")
    val domains: List<Domain>
) {
    data class Domain(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("style")
        val style: String
    )
}
