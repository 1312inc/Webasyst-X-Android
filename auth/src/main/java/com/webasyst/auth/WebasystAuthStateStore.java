package com.webasyst.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This singleton class holds the reference to current {@link AuthState}
 * and persists it in {@link SharedPreferences}.
 *
 * Said sate can be observed with {@link WebasystAuthStateStore.Observer}
 * (see {@link #addObserver}, {@link #removeObserver}).
 */
public final class WebasystAuthStateStore {
    private static final String STORE_NAME = "webasyst_auth_store";
    private static final String STATE_KEY = "state";
    private static final String TAG = "WebasystAuthState";

    private final SharedPreferences preferences;
    private final AtomicReference<AuthState> currentStateRef;
    private final Handler mainThreadHandler;
    private final Set<Observer> observers;

    private WebasystAuthStateStore(Context context) {
        preferences = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
        mainThreadHandler = new Handler(context.getMainLooper());
        observers = new HashSet<>();
        currentStateRef = new AtomicReference<>();
    }

    private volatile static WebasystAuthStateStore instance = null;
    /**
     * Returns singleton instance of {@link WebasystAuthStateStore}
     * @param context Android {@link Context}
     */
    public static WebasystAuthStateStore getInstance(Context context) {
        if (null != instance) return instance;
        synchronized (STORE_NAME) {
            if (instance == null) instance = new WebasystAuthStateStore(context.getApplicationContext());
            return instance;
        }
    }

    @NonNull
    public final AuthState getCurrent() {
        final AuthState currentState = currentStateRef.get();
        if (null != currentState) return currentState;

        final AuthState state = readState();
        if (currentStateRef.compareAndSet(null, state)) {
            notifyObservers(state);
            return state;
        } else {
            AuthState newState = currentStateRef.get();
            notifyObservers(newState);
            return newState;
        }
    }

    @NonNull
    AuthState replace(@NonNull final AuthState state) {
        writeState(state);
        currentStateRef.set(state);
        notifyObservers(state);
        return state;
    }

    @NonNull
    AuthState updateAfterAuthorization(@Nullable final AuthorizationResponse response,
                                       @Nullable final AuthorizationException exception) {
        final AuthState current = getCurrent();
        current.update(response, exception);
        return replace(current);
    }

    @NonNull
    AuthState updateAfterTokenResponse(@Nullable final TokenResponse response,
                                       @Nullable final AuthorizationException exception) {
        final AuthState current = getCurrent();
        current.update(response, exception);
        return replace(current);
    }

    @NonNull
    private AuthState readState() {
        final String currentState = preferences.getString(STATE_KEY, null);
        if (currentState == null) return new AuthState();

        try {
            Log.w(TAG, "Failed to deserialize stored auth state - discarding");
            return AuthState.jsonDeserialize(currentState);
        } catch (JSONException e) {
            return new AuthState();
        }
    }

    private void writeState(@Nullable final AuthState state) {
        final SharedPreferences.Editor editor = preferences.edit();
        try {
            if (null == state) {
                editor.remove(STATE_KEY);
            } else {
                editor.putString(STATE_KEY, state.jsonSerializeString());
            }
        } finally {
            editor.apply();
        }
    }

    /**
     * Adds new {@link Observer} to this {@link WebasystAuthStateStore}
     * @param observer Observer to add
     * @param callWithCurrentState If true, call {@link Observer#onAuthStateChange(AuthState)} right away
     */
    @AnyThread
    void addObserver(@NonNull final Observer observer, boolean callWithCurrentState) {
        synchronized (observers) {
            observers.add(observer);
            if (callWithCurrentState) {
                observer.onAuthStateChange(getCurrent());
            }
        }
    }

    /**
     * A helper function to call {@link #addObserver(Observer, boolean)}
     * with second parameter set to true.
     * @param observer
     */
    @AnyThread
    public void addObserver(@NonNull final Observer observer) {
        addObserver(observer, true);
    }

    /**
     * Removes {@link Observer} from this {@link WebasystAuthStateStore}
     * @param observer Observer to remove
     */
    @AnyThread
    public void removeObserver(@NonNull final Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    private void notifyObservers(@NonNull final AuthState state) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (observers) {
                    for (final Observer observer : observers) {
                        observer.onAuthStateChange(state);
                    }
                }
            }
        });
    }

    /**
     * Interface to be implemented by classes interested in authentication state changes
     */
    public interface Observer {
        /**
         * Method to be called when authentication state changes.
         * Guaranteed to be called on Main (UI) thread.
         */
        @UiThread
        void onAuthStateChange(AuthState state);
    }
}
