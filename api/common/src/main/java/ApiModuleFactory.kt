package com.webasyst.api

import com.google.gson.GsonBuilder

abstract class ApiModuleFactory<out T : ApiModule> {
    abstract val scope: String
    abstract fun instanceForInstallation(installation: Installation): T
    open val gsonConfigurator: ((GsonBuilder) -> Unit)? = null
}
