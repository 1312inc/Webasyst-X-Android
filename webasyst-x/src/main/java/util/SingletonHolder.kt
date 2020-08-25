package com.webasyst.x.util

abstract class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
    private var creator : ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val checkInstance = instance
        if (null != checkInstance) {
            return checkInstance
        }

        return synchronized(this) {
            val checkInstanceAgain = instance
            if (null != checkInstanceAgain) {
                checkInstanceAgain
            } else {
                val created = creator!!(arg)
                instance = created
                created
            }
        }
    }
}
