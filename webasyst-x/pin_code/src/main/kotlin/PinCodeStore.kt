package com.webasyst.x.pin_code

interface PinCodeStore {
    fun hasPinCode(): Boolean
    fun hasPinCodeWithTime(): Boolean
    fun setPinCode(pinCode: Int): Boolean
    fun resetPinCode(pinCode: Int): Boolean
    fun checkPinCode(pinCode: Int): Boolean //enter pin
    fun removePinCode(): Boolean
    fun setLastEnterTime(): Boolean
}
