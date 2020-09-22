package com.webasyst.auth;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Webasyst ID (WAID) configuration
 */
public class WebasystAuthConfiguration {
    /** Application's id. TODO: add add information on creating new application */
    final String clientId;
    /** Authentication URL */
    final Uri authEndpoint;
    /** Token URL */
    final Uri tokenEndpoint;
    /** Internal URL used in authentication flow */
    final Uri callbackUri;
    /** Scope(s) to request */
    final Set<String> scope;

    private WebasystAuthConfiguration(final Builder builder) {
        clientId = builder.clientId;
        authEndpoint = Uri.parse(builder.authEndpoint);
        tokenEndpoint = Uri.parse(builder.tokenEndpoint);
        callbackUri = Uri.parse(builder.callbackUri);
        scope = builder.scope;
    }

    /**
     * {@link #WebasystAuthConfiguration} builder
     */
    public static class Builder {
        private @Nullable String clientId = null;
        private @Nullable String authEndpoint = null;
        private @Nullable String tokenEndpoint = null;
        private @Nullable String callbackUri = null;
        private @Nullable Set<String> scope = null;

        Builder(@Nullable WebasystAuthConfiguration configuration) {
            if (null != configuration) {
                clientId = configuration.clientId;
                authEndpoint = configuration.authEndpoint.toString();
                tokenEndpoint = configuration.tokenEndpoint.toString();
                callbackUri = configuration.callbackUri.toString();
            }
        }

        public void setClientId(@NonNull String clientId) {
            this.clientId = clientId;
        }

        /**
         * Convenient method to set Authentication and Token endpoints.
         * @param host Webasyst host (eg. <code>https://www.webasyst.com</code>. Note the leading https://)
         */
        public void setHost(@NonNull String host) {
            setAuthEndpoint(host + "/id/oauth2/auth/code");
            setTokenEndpoint(host + "/id/oauth2/auth/token");
        }

        public void setAuthEndpoint(@NonNull String authEndpoint) {
            this.authEndpoint = authEndpoint;
        }

        public void setTokenEndpoint(@NonNull String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
        }

        public void setCallbackUri(@NonNull String callbackUri) {
            this.callbackUri = callbackUri;
        }

        public void setScope(@NonNull String... scope) {
            this.scope = new HashSet<>(Arrays.asList(scope));
        }

        /**
         * Creates {@link WebasystAuthConfiguration}.
         * @throws IllegalStateException if this {@link #Builder} is not fully configured.
         */
        public WebasystAuthConfiguration build() throws IllegalStateException {
            if (null == this.clientId) throw new IllegalStateException("Client ID must be set");
            if (null == this.authEndpoint) throw new IllegalStateException("Auth endpoint must be set");
            if (null == this.tokenEndpoint) throw new IllegalStateException("Token endpoint must be set");
            if (null == this.callbackUri) throw new IllegalStateException("Callback url must be set");
            if (null == this.scope) throw new IllegalStateException("Scope must be set");
            return new WebasystAuthConfiguration(this);
        }
    }
}
