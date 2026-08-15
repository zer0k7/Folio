package com.ghost.folio.util.updater

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.FileProvider
import com.ghost.folio.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val isAvailable: Boolean,
    val currentVersion: String,
    val latestVersion: String,
    val releaseNotes: String,
    val apkDownloadUrl: String
)

class GitHubAppUpdater(
    private val context: Context,
    private val githubRepo: String = "zer0k7/Folio"
) {
    companion object {
        private const val PREFS_NAME = "folio_updater_prefs"
        private const val KEY_LAST_CHECK_TIMESTAMP = "last_check_timestamp"
        private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun shouldCheckForUpdate(): Boolean {
        val lastCheck = prefs.getLong(KEY_LAST_CHECK_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()
        return (now - lastCheck) >= CHECK_INTERVAL_MS
    }

    fun markUpdateChecked() {
        prefs.edit().putLong(KEY_LAST_CHECK_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    /**
     * Checks GitHub API for the latest release and compares semantic versions.
     */
    suspend fun checkForUpdate(force: Boolean = false): AppUpdateInfo? = withContext(Dispatchers.IO) {
        if (!force && !shouldCheckForUpdate()) {
            return@withContext null
        }

        try {
            val url = URL("https://api.github.com/repos/$githubRepo/releases/latest")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Folio-Android-Updater")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (connection.responseCode != 200) {
                return@withContext null
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)

            val latestTag = json.getString("tag_name").removePrefix("v")
            val releaseNotes = json.optString("body", "")
            val currentVersion = getAppVersionName()

            var apkUrl: String? = null
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }
            }

            if (apkUrl == null) {
                apkUrl = json.optString("html_url", "https://github.com/$githubRepo/releases/latest")
            }

            val isNewer = compareVersions(latestTag, currentVersion) > 0

            markUpdateChecked()

            return@withContext AppUpdateInfo(
                isAvailable = isNewer,
                currentVersion = currentVersion,
                latestVersion = latestTag,
                releaseNotes = cleanReleaseNotes(releaseNotes),
                apkDownloadUrl = apkUrl
            )
        } catch (_: Exception) {
            return@withContext null
        }
    }

    /**
     * Downloads the APK directly to cache directory with live progress callbacks.
     */
    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (percentage: Int, bytesDownloaded: Long, totalBytes: Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val destinationFile = File(cacheDir, "folio_update.apk")

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Folio-Android-Updater")
                connectTimeout = 15000
                readTimeout = 30000
                connect()
            }

            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes > 0) {
                            val percent = ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                            withContext(Dispatchers.Main) {
                                onProgress(percent, downloadedBytes, totalBytes)
                            }
                        }
                    }
                }
            }
            return@withContext destinationFile
        } catch (_: Exception) {
            return@withContext null
        }
    }

    /**
     * Triggers the Android Native Package Installer via FileProvider URI.
     */
    fun installApk(apkFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/$githubRepo/releases/latest")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    private fun getAppVersionName(): String {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: BuildConfig.VERSION_NAME
        } catch (_: Exception) {
            BuildConfig.VERSION_NAME
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".", "-").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".", "-").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLen) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 != num2) return num1.compareTo(num2)
        }
        return 0
    }

    private fun cleanReleaseNotes(raw: String): String {
        if (raw.isBlank()) return "General bug fixes and performance improvements."
        return raw.replace(Regex("(?m)^###+ "), "")
            .replace(Regex("(?m)^## "), "")
            .replace(Regex("`"), "")
            .trim()
    }
}
