package com.webasyst.x.auth

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import com.webasyst.x.util.SingletonHolder
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.CodeVerifierUtil
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import kotlin.coroutines.suspendCoroutine

class WebasystAuthService internal constructor(
    context: Context,
    private val clientId: String,
    authEndpoint: String,
    tokenEndpoint: String,
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
        if (state.needsTokenRefresh && state.refreshToken != null) {
            performTokenRequest(state.createTokenRefreshRequest())
        }
    }

    fun authorize(authRequest: AuthorizationRequest, pi: PendingIntent) {
        authService.performAuthorizationRequest(authRequest, pi)
    }

    fun logoff() {
        stateStore.replace(AuthState())
    }

    fun createAuthorizationRequest(): AuthorizationRequest {
        val codeVerivier = CodeVerifierUtil.generateRandomCodeVerifier()

        return AuthorizationRequest.Builder(
            authServiceConfiguration,
            clientId,
            ResponseTypeValues.CODE,
            callbackUri
        )
            .setCodeVerifier(codeVerivier)
            .build()
    }

    fun performTokenRequest(request: TokenRequest) {
        authService.performTokenRequest(request) { response, exception ->
            stateStore.updateAfterTokenResponse(response, exception)
        }
    }

    suspend fun <T> withFreshToken(block: suspend (accessToken: String?) -> T): T {
        val token = suspendCoroutine<String> { continuation ->
            stateStore.getCurrent().performActionWithFreshTokens(authService) { accessToken, _, ex ->
                continuation.resumeWith(when {
                    accessToken != null -> Result.success(accessToken)
                    ex != null -> Result.failure(ex)
                    else -> Result.failure(
                        IllegalStateException("Either token or exception should be set")
                    )
                })
            }
        }
        return block(token)
    }

    companion object : SingletonHolder<WebasystAuthService, Context>(::createWebasystAuthService) {
        const val EXTRA_AUTH_SERVICE_DISCOVERY = "authServiceDiscovery"

        internal val configuration = WebasystAuthConfiguration()
        fun configure(block: WebasystAuthConfiguration.() -> Unit) {
            configuration.apply(block)
        }
    }
}

private fun createWebasystAuthService(context: Context): WebasystAuthService = WebasystAuthService(
    context = context,
    clientId = WebasystAuthService.configuration.clientId ?: throw IllegalStateException("Client id must be set"),
    authEndpoint = WebasystAuthService.configuration.authEndpoint ?: throw IllegalStateException("Auth endpoint must be set"),
    tokenEndpoint = WebasystAuthService.configuration.tokenEndpoint ?: throw IllegalStateException("Token endpoint must be set"),
    callbackUri = WebasystAuthService.configuration.callbackUri ?: throw IllegalStateException("Callback uri must be set")
)
