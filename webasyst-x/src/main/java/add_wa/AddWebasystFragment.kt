package com.webasyst.x.add_wa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.R
import com.webasyst.x.databinding.FragAddWebasystBinding

class AddWebasystFragment : Fragment() {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(AddWebasystViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = DataBindingUtil.inflate<FragAddWebasystBinding>(
        inflater,
        R.layout.frag_add_webasyst,
        container,
        false
    ).let { binding ->
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }
}
