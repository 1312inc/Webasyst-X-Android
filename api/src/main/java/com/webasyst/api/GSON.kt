package com.webasyst.api

import com.google.gson.Gson
import com.webasyst.util.SingletonHolder

object GSON : SingletonHolder<Gson, Unit>(::createGson)

private fun createGson(u: Unit): Gson {
    return Gson()
}
