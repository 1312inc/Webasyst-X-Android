package com.webasyst.x.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.webasyst.x.util.SingletonHolder
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.AuthorizationServiceDiscovery
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest

class WebasystAuthService private constructor(
    context: Context,
    private val clientId: String,
    private val authEndpoint: String,
    private val tokenEndpoint: String,
    callbackUri: String,
) : WebasystAuthStateManager.AuthStateObserver {
    private val callbackUri = Uri.parse(callbackUri)
    private val authService = AuthorizationService(context)
    private val authServiceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse(authEndpoint),
        Uri.parse(tokenEndpoint)
    )

    private val stateStore = WebasystAuthStateManager.getInstance(context)

    init {
        stateStore.addObserver(this)
    }

    override fun onChange(state: AuthState) {
        println(state)
        if (state.needsTokenRefresh && state.refreshToken != null) {
            performTokenRequest(state.createTokenRefreshRequest())
        }
    }

    fun authorize(context: Context) {
        val codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier()

        val authRequest = AuthorizationRequest.Builder(
            authServiceConfiguration,
            clientId,
            ResponseTypeValues.CODE,
            callbackUri
        )
            .setCodeVerifier(codeVerifier)
            .build()

        authService.performAuthorizationRequest(authRequest, createPostAuthorizationIntent(
            context,
            authRequest,
            authServiceConfiguration.discoveryDoc
        ))
    }

    private fun createPostAuthorizationIntent(
        context: Context,
        request: AuthorizationRequest,
        discoveryDoc: AuthorizationServiceDiscovery?
    ) : PendingIntent {
        val intent = Intent(context, WebasystAuthHelperService::class.java)
        intent.action = WebasystAuthHelperService.ACTION_UPDATE_AFTER_AUTHORIZATION
        if (null != discoveryDoc) {
            intent.putExtra(EXTRA_AUTH_SERVICE_DISCOVERY, discoveryDoc.docJson.toString())
        }
        return PendingIntent.getService(context, request.hashCode(), intent, 0)
    }

    fun performTokenRequest(request: TokenRequest) {
        authService.performTokenRequest(request) { response, exception ->
            stateStore.updateAfterTokenResponse(response, exception)
        }
    }

    companion object : SingletonHolder<WebasystAuthService, Context>(Companion::create) {
        const val EXTRA_AUTH_SERVICE_DISCOVERY = "authServiceDiscovery"

        private val configuration = WebasystAuthConfiguration()
        fun configure(block: WebasystAuthConfiguration.() -> Unit) {
            configuration.apply(block)
        }
        private fun create(context: Context): WebasystAuthService = WebasystAuthService(
            context = context.applicationContext,
            clientId = configuration.clientId ?: throw IllegalStateException("Client id must be set"),
            authEndpoint = configuration.authEndpoint ?: throw IllegalStateException("Auth endpoint must be set"),
            tokenEndpoint = configuration.tokenEndpoint ?: throw IllegalStateException("Token endpoint must be set"),
            callbackUri = configuration.callbackUri ?: throw IllegalStateException("Callback uri must be set")
        )
    }
}
