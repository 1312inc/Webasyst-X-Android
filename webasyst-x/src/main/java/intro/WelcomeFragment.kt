package com.webasyst.x.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.databinding.FragIntroWelcomeBinding

class WelcomeFragment : Fragment() {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(WelcomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragIntroWelcomeBinding
        .inflate(inflater, container, false)
        .let { binding ->
            binding.viewModel = viewModel
            binding.root
        }
}
