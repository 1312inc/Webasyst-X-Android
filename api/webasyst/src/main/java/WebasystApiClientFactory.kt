package com.webasyst.api.webasyst

import com.webasyst.api.ApiClientConfiguration
import com.webasyst.api.ApiModuleFactory
import com.webasyst.api.Installation
import com.webasyst.api.WAIDAuthenticator

class WebasystApiClientFactory(
    private val config: ApiClientConfiguration,
    private val waidAuthenticator: WAIDAuthenticator,
) : ApiModuleFactory<WebasystApiClient>() {
    override val scope = WebasystApiClient.SCOPE

    override fun instanceForInstallation(installation: Installation): WebasystApiClient {
        return WebasystApiClient(
            config = config,
            waidAuthenticator = waidAuthenticator,
            installation = installation,
        )
    }
}
