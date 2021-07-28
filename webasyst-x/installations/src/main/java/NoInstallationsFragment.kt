package com.webasyst.x.installations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.webasyst.x.installations.databinding.FragNoInstallationsBinding

class NoInstallationsFragment : Fragment(R.layout.frag_no_installations) {
    lateinit var binding: FragNoInstallationsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragNoInstallationsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_hamburger)
        }

        binding.buttonAddWebasyst.setOnClickListener {
            it.findNavController().navigate(R.id.action_noInstallationsFragment_to_addWebasystFragment)
        }
    }
}
