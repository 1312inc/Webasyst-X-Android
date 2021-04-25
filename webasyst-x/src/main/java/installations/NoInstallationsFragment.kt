package com.webasyst.x.installations

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.webasyst.x.R
import kotlinx.android.synthetic.main.frag_no_installations.buttonAddWebasyst

class NoInstallationsFragment : Fragment(R.layout.frag_no_installations) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_hamburger)
        }

        buttonAddWebasyst.setOnClickListener {
            it.findNavController().navigate(R.id.action_noInstallationsFragment_to_addWebasystFragment)
        }
    }
}
