package com.webasyst.auth;

import android.net.Uri;

import androidx.annotation.Nullable;

public class WebasystAuthConfiguration {
    final String clientId;
    final Uri authEndpoint;
    final Uri tokenEndpoint;
    final Uri callbackUri;

    private WebasystAuthConfiguration(final Builder builder) {
        clientId = builder.clientId;
        authEndpoint = Uri.parse(builder.authEndpoint);
        tokenEndpoint = Uri.parse(builder.tokenEndpoint);
        callbackUri = Uri.parse(builder.callbackUri);
    }

    public static class Builder {
        private String clientId;
        private String authEndpoint;
        private String tokenEndpoint;
        private String callbackUri;

        Builder(@Nullable WebasystAuthConfiguration configuration) {
            if (null != configuration) {
                clientId = configuration.clientId;
                authEndpoint = configuration.authEndpoint.toString();
                tokenEndpoint = configuration.tokenEndpoint.toString();
                callbackUri = configuration.callbackUri.toString();
            }
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public void setAuthEndpoint(String authEndpoint) {
            this.authEndpoint = authEndpoint;
        }

        public void setTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
        }

        public void setCallbackUri(String callbackUri) {
            this.callbackUri = callbackUri;
        }

        public WebasystAuthConfiguration build() {
            return new WebasystAuthConfiguration(this);
        }
    }
}
