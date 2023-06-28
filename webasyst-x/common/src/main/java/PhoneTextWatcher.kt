package com.webasyst.x.common

import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher

class PhoneTextWatcher : TextWatcher {
    private val formatter = PhoneNumberFormattingTextWatcher()
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        formatter.beforeTextChanged(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        formatter.onTextChanged(s, start, before, count)
    }

    override fun afterTextChanged(s: Editable) {
        val str = s.trim()
        if (!str.startsWith("+")) {
            s.insert(0, "+")
        }
        formatter.afterTextChanged(s)
    }
}
