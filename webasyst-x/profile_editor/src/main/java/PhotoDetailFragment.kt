package com.webasyst.x.profile_editor

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.webasyst.x.common.binding.viewBinding
import com.webasyst.x.common.findRootNavController
import com.webasyst.x.profile_editor.databinding.FragPhotoDetailBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class PhotoDetailFragment : Fragment(R.layout.frag_photo_detail) {
    private val binding by viewBinding(FragPhotoDetailBinding::bind)
    var url = ""
    private val imageLoader: ImageLoader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = requireArguments().getString(PhotoDetailBundle,"")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.layout) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                bottomMargin = insets.bottom
                topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }

        val request = ImageRequest.Builder(requireContext())
            .data(url)
            .target { drawable ->
                binding.photoView.setImageDrawable(drawable)
            }
            .build()
        lifecycleScope.launch { imageLoader.execute(request) }
    }

    fun onCloseClick(view: View){
        view.findRootNavController().popBackStack()
    }
}

const val PhotoDetailBundle = "photoUrlBundle"
