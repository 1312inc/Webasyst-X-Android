package com.webasyst.x.auth

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.auth.databinding.FragSignInPhoneBinding

class PhoneInputFragment : Fragment(R.layout.frag_sign_in_phone) {
    private lateinit var binding: FragSignInPhoneBinding
    private val viewModel: SignInViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(requireActivity())[SignInViewModel::class.java]
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

    private class PhoneTextWatcher : TextWatcher {
        private val formatter = PhoneNumberFormattingTextWatcher()
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            formatter.beforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            formatter.onTextChanged(s, start, before, count)
        }

        override fun afterTextChanged(s: Editable) {
            val str = s.trim()
            if (!str.startsWith("+")) {
                s.insert(0, "+")
            }
            formatter.afterTextChanged(s)
        }
    }
}
