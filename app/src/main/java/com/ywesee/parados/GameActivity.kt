package com.ywesee.parados

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AlphaAnimation
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import kotlin.math.abs

class GameActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var fabBack: FloatingActionButton
    private val handler = Handler(Looper.getMainLooper())
    private val hideDelay = 3000L
    private var currentFilename: String? = null

    // CSV import: file chooser callback
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>

    private val hideFab = Runnable {
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 400; fillAfter = true }
        fabBack.startAnimation(fadeOut)
        fabBack.postDelayed({ fabBack.visibility = View.GONE }, 400)
    }

    private fun showFabTemporarily() {
        fabBack.clearAnimation()
        fabBack.visibility = View.VISIBLE
        fabBack.alpha = 1f
        handler.removeCallbacks(hideFab)
        handler.postDelayed(hideFab, hideDelay)
    }

    private fun goBackToMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        hideSystemBars()

        // Register file chooser launcher for CSV import
        fileChooserLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { arrayOf(it) }
            } else null
            fileUploadCallback?.onReceiveValue(uri)
            fileUploadCallback = null
        }

        fabBack = findViewById(R.id.fab_back)
        fabBack.setOnClickListener { goBackToMenu() }

        webView = findViewById(R.id.game_webview)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        // Add JavaScript interface for CSV export
        webView.addJavascriptInterface(CsvExportBridge(), "AndroidCsvExport")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Inject JS to intercept blob download links (CSV export)
                view?.evaluateJavascript(INJECTED_JS, null)
            }
        }

        // WebChromeClient with file chooser support for CSV import
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                fileChooserLauncher.launch(intent)
                return true
            }
        }

        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (e1.x < 80 && dx > 120 && abs(dy) < abs(dx) && velocityX > 300) {
                    goBackToMenu()
                    return true
                }
                return false
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (e.x < 150 && e.y < 150) {
                    showFabTemporarily()
                }
                return false
            }
        })

        webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        loadGame(intent)
        handler.postDelayed(hideFab, hideDelay)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newFilename = intent.getStringExtra("filename") ?: "index.html"
        // Only reload if it's a different game
        if (newFilename != currentFilename) {
            loadGame(intent)
        }
        // Otherwise: same game, keep WebView state as-is
        hideSystemBars()
        showFabTemporarily()
    }

    private fun loadGame(intent: Intent) {
        val filename = intent.getStringExtra("filename") ?: "index.html"
        currentFilename = filename
        val repository = GameRepository(this)
        webView.loadUrl(repository.getGameUri(filename))
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        }
    }

    @Deprecated("Use onBackPressedDispatcher")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            goBackToMenu()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onDestroy() {
        handler.removeCallbacks(hideFab)
        super.onDestroy()
    }

    /** JavaScript interface to receive CSV data from the WebView */
    inner class CsvExportBridge {
        @JavascriptInterface
        fun exportCsv(filename: String, content: String) {
            runOnUiThread {
                shareCsvFile(filename, content)
            }
        }
    }

    private fun shareCsvFile(filename: String, content: String) {
        val csvDir = File(cacheDir, "csv_exports")
        csvDir.mkdirs()
        val csvFile = File(csvDir, filename)
        csvFile.writeText(content)

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            csvFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, filename))
    }

    companion object {
        /**
         * Injected JavaScript that intercepts <a download> blob clicks
         * and routes them through the Android bridge for sharing.
         */
        private const val INJECTED_JS = """
            (function() {
                if (window.__androidCsvBridgeInstalled) return;
                window.__androidCsvBridgeInstalled = true;
                var _origClick = HTMLAnchorElement.prototype.click;
                HTMLAnchorElement.prototype.click = function() {
                    if (this.hasAttribute('download') && this.href && this.href.startsWith('blob:')) {
                        var filename = this.getAttribute('download') || 'export.csv';
                        fetch(this.href).then(function(r) { return r.text(); }).then(function(text) {
                            AndroidCsvExport.exportCsv(filename, text);
                        });
                        return;
                    }
                    _origClick.call(this);
                };
            })();
        """
    }
}
