package com.webasyst.x.intro

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.annotation.ColorInt
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.auth.WebasystAuthStateStore
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import net.openid.appauth.AuthState

class IntroActivity : AppIntro(), WebasystAuthStateStore.Observer {
    private val authState: WebasystAuthStateStore by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthStateStore.getInstance(this)
    }
    private val authHelper by lazy(LazyThreadSafetyMode.NONE) {
        WebasystAuthHelper(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
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

        isSkipButtonEnabled = false

        @ColorInt val backgroundColor = resolveColor(R.attr.backgroundColor)
        @ColorInt val titleColor = resolveColor(R.attr.colorOnSurfaceHighEmphasis)
        @ColorInt val descriptionColor = titleColor
        @ColorInt val highlightColor = resolveColor(R.attr.colorControlHighlight)
        @ColorInt val controlColor = resolveColor(R.attr.colorPrimary)

        setIndicatorColor(resolveColor(R.attr.colorPrimary), highlightColor)
        setSeparatorColor(highlightColor)
        setColorSkipButton(controlColor)
        setBackArrowColor(controlColor)
        setNextArrowColor(controlColor)

        addSlide(AppIntroFragment.newInstance(
            title = getString(R.string.intro_slide_1_title),
            description = getString(R.string.intro_slide_1_description),
            imageDrawable = R.drawable.x_logo,
            backgroundColor = backgroundColor,
            titleColor = titleColor,
            descriptionColor = descriptionColor,
        ))

        addSlide(AppIntroFragment.newInstance(
            title = getString(R.string.intro_slide_2_title),
            description = getString(R.string.intro_slide_2_description),
            imageDrawable = R.drawable.x_logo,
            backgroundColor = backgroundColor,
            titleColor = titleColor,
            descriptionColor = descriptionColor,
        ))

        addSlide(AppIntroFragment.newInstance(
            title = getString(R.string.intro_slide_3_title),
            description = getString(R.string.intro_slide_3_description),
            imageDrawable = R.drawable.x_logo,
            backgroundColor = backgroundColor,
            titleColor = titleColor,
            descriptionColor = descriptionColor,
        ))

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
