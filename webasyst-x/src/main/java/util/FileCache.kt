package com.webasyst.x.util

import android.content.Context
import java.io.File

fun Context.getCacheFile(filename: String): File = File(cacheDir, filename)

const val USERPIC_FILE = "userpic"
