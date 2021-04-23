package com.webasyst.x.userinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.databinding.NavHeaderAuthorizedBinding
import kotlinx.android.synthetic.main.nav_header_authorized.userpicView

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
                Glide
                    .with(this)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_userpic_placeholder)
                    .into(userpicView)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.updateUserInfo()
    }

    companion object {
        private const val MAX_USERPIC_AGE = 1000 * 60 * 60 * 2 // 2 hours
    }
}
