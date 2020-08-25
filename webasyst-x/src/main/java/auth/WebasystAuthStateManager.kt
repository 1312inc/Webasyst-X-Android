package com.webasyst.x.auth

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.annotation.UiThread
import androidx.core.content.edit
import com.webasyst.x.util.SingletonHolder
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.TokenResponse
import org.json.JSONException
import java.util.concurrent.atomic.AtomicReference

class WebasystAuthStateManager private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    private val currentStateRef = AtomicReference<AuthState>()
    private val observers: MutableSet<AuthStateObserver> = HashSet()

    fun getCurrent(): AuthState {
        val currentState = currentStateRef.get()
        if (null != currentState) return currentState

        val state = readState()
        return if (currentStateRef.compareAndSet(null, state)) {
            notifyObservers(state)
            state
        } else {
            currentStateRef.get()
        }
    }

    fun replace(state: AuthState): AuthState {
        writeState(state)
        currentStateRef.set(state)
        notifyObservers(state)
        return state
    }

    fun updateAfterAuthorization(response: AuthorizationResponse?, e: AuthorizationException?): AuthState {
        val current = getCurrent()
        current.update(response, e)
        return replace(current)
    }

    fun updateAfterTokenResponse(response: TokenResponse?, e: AuthorizationException?): AuthState {
        val current = getCurrent()
        current.update(response, e)
        return replace(current)
    }

    private fun readState(): AuthState {
        val currentState = prefs.getString(KEY_STATE, null) ?:
            return AuthState()

        return try {
            AuthState.jsonDeserialize(currentState)
        } catch (e: JSONException) {
            Log.w(TAG, "Failed to deserialize stored auth state - discarding")
            AuthState()
        }
    }

    private fun writeState(state: AuthState?) {
        prefs.edit {
            if (null == state) {
                remove(KEY_STATE)
            } else {
                putString(KEY_STATE, state.jsonSerializeString())
            }
        }
    }

    /**
     * If [callWithCurrentState], calls [observers]'s [AuthStateObserver.onChange] right away.
     */
    fun addObserver(observer: AuthStateObserver, callWithCurrentState: Boolean = true) = synchronized(observers) {
        observers.add(observer)
        if (callWithCurrentState) {
            Looper.getMainLooper().run {
                observer.onChange(getCurrent())
            }
        }
    }

    fun removeObserver(observer: AuthStateObserver) = synchronized(observers) {
        observers.remove(observer)
    }

    private fun notifyObservers(state: AuthState) = synchronized(observers) {
        Looper.getMainLooper().run {
            observers.forEach { it.onChange(state) }
        }
    }

    /**
     * Interface to be implemented by classes interested
     */
    interface AuthStateObserver {
        /**
         * Method to be called when authentication state changes.
         * Guaranteed to be called on Main (UI) thread.
         */
        @UiThread
        fun onChange(state: AuthState)
    }

    companion object : SingletonHolder<WebasystAuthStateManager, Context>(::WebasystAuthStateManager) {
        private const val TAG = "StateStore"
        private const val STORE_NAME = "webasyst_auth_store"
        private const val KEY_STATE = "state"
    }
}
