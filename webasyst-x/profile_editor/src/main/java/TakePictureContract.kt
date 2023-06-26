package com.webasyst.x.profile_editor

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class TakePictureContract : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return super
            .createIntent(context, input)
            .also { intent ->
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
    }
}
