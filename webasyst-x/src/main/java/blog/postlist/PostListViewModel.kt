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
import com.webasyst.api.blog.BlogApiClient
import com.webasyst.api.blog.Post
import com.webasyst.x.R

class PostListViewModel(
    application: Application,
    private val installationId: String?,
    private val installationUrl: String?
) : AndroidViewModel(application) {
    private val blogApiClient by lazy {
        BlogApiClient.getInstance(getApplication())
    }

    private val mutablePostList = MutableLiveData<List<Post>>()
    val postList: LiveData<List<Post>> = mutablePostList

    private val mutableState = MutableLiveData<Int>().apply { value = STATE_LOADING }
    val state: LiveData<Int> = mutableState

    private val mutableErrorText = MutableLiveData<String>()
    val errorText: LiveData<String> = mutableErrorText

    suspend fun updateData(context: Context) {
        if (installationId == null || installationUrl == null) {
            mutableState.postValue(STATE_ERROR)
            return
        }
        blogApiClient.getPosts(installationUrl, installationId)
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
                if (it is ApiException) {
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
        const val STATE_LOADING = 0
        const val STATE_LOADED = 1
        const val STATE_LOADED_EMPTY = 2
        const val STATE_ERROR = 3
    }

}