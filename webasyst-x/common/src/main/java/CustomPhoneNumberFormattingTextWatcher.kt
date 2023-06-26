package com.webasyst.x.common

import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable


class CustomPhoneNumberFormattingTextWatcher: PhoneNumberFormattingTextWatcher() {
    override fun afterTextChanged(s: Editable?) {
        super.afterTextChanged(s)
        val str = s?.trim()
        if (str?.startsWith("+") == false) {
            s.insert(0, "+")
        }
    }
}
