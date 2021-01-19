package com.webasyst.api

interface WAIDAuthenticator {
    suspend fun getInstallationApiAuthCodes(appClientIDs: Set<String>): Response<Map<String, String>>
}
