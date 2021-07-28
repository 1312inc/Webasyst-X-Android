package com.webasyst.x.common

open class SingletonHolder<in A, out T: Any>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun instance(arg: A): T {
        val checkInstance = instance
        if (null != checkInstance) {
            return checkInstance
        }

        return synchronized(this) {
            val checkInstanceAgain = instance
            return if (null != checkInstanceAgain) {
                checkInstanceAgain
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
