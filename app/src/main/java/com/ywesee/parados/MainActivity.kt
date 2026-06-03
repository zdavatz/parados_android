package com.ywesee.parados

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var repository: GameRepository
    private lateinit var webView: WebView
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "PARADOS"

        repository = GameRepository(this)
        repository.ensureGamesInstalled()

        // The landing page is the SHARED index.html (same file the website /
        // iOS / desktop ship), so the website's branding/content edits reach
        // Android through the update button (Walter, 2026-06-03: "the
        // index.html has to be the same everywhere").
        webView = findViewById(R.id.menu_webview)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val uri = request?.url ?: return false
                return handleMenuLink(uri)
            }
        }
        loadMenu()
    }

    private fun loadMenu() {
        webView.loadUrl(repository.getGameUri("index.html"))
    }

    /**
     * Intercept taps on the index.html game cards.
     * Returns true if we handled the navigation (so the menu WebView stays put).
     */
    private fun handleMenuLink(uri: Uri): Boolean {
        val scheme = uri.scheme ?: return false

        // External http(s) links (e.g. the GitHub footer link) → browser.
        if (scheme == "http" || scheme == "https") {
            openExternal(uri)
            return true
        }
        if (scheme != "file") return false

        val filename = uri.lastPathSegment ?: ""

        // Remote-multiplayer variants need a real https origin for
        // PeerJS/WebRTC — open the public site instead of the local file.
        if (filename.endsWith("_remote.html")) {
            openExternal(Uri.parse("https://game.ywesee.com/parados/$filename"))
            return true
        }

        // The index.html itself (shouldn't reach here for the initial load).
        if (filename.isEmpty() || filename == "index.html") return false

        // Any other local page (a game or the Startpositionen tool) opens in
        // the existing full-screen GameActivity.
        launchGame(filename)
        return true
    }

    private fun launchGame(filename: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("filename", filename)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    private fun openExternal(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            // No handler available — silently ignore.
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {
                updateGames()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateGames() {
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        scope.launch {
            val updated = withContext(Dispatchers.IO) {
                repository.updateFromGithub()
            }
            progressBar.visibility = View.GONE
            Toast.makeText(
                this@MainActivity,
                if (updated > 0) "$updated Dateien aktualisiert" else "Keine Updates verfügbar",
                Toast.LENGTH_SHORT
            ).show()
            // Reload so the refreshed index.html (and any branding change)
            // shows immediately.
            if (updated > 0) loadMenu()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
