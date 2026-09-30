package com.rising.pos.core.update.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class GithubRelease(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("html_url") val htmlUrl: String? = null,
    val assets: List<GithubAsset> = emptyList()
)

@Serializable
data class GithubAsset(
    val name: String,
    val size: Long = 0,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val releaseName: String,
        val releaseNotes: String,
        val publishedAt: String?,
        val downloadUrl: String,
        val apkSize: Long
    ) : UpdateState
    data class UpToDate(val currentVersion: String) : UpdateState
    data class Downloading(
        val progress: Float, // 0.0f .. 1.0f
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : UpdateState
    data class Downloaded(
        val apkFile: File,
        val latestVersion: String
    ) : UpdateState
    data class Error(val message: String) : UpdateState
}
