package com.webasyst.x.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.webasyst.x.MainActivity
import com.webasyst.x.R
import com.webasyst.x.databinding.FragBlogRootBinding
import com.webasyst.x.util.BackPressHandler

class BlogRootFragment : Fragment(), BackPressHandler {
    private lateinit var binding: FragBlogRootBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragBlogRootBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).addBackPressHandler(this)

        val navController = binding.blogRoot.findNavController()
        navController.setGraph(R.navigation.nav_blog, arguments)
    }

    override fun onBackPressed(): Boolean =
        try {
            val navController = binding.blogRoot.findNavController()
            navController.popBackStack()
        } catch (e: IllegalStateException) {
            // This was called before onCreateView
            false
        }
}
