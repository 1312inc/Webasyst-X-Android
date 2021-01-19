package com.webasyst.api.blog

import com.webasyst.api.ApiClientConfiguration
import com.webasyst.api.ApiModule
import com.webasyst.api.Installation
import com.webasyst.api.Response
import com.webasyst.api.WAIDAuthenticator
import com.webasyst.api.apiRequest
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.ContentType

class BlogApiClient(
    config: ApiClientConfiguration,
    installation: Installation,
    waidAuthenticator: WAIDAuthenticator,
) : ApiModule(
    config = config,
    installation = installation,
    waidAuthenticator = waidAuthenticator,
) {
     suspend fun getPosts(): Response<Posts> = apiRequest {
         return client.doGet("$urlBase/api.php/blog.post.search") {
             parameter("limit", 10)
             parameter("hash", "author")
             headers {
                 accept(ContentType.Application.Json)
             }
         }
    }

    companion object {
        const val SCOPE = "blog"
    }
}
