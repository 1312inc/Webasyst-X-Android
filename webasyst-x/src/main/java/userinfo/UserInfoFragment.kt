package com.webasyst.x.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.databinding.NavHeaderAuthorizedBinding
import com.webasyst.x.util.USERPIC_FILE
import com.webasyst.x.util.decodeBitmap
import com.webasyst.x.util.getCacheFile
import kotlinx.android.synthetic.main.nav_header_authorized.userpicView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UserInfoFragment : Fragment() {
    private val waidClient by lazy(LazyThreadSafetyMode.NONE) {
        (requireActivity().application as WebasystXApplication)
            .waidClient
    }
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(UserInfoViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<NavHeaderAuthorizedBinding>(
        inflater,
        R.layout.nav_header_authorized,
        container,
        false
    ).let { binding ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.userpicUrl.observe(viewLifecycleOwner) { url ->
            if (url.isEmpty()) {
                userpicView.setImageResource(R.drawable.ic_userpic_placeholder)
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    val userpicFile = requireContext().getCacheFile(USERPIC_FILE)
                    try {
                        if (!userpicFile.exists() ||
                            userpicFile.lastModified() + MAX_USERPIC_AGE < System.currentTimeMillis()
                        ) {
                            waidClient.downloadUserpic(url, userpicFile)
                        }
                        updateUserpicFromFile(userpicFile)
                    } catch (e: Throwable) {
                        updateUserpicFromFile(userpicFile)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.updateUserInfo()
    }

    private suspend fun updateUserpicFromFile(userpicFile: File) {
        if (userpicFile.exists()) {
            userpicView.setImageBitmap(withContext(Dispatchers.Default) {
                userpicFile.decodeBitmap(userpicView)
            })
        } else {
            userpicView.setImageResource(R.drawable.ic_userpic_placeholder)
        }
    }

    companion object {
        private const val MAX_USERPIC_AGE = 1000 * 60 * 60 * 2 // 2 hours
    }
}
