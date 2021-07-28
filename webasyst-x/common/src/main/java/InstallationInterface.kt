package com.webasyst.x.common

import com.webasyst.api.webasyst.InstallationInfo
import java.util.Calendar

interface InstallationInterface {
    val id: String
    val name: String
    val domain: String
    val url: String
    val cloudExpireDate: Calendar?
    val logo: InstallationInfo.Logo?
}
