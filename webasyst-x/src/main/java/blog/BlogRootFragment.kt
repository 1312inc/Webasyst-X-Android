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
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.frag_blog_root.view.blogRoot

class BlogRootFragment : Fragment(), BackPressHandler {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragBlogRootBinding.inflate(
        inflater, container, false
    ).let { binding ->
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as MainActivity).addBackPressHandler(this)

        val navController = requireView().blogRoot.findNavController()
        navController.setGraph(R.navigation.nav_blog, arguments)

        val toolbar = (requireActivity() as MainActivity).toolbar
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (controller.previousBackStackEntry != null) {
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            } else {
                toolbar.setNavigationIcon(R.drawable.ic_hamburger)
            }
        }
    }

    override fun onBackPressed(): Boolean =
        try {
            val navController = requireView().blogRoot.findNavController()
            navController.popBackStack()
        } catch (e: IllegalStateException) {
            // This was called before onCreateView
            false
        }
}
