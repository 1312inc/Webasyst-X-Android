package com.webasyst.api.blog

import com.webasyst.api.ApiClientConfiguration
import com.webasyst.api.ApiModuleFactory
import com.webasyst.api.Installation
import com.webasyst.api.WAIDAuthenticator

class BlogApiClientFactory(
    private val config: ApiClientConfiguration,
    private val waidAuthenticator: WAIDAuthenticator,
) : ApiModuleFactory<BlogApiClient>() {
    override val scope = BlogApiClient.SCOPE

    override fun instanceForInstallation(installation: Installation): BlogApiClient {
        return BlogApiClient(
            config = config,
            waidAuthenticator = waidAuthenticator,
            installation = installation,
        )
    }
}
