package com.webasyst.x.pin_code

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.addCallback
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.webasyst.x.common.UserInfoNavigator
import com.webasyst.x.common.binding.viewBinding
import com.webasyst.x.pin_code.databinding.FragPincodeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import kotlin.properties.Delegates


class PinCodeFragment() : Fragment(R.layout.frag_pincode) {

    private val binding by viewBinding(FragPincodeBinding::bind)
    private val pinCodeStore = get<PinCodeStore>()
    private var code = EMPTY_CODE
    private var codeTmp = EMPTY_CODE //code for repeat
    private lateinit var authController: UserInfoNavigator

    private var forRemove by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authController = requireActivity() as UserInfoNavigator
        forRemove = requireArguments().getBoolean(BUNDLE_FOR_REMOVE, false)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (!pinCodeStore.hasPinCode()) authController.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindNumbersListeners()
        bindBiometricAuth()
        bindRemoveClickListener()
        bindExitClickListener()
        insertMargin()
        if (forRemove || !pinCodeStore.hasPinCode()) {
            binding.bExit.apply {
                visibility = View.VISIBLE
                setOnClickListener { authController.popBackStack() }
            }
        }
        if (!pinCodeStore.hasPinCode()) {
            binding.bFingerprint.visibility = View.INVISIBLE
            changeText(R.string.password_set)
        }
    }

    private fun bindExitClickListener(forCancel: Boolean = false) {
        if (forRemove || !pinCodeStore.hasPinCode()) {
            binding.bExit.apply {
                visibility = View.VISIBLE
                if (forCancel) {
                    text = getString(R.string.password_cancel)
                    setOnClickListener {
                        codeTmp = EMPTY_CODE
                        fillCircles(EMPTY_CODE_INT)
                        changeText(R.string.password_set)
                        bindExitClickListener()
                    }
                } else {
                    text = getString(R.string.password_exit)
                    setOnClickListener { authController.popBackStack() }
                }

            }
        }
    }

    private fun insertMargin() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                bottomMargin = insets.bottom
                topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun bindRemoveClickListener() {
        binding.bRemove.setOnClickListener {
            if (code.length != EMPTY_CODE_INT) {
                code = code.dropLast(1)
                fillCircles(code.length)
            }
        }
    }

    private fun bindBiometricAuth() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricManager = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != ERROR_USER_CANCELED && errorCode != ERROR_NEGATIVE_BUTTON)
                        Snackbar.make(requireView(), errString, Snackbar.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    if (forRemove) {
                        pinCodeStore.removePinCode()
                        requireActivity().supportFragmentManager.setFragmentResult(
                            REQUEST_PASSWORD_SUCCESS,
                            bundleOf(BUNDLE_PASSWORD_SUCCESS to getString(R.string.password_removed_successfully))
                        )
                    } else pinCodeStore.setLastEnterTime()
                    authController.popBackStack()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })


        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.finger_print_login))
            .setNegativeButtonText(getString(R.string.use_numeric_password))
            .build()

        binding.bFingerprint.setOnClickListener {
            biometricManager.authenticate(promptInfo)
        }
        if (pinCodeStore.hasPinCode()) biometricManager.authenticate(promptInfo)
    }

    private fun bindNumbersListeners() {
        binding.b0.setOnClickListener(numberClickListener)
        binding.b1.setOnClickListener(numberClickListener)
        binding.b2.setOnClickListener(numberClickListener)
        binding.b3.setOnClickListener(numberClickListener)
        binding.b4.setOnClickListener(numberClickListener)
        binding.b5.setOnClickListener(numberClickListener)
        binding.b6.setOnClickListener(numberClickListener)
        binding.b7.setOnClickListener(numberClickListener)
        binding.b8.setOnClickListener(numberClickListener)
        binding.b9.setOnClickListener(numberClickListener)
    }

    private val numberClickListener = View.OnClickListener {
        if (it is Button) {
            code += it.text

            when (pinCodeStore.hasPinCode()) {
                true -> {
                    if (code.length < MAX_LENGTH_SIZE) {
                        fillCircles(code.length)
                    } else if (pinCodeStore.checkPinCode(code.toInt())) {
                        fillCircles(code.length, false)
                        if (forRemove) {
                            pinCodeStore.removePinCode()
                            requireActivity().supportFragmentManager.setFragmentResult(
                                REQUEST_PASSWORD_SUCCESS,
                                bundleOf(BUNDLE_PASSWORD_SUCCESS to getString(R.string.password_removed_successfully))
                            )
                        }
                        authController.popBackStack()
                    } else {
                        lifecycle.coroutineScope.launch {
                            fillCircles(code.length)
                            changeText(R.string.password_error, DELAY_FOR_RETURN_TEXT)
                            animatePasswordDots()
                            delay(DELAY_FOR_ANIM) //for last dots filling
                            fillCircles(EMPTY_CODE_INT)
                            code = EMPTY_CODE
                        }
                    }
                }

                false -> {
                    if (code.length < MAX_LENGTH_SIZE) {
                        fillCircles(code.length)
                    } else if (codeTmp != EMPTY_CODE) {
                        if (codeTmp == code) {
                            pinCodeStore.setPinCode(code.toInt())
                            requireActivity().supportFragmentManager.setFragmentResult(
                                REQUEST_PASSWORD_SUCCESS,
                                bundleOf(BUNDLE_PASSWORD_SUCCESS to getString(R.string.password_set_successfully))
                            )
                            authController.popBackStack()
                        } else {
                            lifecycle.coroutineScope.launch {
                                changeText(R.string.password_mismatch, DELAY_FOR_RETURN_TEXT)
                                animatePasswordDots()
                                fillCircles(code.length)
                                delay(DELAY_FOR_ANIM)
                                code = EMPTY_CODE//for last dots filling
                                fillCircles(EMPTY_CODE_INT)
                            }
                        }
                    } else {
                        lifecycle.coroutineScope.launch {
                            fillCircles(code.length, false)
                            changeText(R.string.password_repeat)
                            bindExitClickListener(true)
                            delay(DELAY_FOR_ANIM) //for last dots filling
                            fillCircles(EMPTY_CODE_INT)
                            codeTmp = code
                            code = EMPTY_CODE
                        }
                    }
                }
            }
        }
    }

    private fun fillCircles(countFill: Int, wrongPassword: Boolean = true) {
        if (countFill > MAX_LENGTH_SIZE) return

        if (countFill == EMPTY_CODE_INT) {
            binding.bRemove.apply {
                setColorFilter(getColor(R.color.removeButtonDisable))
                isEnabled = false
            }
        } else {
            binding.bRemove.apply {
                setColorFilter(getColor(R.color.removeButton))
                isEnabled = true
            }
        }

        val listCircles = listOf(
            binding.circleNumber1,
            binding.circleNumber2,
            binding.circleNumber3,
            binding.circleNumber4
        )
        listCircles.forEach {
            it.setCardBackgroundColor(
                getColor(R.color.disableDots)
            )
        }

        for (i in 0 until countFill) {
            listCircles[i].setCardBackgroundColor(
                getColor(R.color.enterSymbolDots)
            )
        }
        if (countFill == MAX_LENGTH_SIZE && wrongPassword)
            for (i in 0 until countFill) {
                listCircles[i].setCardBackgroundColor(
                    getColor(R.color.wrongDots)
                )
            }
    }

    private suspend fun animatePasswordDots() {
        delay(DELAY_BEFORE_ANIM)
        val shake: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        binding.codeDots.startAnimation(shake)
    }

    private fun changeText(@StringRes textId: Int, delay: Long = 0L) {
        if (delay == 0L) {
            binding.tvTitlePassword.text = getString(textId)
        } else
            lifecycleScope.launch {
                val textTmp = binding.tvTitlePassword.text
                binding.tvTitlePassword.text = getString(textId)
                delay(delay)
                binding.tvTitlePassword.text = textTmp
            }

    }

    private fun getColor(@ColorRes id: Int) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            resources.getColor(id, requireActivity().theme)
        else resources.getColor(id)

    companion object {
        const val BUNDLE_FOR_REMOVE = "forRemove"
        const val BUNDLE_PASSWORD_SUCCESS = "bundle_password_success"
        const val REQUEST_PASSWORD_SUCCESS = "request_password_success"
    }

}

private const val MAX_LENGTH_SIZE = 4
private const val EMPTY_CODE = ""
private const val EMPTY_CODE_INT = 0
private const val DELAY_FOR_ANIM = 70L
private const val DELAY_BEFORE_ANIM = 30L
private const val DELAY_FOR_RETURN_TEXT = 2000L
