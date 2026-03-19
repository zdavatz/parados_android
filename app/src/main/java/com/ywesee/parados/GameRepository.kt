package com.ywesee.parados

import android.content.Context
import android.content.pm.PackageManager
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class GameRepository(private val context: Context) {

    private val gamesDir = File(context.filesDir, "games")
    private val prefs = context.getSharedPreferences("parados_prefs", Context.MODE_PRIVATE)

    fun ensureGamesInstalled() {
        val currentVersion = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (_: PackageManager.NameNotFoundException) { 1 }

        val installedVersion = prefs.getInt("assets_version", 0)
        if (installedVersion >= currentVersion && gamesDir.exists()) return
        copyAssetsToInternal()
        prefs.edit().putInt("assets_version", currentVersion).apply()
    }

    private fun copyAssetsToInternal() {
        gamesDir.mkdirs()
        val assetFiles = context.assets.list("games") ?: return
        for (filename in assetFiles) {
            val outFile = File(gamesDir, filename)
            context.assets.open("games/$filename").use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun getGameFile(filename: String): File {
        return File(gamesDir, filename)
    }

    fun getGameUri(filename: String): String {
        return "file://${getGameFile(filename).absolutePath}"
    }

    /**
     * Download all game HTML files from GitHub.
     * Returns the number of files successfully updated.
     */
    fun updateFromGithub(): Int {
        val baseUrl = "https://raw.githubusercontent.com/zdavatz/parados/main/"
        var updated = 0

        for (filename in GameInfo.allFilenames) {
            try {
                val url = URL("$baseUrl$filename")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val content = connection.inputStream.bufferedReader().readText()
                    val file = File(gamesDir, filename)
                    file.writeText(content)
                    updated++
                }
                connection.disconnect()
            } catch (_: Exception) {
                // Skip files that fail to download
            }
        }

        if (updated > 0) {
            prefs.edit()
                .putLong("last_update", System.currentTimeMillis())
                .apply()
        }

        return updated
    }

    fun getLastUpdateTime(): Long {
        return prefs.getLong("last_update", 0)
    }
}
