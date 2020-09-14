package com.webasyst.x.blog.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.webasyst.x.databinding.FragBlogPostViewBinding

class PostViewFragment : Fragment() {
    val post: Post? by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getParcelable(POST)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragBlogPostViewBinding.inflate(
        inflater
    ).let { binding ->
        binding.post = post
        binding.lifecycleOwner = viewLifecycleOwner
        binding.root
    }

    companion object {
        const val POST = "post"
    }
}
