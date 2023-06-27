package com.webasyst.x.pin_code

import android.content.Context
import android.icu.util.GregorianCalendar
import android.os.Build
import androidx.core.content.edit
import kotlin.math.abs

class PinCodeStoreImpl(context: Context) : PinCodeStore {

    private val sharedPreferences = context.getSharedPreferences(PREFS_STORE, Context.MODE_PRIVATE)

    override fun hasPinCode(): Boolean {
        return try {
            sharedPreferences.contains(PIN)
        } catch (e: Exception) {
            false
        }
    }

    override fun hasPinCodeWithTime(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val currentTime = GregorianCalendar.getInstance().time.time
                val lastTime = sharedPreferences.getLong(TIME, -1L)
                if (abs(currentTime - lastTime) < FIVE_MIN)
                    false
                else sharedPreferences.contains(PIN)

            } else
                sharedPreferences.contains(PIN)
        } catch (e: Exception) {
            false
        }
    }

    override fun setPinCode(pinCode: Int): Boolean {
        return try {
            sharedPreferences.edit {
                putInt(PIN, pinCode)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun resetPinCode(pinCode: Int): Boolean {
        return try {
            sharedPreferences.edit {
                putInt(PIN, pinCode)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun checkPinCode(pinCode: Int): Boolean {
        return try {
            setLastEnterTime()
            pinCode == sharedPreferences.getInt(PIN, -1)
        } catch (e: Exception) {
            false
        }
    }

    override fun removePinCode(): Boolean {
        return try {
            sharedPreferences.edit {
                remove(PIN)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun setLastEnterTime(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val currentTime = GregorianCalendar.getInstance().time.time
            sharedPreferences.edit {
                putLong(TIME, currentTime)
            }
            true
        } else false
    }

    companion object {
        private const val PREFS_STORE = "pin_store"
        private const val PIN = "pin_code"
        private const val TIME = "last_time"
        private const val FIVE_MIN = 300000L // in milliseconds
    }
}
