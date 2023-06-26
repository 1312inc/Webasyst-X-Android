package com.webasyst.x.common.binding

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentViewBindingProperty<T : ViewBinding>(
    fragment: Fragment,
    viewBindingFactory: (View) -> T,
) : ReadOnlyProperty<Fragment, T> {
    private var viewBindingFactory: ((View) -> T)? = viewBindingFactory
    private var binding: T? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.addObserver(DestructionObserver())
        }

        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                fragment.view ?: error("You must either pass in the layout ID into ${fragment.javaClass.simpleName}'s constructor or inflate a view in onCreateView()")
            }
        })
    }

    @MainThread
    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        //checkIsMainThread()
        binding?.let { return it }

        val viewLifecycleOwner = try {
            thisRef.viewLifecycleOwner
        } catch (e: IllegalStateException) {
            error("Should not attempt to get bindings when Fragment views haven't been created yet. The fragment has not called onCreateView() at this point.")
        }
        if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            error("Should not attempt to get bindings when Fragment views are destroyed. The fragment has already called onDestroyView() at this point.")
        }

        return viewBindingFactory!!(thisRef.requireView()).also { viewBinding ->
            if (viewBinding is ViewDataBinding) {
                viewBinding.lifecycleOwner = viewLifecycleOwner
            }

            this.binding = viewBinding
        }
    }

    private inner class DestructionObserver : DefaultLifecycleObserver {
        private val mainHandler = Handler(Looper.getMainLooper())

        @MainThread
        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            mainHandler.post {
                (binding as? ViewDataBinding)?.unbind()
                binding = null
            }
        }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(
    viewBindingFactory: (View) -> T
) = FragmentViewBindingProperty(this, viewBindingFactory)
