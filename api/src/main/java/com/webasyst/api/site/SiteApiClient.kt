package com.webasyst.api.site

import com.webasyst.api.ApiClientConfiguration
import com.webasyst.api.ApiModule
import com.webasyst.api.Installation
import com.webasyst.api.Response
import com.webasyst.api.WAIDAuthenticator
import com.webasyst.api.apiRequest

class SiteApiClient(
    config: ApiClientConfiguration,
    installation: Installation,
    waidAuthenticator: WAIDAuthenticator,
) : ApiModule(
    config = config,
    installation = installation,
    waidAuthenticator = waidAuthenticator,
) {
    suspend fun getDomainList(): Response<Domains> = apiRequest {
        return client.doGet("$urlBase/api.php/site.domain.getList")
    }

    companion object {
        const val SCOPE = "site"
    }
}
