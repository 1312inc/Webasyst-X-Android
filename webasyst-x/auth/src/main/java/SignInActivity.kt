package com.webasyst.x.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.barcode.QRCodeFragment
import com.webasyst.x.common.XComponentProvider
import net.openid.appauth.AuthState

class SignInActivity : AppCompatActivity(), WebasystAuthStateStore.Observer, SignInViewModel.Navigator {
    lateinit var frame: FragmentContainerView

    private val xComponentProvider by lazy(LazyThreadSafetyMode.NONE) {
        (application as XComponentProvider)
    }
    private val authState: WebasystAuthStateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)
        frame = findViewById(R.id.frame)

        val fragment = when(intent?.getIntExtra(ARG_AUTH_TYPE, AUTH_TYPE_PHONE)){
            AUTH_TYPE_QR -> QRCodeFragment()
            else -> PhoneInputFragment()
        }
        supportFragmentManager.beginTransaction().also { transaction ->
            transaction.add(R.id.frame, fragment)
            transaction.commit()
        }
    }

    override fun onStart() {
        super.onStart()
        authState.addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        authState.removeObserver(this)
    }

    override fun onAuthStateChange(authState: AuthState) {
        if (authState.isAuthorized) {
            val intent = Intent(this, xComponentProvider.mainActivityClass())
            startActivity(intent)
            finish()
        }
    }

    override fun navigateFromPhoneInputToCodeInput() {
        if (supportFragmentManager.findFragmentByTag("code_input") == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.frame, CodeInputFragment(), "code_input")
                addToBackStack("code_input")
                commit()
            }
        }
    }

    override fun popBackStack() {
        supportFragmentManager.popBackStack()
    }

    companion object{
        const val ARG_AUTH_TYPE = "auth_type"
        const val AUTH_TYPE_PHONE = 0
        const val AUTH_TYPE_QR = 1
    }
}
