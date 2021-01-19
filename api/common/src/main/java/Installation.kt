package com.webasyst.api

interface Installation {
    val id: String
    val urlBase: String

    companion object {
        operator fun invoke(
            id: String,
            urlBase: String,
        ): Installation = InstallationImpl(
            id = id,
            urlBase = urlBase,
        )
    }
}

private class InstallationImpl(
    override val id: String,
    override val urlBase: String,
) : Installation
