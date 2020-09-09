package com.webasyst.x.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.auth.WebasystAuthHelper
import com.webasyst.x.R
import com.webasyst.x.databinding.FragAuthBinding
import com.webasyst.x.util.getActivity

class AuthFragment : Fragment() {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(AuthViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<FragAuthBinding>(
        inflater,
        R.layout.frag_auth,
        container,
        false
    ).let { binding ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.getActivity()?.intent?.let { intent ->
            if (intent.action == WebasystAuthHelper.ACTION_UPDATE_AFTER_AUTHORIZATION) {
                viewModel.state.value = AuthViewModel.STATE_AUTHENTICATING
            }
        }
    }
}
