package com.webasyst.x.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.R
import com.webasyst.x.databinding.FragAuthBinding
import com.webasyst.x.util.getActivity

class AuthFragment : Fragment() {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(AuthViewModel::class.java).also {
            val state = arguments?.getInt("state")
            if (state != null) {
                it.state.value = state
            }
        }
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

    override fun onResume() {
        super.onResume()

        val intent = view?.getActivity()?.intent
        if (null == intent) {
            viewModel.state.value = AuthViewModel.STATE_IDLE
            return
        }
    }
}
