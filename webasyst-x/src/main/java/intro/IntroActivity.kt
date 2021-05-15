package com.webasyst.x.intro

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import net.openid.appauth.AuthState

class IntroActivity : AppIntro(), WebasystAuthStateStore.Observer {
    override val layoutId = R.layout.activity_intro

    private val intro: View by lazy(LazyThreadSafetyMode.NONE) { findViewById(R.id.background) }
    private val signingIn: View by lazy(LazyThreadSafetyMode.NONE) { findViewById(R.id.signingIn) }

    private val authState: WebasystAuthStateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(this)
    }
    private val authHelper by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthHelper(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (WebasystAuthHelper.ACTION_UPDATE_AFTER_AUTHORIZATION == intent.action) {
            intro.visibility = View.GONE
            signingIn.visibility = View.VISIBLE
        } else {
            intro.visibility = View.VISIBLE
            signingIn.visibility = View.GONE
        }
        authHelper.handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        authState.addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        authState.removeObserver(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (null != intent) {
            onNewIntent(intent)
        }

        isSkipButtonEnabled = false

        @ColorInt val highlightColor = resolveColor(R.attr.colorControlHighlight)
        @ColorInt val controlColor = resolveColor(R.attr.colorPrimary)

        setIndicatorColor(resolveColor(R.attr.colorPrimary), highlightColor)
        setSeparatorColor(highlightColor)
        setColorSkipButton(controlColor)
        setBackArrowColor(controlColor)
        setNextArrowColor(controlColor)

        addSlide(Fragment(R.layout.frag_intro_hello_world))

        addSlide(Fragment(R.layout.frag_intro_projects))

        addSlide(GithubFragment())

        addSlide(WelcomeFragment())
    }

    override fun onPageSelected(position: Int) {
        findViewById<Button>(R.id.done).visibility = View.INVISIBLE
    }

    override fun onAuthStateChange(authState: AuthState) {
        if (authState.isAuthorized) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private val typedValue = TypedValue()
    private fun resolveColor(attr: Int): Int {
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
