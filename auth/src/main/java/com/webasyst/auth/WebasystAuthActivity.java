package com.webasyst.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Basic authentication activity.
 * Webasyst client applications should either inherit from it or use it as a reference.
 */
public abstract class WebasystAuthActivity extends AppCompatActivity {
    private WebasystAuthHelper authHelper;

    /**
     * Handles authentication callback in case Activity is created with launchMode=singleInstance
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        authHelper.handleIntent(intent);
    }

    /**
     * This override adds authentication callback handling
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new WebasystAuthHelper(this);

        final Intent intent = getIntent();
        if (null != intent) {
            authHelper.handleIntent(intent);
        }
    }

    /**
     * Performs the whole sign in process.
     * {@link WebasystAuthService} must be configured before attempting interactions with WAID.
     * @see WebasystAuthService#configure(WebasystAuthConfiguration)
     */
    public final void waSignIn() {
        authHelper.signIn(this.getClass());
    }

    /**
     * Performs sign out process.
     * {@link WebasystAuthService} must be configured before attempting interactions with WAID.
     * @see WebasystAuthService#configure(WebasystAuthConfiguration)
     */
    public final void waSignOut() {
        authHelper.signOut();
    }
}
