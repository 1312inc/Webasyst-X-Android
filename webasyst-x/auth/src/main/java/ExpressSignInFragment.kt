package com.webasyst.x.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.auth.databinding.FragSignInExpressBinding
import com.webasyst.x.common.PhoneTextWatcher
import com.webasyst.x.common.binding.viewBinding

class ExpressSignInFragment: Fragment(R.layout.frag_sign_in_express) {
    private val binding by viewBinding(FragSignInExpressBinding::bind)
    private val viewModel: SignInViewModel by lazy(LazyThreadSafetyMode.NONE) {
        val activity = requireActivity()
        ViewModelProvider(
            activity,
            SignInViewModel.Factory(activity as SignInViewModel.Navigator, activity.application)
        )[SignInViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val phoneTextWatcher = PhoneTextWatcher()
        binding.phoneInput.addTextChangedListener(phoneTextWatcher)
        binding.toolbar.setNavigationOnClickListener(viewModel::navigateBackFromPhoneInput)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.parseAddAccountCode(arguments?.getString(ARG_CODE))
    }

    companion object{
        const val TAG = "ExpressSignInFragment"
        const val ARG_CODE = "ADD_QR_CODE"
    }
}
