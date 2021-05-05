package com.webasyst.x.intro

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.webasyst.x.R
import com.webasyst.x.installations.InstallationListViewModel

class LoadingFragment : Fragment(R.layout.frag_loading) {
    val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(requireActivity()).get(InstallationListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) {
            Log.d("frag_loading", "Installation list state: $it")
        }
    }
}
