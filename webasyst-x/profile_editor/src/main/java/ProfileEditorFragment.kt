package com.webasyst.x.profile_editor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.webasyst.waid.UpdateUserInfo
import com.webasyst.x.common.binding.viewBinding
import com.webasyst.x.common.errorTexts
import com.webasyst.x.common.findRootNavController
import com.webasyst.x.profile_editor.databinding.FragProfileEditorBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfileEditorFragment : Fragment(R.layout.frag_profile_editor), ProfileEditorViewModel.ProfileEditor {
    private val viewModel: ProfileEditorViewModel by viewModels()
    private val binding by viewBinding(FragProfileEditorBinding::bind)
    private val imageLoader: ImageLoader by inject()

    private val isEmptyUser: Boolean
        get() = arguments?.getBoolean(IS_EMPTY_USER) ?: false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layout) { v, windowInsets ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                val insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars())
                bottomMargin = insets.bottom
                topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }

        viewModel.isEmptyUser = isEmptyUser
        binding.viewModel = viewModel

        sharedElementEnterTransition = MaterialContainerTransform()
        postponeEnterTransition(300, TimeUnit.MILLISECONDS)

        viewLifecycleOwner.lifecycleScope.launch {
            var transitionStarted = false
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var firstLoad = true
                viewModel.userPic.collect {
                    val request = ImageRequest
                        .Builder(requireContext())
                        .let { builder ->
                            if (it.isValidUrl()) {
                                builder.data(it)
                            } else {
                                builder.data(R.drawable.ic_default_userpic)
                            }

                            if (firstLoad) {
                                firstLoad = false
                                builder
                            } else {
                                builder.crossfade(true)
                            }
                        }
                        .target(binding.userpicView)
                        .listener { _, _ ->
                            if (!transitionStarted) {
                                startPostponedEnterTransition()
                            }
                            transitionStarted = true
                        }
                        .build()
                    imageLoader.execute(request)
                    binding.userpicView.setOnClickListener { _ ->
                        findNavController().navigate(
                            R.id.action_profileEditorFragment_to_photoDetailFragment,
                            bundleOf(PhotoDetailBundle to it)
                        )
                    }
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            view.findRootNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.profileEditor = this
    }

    override fun onPause() {
        super.onPause()
        viewModel.profileEditor = null
    }

    val getGalleryImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            viewModel.onUserpicSelected(requireContext(), it) {}
        }
    }

    var takePictureUri: Uri? = null
    val takePicture = registerForActivityResult(TakePictureContract()) { success ->
        val uri = takePictureUri
        if (success && uri != null) {
            viewModel.onUserpicSelected(requireContext(), uri) {
                try {
                    uri
                        .buildUpon()
                        .scheme("file")
                        .build()
                        .toFile()
                        .delete()
                } catch (e: Throwable) {
                    Log.w(TAG, "Failed to delete temporary file ($uri)", e)
                }
            }
        }
    }
    fun takePictureFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return imageFile
    }

    override fun onSetUserpic(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.userpic_source, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.from_gallery -> {
                    getGalleryImage.launch("image/*")
                    true
                }
                R.id.take_photo -> {
                    lifecycleScope.launchWhenStarted {
                        if (activity?.requestPermissionCameraForResult() == true) {
                            val file = takePictureFile()
                            takePictureUri = FileProvider.getUriForFile(
                                requireContext(),
                                "com.webasyst.shopscript.provider",
                                file
                            )
                            takePicture.launch(takePictureUri)
                        }
                    }
                    true
                }
                else ->
                    false
            }
        }
        popup.show()
    }

    override fun onUpdateUserInfo() {
        val firstName = binding.firstname.text?.toString()
        val lastName = binding.lastname.text?.toString()
        viewModel.onSave(UpdateUserInfo(
            firstName = firstName,
            lastName = lastName,
        ))
    }

    override fun handleException(e: Throwable) {
        val (title, _) = requireContext().errorTexts(e)
        Snackbar.make(requireView(),
            title,
            Snackbar.LENGTH_LONG)
            .setAnchorView(binding.anchor).show()
    }

    override fun toast(@StringRes resId: Int) {
        Snackbar.make(requireView(), resId, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.anchor).show()
    }

    override fun popBack() {
        if (isEmptyUser) {
            view?.findRootNavController()?.popBackStack()
        }
    }

    private var mCont: Continuation<Boolean>? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        mCont?.resume(isGranted)
    }
    private suspend fun Context.requestPermissionCameraForResult(): Boolean =
        suspendCoroutine { cont ->
            mCont = cont
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                cont.resume(true)
            }
        }

    private fun String.isValidUrl() = Patterns.WEB_URL.matcher(this).matches()

    companion object {
        const val TAG = "profile_editor"
        const val IS_EMPTY_USER = "is_empty_user"
    }
}
