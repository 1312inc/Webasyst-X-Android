package com.webasyst.x.blog.postlist

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.webasyst.api.ApiException
import com.webasyst.api.Installation
import com.webasyst.api.blog.BlogApiClient
import com.webasyst.api.blog.BlogApiClientFactory
import com.webasyst.api.blog.Post
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import kotlinx.coroutines.CancellationException

class PostListViewModel(
    application: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(application) {
    private val blogApiClient by lazy {
        (getApplication<WebasystXApplication>().apiClient.getFactory(BlogApiClient::class.java) as BlogApiClientFactory)
            .instanceForInstallation(Installation.invoke(
                id = installationId ?: "",
                urlBase = installationUrl ?: ""))
    }

    val appName = application.getString(R.string.app_blog)
    val apiName = "blog.post.search"

    private val mutablePostList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = mutablePostList

    private val mutableState = MutableLiveData<Int>().apply { value = STATE_UNKNOWN }
    val state: LiveData<Int> = mutableState

    private val mutableErrorText = MutableLiveData<String>()
    val errorText: LiveData<String> = mutableErrorText

    suspend fun updateData(context: Context) {
        if (mutableState.value == STATE_LOADING) {
            return
        }
        mutableState.postValue(STATE_LOADING)
        if (installationId == null || installationUrl == null) {
            return
        }
        blogApiClient.getPosts()
            .onSuccess { posts ->
                mutableErrorText.postValue("")
                mutablePostList.postValue(posts.posts)
                mutableState.postValue(if (posts.posts?.isEmpty() == true) {
                    STATE_LOADED_EMPTY
                } else {
                    STATE_LOADED
                })
            }
            .onFailure {
                if (it is ApiException && it.cause !is CancellationException) {
                    AlertDialog
                        .Builder(context)
                        .setMessage(context.getString(R.string.waid_error, it.localizedMessage))
                        .setPositiveButton(R.string.btn_ok) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                mutableState.postValue(STATE_ERROR)
                mutableErrorText.postValue(it.localizedMessage)
            }
    }

    class Factory(
        private val application: Application,
        private val installationId: String?,
        private val installationUrl: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            PostListViewModel(application, installationId, installationUrl) as T
    }

    companion object {
        const val STATE_UNKNOWN = 0
        const val STATE_LOADING = 1
        const val STATE_LOADED = 2
        const val STATE_LOADED_EMPTY = 3
        const val STATE_ERROR = 4
    }
}
