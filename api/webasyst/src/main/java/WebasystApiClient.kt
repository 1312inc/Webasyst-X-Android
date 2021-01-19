package com.webasyst.api.webasyst

import com.webasyst.api.ApiClientConfiguration
import com.webasyst.api.ApiModule
import com.webasyst.api.Installation
import com.webasyst.api.Response
import com.webasyst.api.WAIDAuthenticator
import com.webasyst.api.apiRequest
import io.ktor.client.request.get

class WebasystApiClient(
    config: ApiClientConfiguration,
    installation: Installation,
    waidAuthenticator: WAIDAuthenticator,
) : ApiModule(
    config = config,
    installation = installation,
    waidAuthenticator = waidAuthenticator,
) {
    suspend fun getInstallationInfo(): Response<InstallationInfo> = apiRequest {
        client.get("$urlBase/api.php/webasyst.getInfo") { configureRequest() }
    }

    companion object {
        const val SCOPE = "webasyst"
    }
}
