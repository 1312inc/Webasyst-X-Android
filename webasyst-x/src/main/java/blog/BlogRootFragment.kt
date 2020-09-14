package com.webasyst.x.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.webasyst.x.R
import com.webasyst.x.databinding.FragBlogRootBinding
import kotlinx.android.synthetic.main.frag_blog_root.view.blogRoot

class BlogRootFragment : Fragment() {
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

        val navController = requireView().blogRoot.findNavController()
        navController.setGraph(R.navigation.nav_blog, arguments)
    }
}
