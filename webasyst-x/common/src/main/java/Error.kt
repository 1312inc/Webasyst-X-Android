package com.webasyst.x.common

import android.content.Context
import com.webasyst.api.WebasystException

fun Context.errorTexts(e: Throwable): Pair<String, String> =
    when (e) {
        is WebasystException ->
            when (e.webasystCode) {
                "unsupported_scope" ->
                    Pair(
                        getString(R.string.error_unsupported_scope),
                        getString(R.string.error_unsupported_scope_details))
                "required_primary_email" ->
                    Pair(
                        getString(R.string.profile_editor_save_email_error),
                        e.webasystMessage)
                "required_name" ->
                    Pair(
                        getString(R.string.profile_editor_save_name_error),
                        e.webasystMessage)
                WebasystException.ERROR_ACCOUNT_SUSPENDED ->
                    Pair(
                        getString(R.string.error_account_suspended),
                        getString(R.string.error_account_suspended_details))
                else ->
                    Pair(
                        getString(R.string.error_generic),
                        e.webasystMessage)
            }
        else ->
            Pair(
                getString(R.string.error_generic),
                e.message ?: "")
    }
