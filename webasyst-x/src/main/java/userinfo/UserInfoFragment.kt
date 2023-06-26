package com.webasyst.x.userinfo

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.webasyst.x.R
import com.webasyst.x.databinding.NavHeaderAuthorizedBinding

class UserInfoFragment : Fragment() {
    private lateinit var binding: NavHeaderAuthorizedBinding
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(UserInfoViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NavHeaderAuthorizedBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= 29) {
            binding.userpicView.isForceDarkAllowed = false
        }

        viewModel.userpicUrl.observe(viewLifecycleOwner) { url ->
            if (url.isEmpty()) {
                binding.userpicView.setImageResource(R.drawable.ic_userpic_placeholder)
            } else {
                Glide
                    .with(this)
                    .load(url)
                    .circleCrop()
                    .placeholder(R.drawable.ic_userpic_placeholder)
                    .into(binding.userpicView)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.updateUserInfo()
    }
}
