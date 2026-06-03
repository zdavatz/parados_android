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
        var updated = 0
        // Start from the known set, then let the freshly-downloaded index.html
        // ADD any newly-linked games so a brand-new game is fully OTA (Walter,
        // 2026-06-03): drop the file in the web repo + link it in index.html and
        // the next update pulls it — no app rebuild needed.
        val filenames = GameInfo.allFilenames.toMutableSet()

        val indexHtml = downloadAndSave("index.html")
        if (indexHtml != null) {
            updated++
            filenames.remove("index.html")
            filenames.addAll(linkedFiles(indexHtml))
        }

        for (filename in filenames) {
            if (downloadAndSave(filename) != null) updated++
        }

        if (updated > 0) {
            prefs.edit()
                .putLong("last_update", System.currentTimeMillis())
                .apply()
        }

        return updated
    }

    /** Download one file into gamesDir; returns its text body on success (so
     *  index.html can be parsed for links), null otherwise. */
    private fun downloadAndSave(filename: String): String? {
        return try {
            val connection = URL("$GITHUB_BASE$filename").openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.requestMethod = "GET"
            val body = if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().readText()
            } else null
            connection.disconnect()
            if (body != null) File(gamesDir, filename).writeText(body)
            body
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val GITHUB_BASE = "https://raw.githubusercontent.com/zdavatz/parados/main/"
        private val LINK_REGEX = Regex("href\\s*=\\s*\"([^\"]+\\.(?:html|csv))\"", RegexOption.IGNORE_CASE)

        /** Local game/tool files an index.html links to (no scheme, not absolute). */
        fun linkedFiles(html: String): List<String> =
            LINK_REGEX.findAll(html)
                .map { it.groupValues[1] }
                .filter { !it.contains("://") && !it.startsWith("/") && !it.startsWith("#") }
                .toList()
    }

    fun getLastUpdateTime(): Long {
        return prefs.getLong("last_update", 0)
    }
}
