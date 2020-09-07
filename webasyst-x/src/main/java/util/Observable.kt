package com.webasyst.x.util

import java.lang.ref.WeakReference

class Observable <T> {
    private val observers = mutableListOf<WeakReference<T>>()

    fun addObserver(observer: T) {
        observers.add(WeakReference(observer))
    }

    fun notifyObservers(block: T.() -> Unit) {
        val iterator = observers.listIterator()
        while (iterator.hasNext()) {
            val reference = iterator.next()
            val observer = reference.get()
            if (null == observer) {
                iterator.remove()
                continue
            }
            observer.apply(block)
        }
    }
}
