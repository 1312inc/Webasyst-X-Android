package com.webasyst.api.shop

import com.webasyst.api.ApiClientConfiguration
import com.webasyst.api.ApiModuleFactory
import com.webasyst.api.Installation
import com.webasyst.api.WAIDAuthenticator

class ShopApiClientFactory(
    private val config: ApiClientConfiguration,
    private val waidAuthenticator: WAIDAuthenticator,
) : ApiModuleFactory<ShopApiClient>() {
    override val scope = ShopApiClient.SCOPE

    override fun instanceForInstallation(installation: Installation): ShopApiClient {
        return ShopApiClient(
            config = config,
            waidAuthenticator = waidAuthenticator,
            installation = installation,
        )
    }
}
