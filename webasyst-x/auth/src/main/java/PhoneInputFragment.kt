package com.webasyst.x.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.auth.databinding.FragSignInPhoneBinding
import com.webasyst.x.common.PhoneTextWatcher

class PhoneInputFragment : Fragment(R.layout.frag_sign_in_phone) {
    private lateinit var binding: FragSignInPhoneBinding
    private val viewModel: SignInViewModel by lazy(LazyThreadSafetyMode.NONE) {
        val activity = requireActivity()
        ViewModelProvider(
            activity,
            SignInViewModel.Factory(activity as SignInViewModel.Navigator, activity.application)
        )[SignInViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragSignInPhoneBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val phoneTextWatcher = PhoneTextWatcher()
        binding.phoneInput.addTextChangedListener(phoneTextWatcher)
        binding.toolbar.setNavigationOnClickListener(viewModel::navigateBackFromPhoneInput)
    }
}
