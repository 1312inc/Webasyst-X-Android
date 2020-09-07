package com.webasyst.auth;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.CodeVerifierUtil;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;

public class WebasystAuthService {
    private final WebasystAuthStateStore stateStore;
    private final WebasystAuthConfiguration configuration;
    private final AuthorizationServiceConfiguration authServiceConfiguration;
    private final AuthorizationService authorizationService;

    @Nullable
    static WebasystAuthConfiguration currentConfiguration = null;

    private WebasystAuthService(@NonNull final Context context,
                                @NonNull final WebasystAuthConfiguration configuration) {
        stateStore = WebasystAuthStateStore.getInstance(context);
        this.configuration = configuration;
        this.authServiceConfiguration = new AuthorizationServiceConfiguration(
            configuration.authEndpoint,
            configuration.tokenEndpoint
        );
        authorizationService = new AuthorizationService(context);
    }

    public static WebasystAuthService getInstance(@NonNull final Context context) {
        if (null == currentConfiguration) {
            throw new IllegalStateException("Configuration must be set");
        }
        return new WebasystAuthService(context, currentConfiguration);
    }

    private AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public static void configure(WebasystAuthConfiguration configuration) {
        currentConfiguration = configuration;
    }

    void performTokenRequest(TokenRequest request) {
        authorizationService.performTokenRequest(request, stateStore::updateAfterTokenResponse);
    }

    public <T> void withFreshAccessToken(final AccessTokenTask<T> task) {
        stateStore.getCurrent().performActionWithFreshTokens(authorizationService,
            (AuthState.AuthStateAction) (accessToken, idToken, exception) -> {
                if (exception != null) {
                    if (exception.code >= 2000) {
                        stateStore.replace(new AuthState());
                    }
                }
                task.apply(accessToken, exception);
            });
    }

    public <T> void withFreshAccessToken(final AccessTokenTask<T> task, final Consumer<T> callback) {
        stateStore.getCurrent().performActionWithFreshTokens(authorizationService,
            (accessToken, idToken, exception) -> callback.accept(task.apply(accessToken, exception)));
    }

    public void signIn(AuthorizationRequest request, PendingIntent success) {
        getAuthorizationService().performAuthorizationRequest(request, success);
    }

    public void signOff() {
        stateStore.replace(new AuthState());
    }

    public void dispose() {

    }

    AuthorizationRequest createAuthorizationRequest() {
        final String codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier();

        return new AuthorizationRequest.Builder(
            authServiceConfiguration,
            configuration.clientId,
            ResponseTypeValues.CODE,
            configuration.callbackUri
        )
            .setCodeVerifier(codeVerifier)
            .setScopes(configuration.scope)
            .build();
    }

    public interface AccessTokenTask<T> {
        T apply(@Nullable String accessToken, @Nullable Exception exception);
    }
}
