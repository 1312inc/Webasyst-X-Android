package com.webasyst.x.common

interface InstallationListStore {
    suspend fun getSelectedInstallationId(): String
    suspend fun setSelectedInstallationId(id: String)
    suspend fun clearInstallations()
    suspend fun getInstallations(): List<InstallationInterface>
    suspend fun setInstallationList(installations: List<InstallationInterface>)
}
