package com.rising.pos.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.rising.pos.BuildConfig
import com.rising.pos.core.update.model.GithubRelease
import com.rising.pos.core.update.model.UpdateState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var activeApkFile: File? = null

    suspend fun checkForUpdates(
        owner: String = DEFAULT_GITHUB_OWNER,
        repo: String = DEFAULT_GITHUB_REPO
    ): UpdateState = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Checking
        try {
            val url = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("User-Agent", "RisingPOS-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMsg = when (response.code) {
                        404 -> "Belum ada rilis publik di repository GitHub ($owner/$repo)."
                        403 -> "Batas kuota akses GitHub API tercapai. Coba lagi beberapa saat lagi."
                        else -> "Gagal memeriksa update (HTTP ${response.code})."
                    }
                    val state = UpdateState.Error(errorMsg)
                    _updateState.value = state
                    return@withContext state
                }

                val responseBody = response.body?.string()
                    ?: throw IOException("Empty response from GitHub API")

                val release = json.decodeFromString<GithubRelease>(responseBody)

                // Search for .apk asset in release
                val apkAsset = release.assets.firstOrNull {
                    it.name.endsWith(".apk", ignoreCase = true)
                }

                val currentVersion = BuildConfig.VERSION_NAME
                val latestTag = release.tagName.trim().removePrefix("v").removePrefix("V")

                val hasNewVersion = isNewerVersion(latestTag, currentVersion)

                if (hasNewVersion && apkAsset != null) {
                    val state = UpdateState.UpdateAvailable(
                        currentVersion = currentVersion,
                        latestVersion = release.tagName,
                        releaseName = release.name ?: release.tagName,
                        releaseNotes = release.body ?: "Pembaruan versi ${release.tagName}",
                        publishedAt = release.publishedAt,
                        downloadUrl = apkAsset.browserDownloadUrl,
                        apkSize = apkAsset.size
                    )
                    _updateState.value = state
                    state
                } else if (hasNewVersion && apkAsset == null) {
                    val state = UpdateState.Error(
                        "Versi baru ${release.tagName} tersedia, namun file APK belum diunggah ke GitHub Releases."
                    )
                    _updateState.value = state
                    state
                } else {
                    val state = UpdateState.UpToDate(currentVersion = currentVersion)
                    _updateState.value = state
                    state
                }
            }
        } catch (e: Exception) {
            val state = UpdateState.Error("Koneksi gagal: ${e.localizedMessage ?: "Periksa jaringan internet"}")
            _updateState.value = state
            state
        }
    }

    suspend fun downloadApk(
        downloadUrl: String,
        versionTag: String
    ): Result<File> = withContext(Dispatchers.IO) {
        val sanitizedTag = versionTag.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val fileName = "rising-pos-$sanitizedTag.apk"
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }
        val destinationFile = File(updatesDir, fileName)

        _updateState.value = UpdateState.Downloading(
            progress = 0f,
            bytesDownloaded = 0L,
            totalBytes = 0L
        )

        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .addHeader("User-Agent", "RisingPOS-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = "Gagal mengunduh APK (HTTP ${response.code})"
                    _updateState.value = UpdateState.Error(err)
                    return@withContext Result.failure(IOException(err))
                }

                val body = response.body ?: throw IOException("Empty download body")
                val totalBytes = body.contentLength()

                body.byteStream().use { input ->
                    destinationFile.outputStream().use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var bytesRead: Int
                        var downloaded = 0L
                        var lastProgressUpdate = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloaded += bytesRead

                            val now = System.currentTimeMillis()
                            if (now - lastProgressUpdate > 100 || downloaded == totalBytes) {
                                lastProgressUpdate = now
                                val progress = if (totalBytes > 0) downloaded.toFloat() / totalBytes.toFloat() else 0f
                                _updateState.value = UpdateState.Downloading(
                                    progress = progress.coerceIn(0f, 1f),
                                    bytesDownloaded = downloaded,
                                    totalBytes = totalBytes
                                )
                            }
                        }
                    }
                }
            }

            activeApkFile = destinationFile
            _updateState.value = UpdateState.Downloaded(
                apkFile = destinationFile,
                latestVersion = versionTag
            )
            Result.success(destinationFile)
        } catch (e: Exception) {
            destinationFile.delete()
            val state = UpdateState.Error("Gagal mendownload APK: ${e.localizedMessage}")
            _updateState.value = state
            Result.failure(e)
        }
    }

    fun canInstallPackages(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun getUnknownSourcesSettingsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !canInstallPackages()) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            null
        }
    }

    fun installApk(file: File? = null): Result<Unit> {
        val targetFile = file ?: activeApkFile ?: return Result.failure(IllegalStateException("File APK belum didownload"))
        return try {
            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, targetFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Gagal membuka installer: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    /**
     * Semver comparison: returns true if latest is strictly newer than current.
     */
    fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.trim().removePrefix("v").removePrefix("V").split("-")[0].split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.trim().removePrefix("v").removePrefix("V").split("-")[0].split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    companion object {
        const val DEFAULT_GITHUB_OWNER = "ferdiodwi"
        const val DEFAULT_GITHUB_REPO = "rising-POS"
    }
}
