package com.webasyst.x.add_wa

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.webasyst.x.NavDirections
import com.webasyst.x.barcode.QrHandlerInterface
import com.webasyst.x.R
import com.webasyst.x.common.binding.viewBinding
import com.webasyst.x.common.findRootNavController
import com.webasyst.x.databinding.FragAddWebasystNewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin
import org.koin.core.error.NoBeanDefFoundException
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

class AddWebasystFragment : Fragment(R.layout.frag_add_webasyst_new) {
    private val binding by viewBinding(FragAddWebasystNewBinding::bind)
    private val viewModel: AddWebasystViewModel by viewModels()
    private val showSignOut by lazy {
        arguments?.getBoolean("showSignOut") ?: false
    }

    private val qrHandlerModule = module {
        factory <QrHandlerInterface>{
            viewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getKoin().loadModules(listOf(qrHandlerModule), true)
    }

    override fun onDestroy() {
        super.onDestroy()
        getKoin().unloadModules(listOf(qrHandlerModule))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showSignOut = showSignOut

        binding.viewModel = viewModel
        binding.fragment = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.frame) { view, windowInsets ->
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars()
                )
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.konfetti.collectLatest {
                if (it) {
                    binding.konfetti.start(party)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel
                    .emailNotBlank()
                    .combine(viewModel.installationsEmpty()) { a, b -> a to b }
                    .distinctUntilChanged()
                    .collect { (emailNotBlank, installationsEmpty) ->
                        if (!installationsEmpty) {
                            binding.aboutWebasyst.setText(R.string.add_webasyst_btn_how_webasyst_id_works)
                            binding.button8.visibility = View.GONE
                            binding.toolbar.setNavigationOnClickListener {
                                it.findNavController().popBackStack()
                            }
                        } else {
                            binding.aboutWebasyst.setText(R.string.add_webasyst_btn_existing_user_help)
                            binding.button8.visibility = View.VISIBLE
                            binding.toolbar.setNavigationOnClickListener {
                                viewModel.onSignOut(it)
                            }
                        }
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.textAddShopInfo.text = resources.getString(
                R.string.add_webasyst_connect_text,
                viewModel.getWaidContact()
            )
            binding.textAddShopInfo.formatWithSpan(R.drawable.ic_info) {
                MaterialAlertDialogBuilder(it.context)
                    .setTitle(R.string.add_webasyst_connect_dialog_title)
                    .setMessage(R.string.add_webasyst_connect_dialog_text)
                    .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    fun navigateToQr(view: View){
        try {
            get() as QrHandlerInterface
        }catch (e: NoBeanDefFoundException){
            getKoin().loadModules(listOf(qrHandlerModule), true)
        }
        view.findRootNavController().navigate(
            AddWebasystFragmentDirections.actionAddWebasystFragmentToQrCodeFragment()
        )
    }

    private fun TextView.formatWithSpan(@DrawableRes imgSrc: Int, onClick: (View)-> Unit) {

        val ssb = SpannableStringBuilder(text)
        val drawable = ContextCompat.getDrawable(context, imgSrc) ?: return
        drawable.mutate()
        drawable.setBounds(0, 0,
            lineHeight,
            lineHeight)
        val startImg = text.indexOf(TAG_IMG)
        ssb.setSpan(
            ImageSpan(drawable),
            startImg,
            startImg + TAG_IMG.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb.setSpan(
            object: ClickableSpan(){
               override fun onClick (v: View) = onClick(v)
            },
            startImg,
            startImg + TAG_IMG.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        movementMethod = LinkMovementMethod.getInstance()

        val regex = TAG_BOLD_REGEX.toRegex()
        var match = regex.find(ssb)
        while (match != null) {
            ssb.setSpan(
                StyleSpan(Typeface.BOLD),
                match.range.first,
                match.range.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.delete(match.range.last, match.range.last + 1)
            ssb.delete(match.range.first, match.range.first + TAG_BOLD.length)
            match = regex.find(ssb)
        }
        setText(ssb, TextView.BufferType.SPANNABLE)
    }
    companion object{
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
    }
}

private const val TAG_IMG = "@{IMG}"
private const val TAG_BOLD_REGEX = "@\\{B=.+?\\}"
private const val TAG_BOLD = "@{B="
