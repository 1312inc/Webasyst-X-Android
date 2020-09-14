package com.webasyst.x.blog.postlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.webasyst.api.blog.BlogApiClient
import com.webasyst.api.blog.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateData()
        }
    }

    private suspend fun updateData() {
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
