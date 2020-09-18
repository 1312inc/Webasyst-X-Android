package com.webasyst.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class WebasystAuthActivity extends AppCompatActivity {
    private WebasystAuthHelper authHelper;

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        authHelper.handleIntent(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new WebasystAuthHelper(this);

        final Intent intent = getIntent();
        if (null != intent) {
            authHelper.handleIntent(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        authHelper.dispose();
    }


    public final void waSignIn() {
        authHelper.signIn(this.getClass());
    }

    public final void waSignOut() {
        authHelper.signOut();
    }
}
