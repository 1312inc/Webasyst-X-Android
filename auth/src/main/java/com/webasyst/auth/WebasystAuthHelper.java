package com.webasyst.auth;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationServiceDiscovery;

import java.lang.ref.WeakReference;

/**
 * This helper class is responsible for WAID sign in/sign out
 */
public class WebasystAuthHelper {
    public static final String ACTION_UPDATE_AFTER_AUTHORIZATION = "update_post_auth";
    public static final String ACTION_AFTER_AUTHORIZATION_CANCELLED = "handle_auth_cancel";
    private static final String EXTRA_AUTH_SERVICE_DISCOVERY = "authServiceDiscovery";

    private final WeakReference<Context> contextRef;
    private final WebasystAuthStateStore stateStore;
    private final WebasystAuthService authService;

    public WebasystAuthHelper(Context context) {
        contextRef = new WeakReference<>(context);
        stateStore = WebasystAuthStateStore.getInstance(context);
        authService = WebasystAuthService.getInstance(context);
    }

    /**
     * Handles authentication callback.
     * Should be called from Activity's onCreate() and/or onNewIntent().
     */
    public void handleIntent(final @NonNull Intent intent) {
        final String action = intent.getAction();

        if (ACTION_UPDATE_AFTER_AUTHORIZATION.equals(action)) {
            final AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
            final AuthorizationException exception = AuthorizationException.fromIntent(intent);
            stateStore.updateAfterAuthorization(response, exception);

            if (null != response) {
                authService.performTokenRequest(response.createTokenExchangeRequest());
            }
        }
    }

    /**
     * Performs user authentication and sign in.
     *
     * @param activityClass Class representing authentication activity callback
     */
    public void signIn(@NonNull final Class<? extends Activity> activityClass) {
        final AuthorizationRequest request = authService.createAuthorizationRequest();
        final PendingIntent successIntent = createPostAuthorizationIntent(request, null, activityClass);
        final PendingIntent cancelIntent = createAuthorizationCancelIntent(request, activityClass);
        authService.signIn(request, successIntent, cancelIntent);
    }

    /**
     * Performs sign out.
     */
    public void signOut() {
        stateStore.replace(new AuthState());
    }

    private PendingIntent createPostAuthorizationIntent(
        @NonNull final AuthorizationRequest request,
        @Nullable final AuthorizationServiceDiscovery discoveryDoc,
        @NonNull final Class<? extends Activity> activityClass
    ) {
        final Context context = contextRef.get();
        final Intent intent = new Intent(context, activityClass);
        intent.setAction(ACTION_UPDATE_AFTER_AUTHORIZATION);
        if (null != discoveryDoc) {
            intent.putExtra(EXTRA_AUTH_SERVICE_DISCOVERY, discoveryDoc.docJson.toString());
        }
        return PendingIntent.getActivity(context, request.hashCode(), intent, 0);
    }

    private PendingIntent createAuthorizationCancelIntent(
        @NonNull final AuthorizationRequest request,
        @NonNull final Class<? extends Activity> activityClass
    ) {
        final Context context = contextRef.get();
        final Intent intent = new Intent(context, activityClass);
        intent.setAction(ACTION_AFTER_AUTHORIZATION_CANCELLED);
        return PendingIntent.getActivity(context, request.hashCode(), intent, 0);
    }
}
