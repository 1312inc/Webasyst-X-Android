package com.webasyst.x.blog.postlist

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webasyst.api.Installation
import com.webasyst.api.blog.BlogApiClient
import com.webasyst.api.blog.BlogApiClientFactory
import com.webasyst.api.blog.Post
import com.webasyst.x.R
import com.webasyst.x.WebasystXApplication
import com.webasyst.x.util.ConnectivityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

    private val _error = MutableLiveData<Throwable?>(null)
    val error: LiveData<Throwable?> get() = _error

    init {
        val connectivityUtil = ConnectivityUtil(application)
        viewModelScope.launch(Dispatchers.Default) {
            connectivityUtil.connectivityFlow()
                .collect {
                    if (it == ConnectivityUtil.ONLINE) {
                        updateData(application)
                    }
                }
        }
    }

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
                _error.postValue(null)
                mutablePostList.postValue(posts.posts ?: emptyList())
                mutableState.postValue(if (posts.posts?.isEmpty() == true) {
                    STATE_LOADED_EMPTY
                } else {
                    STATE_LOADED
                })
            }
            .onFailure {
                mutableState.postValue(STATE_ERROR)
                _error.postValue(it)
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
