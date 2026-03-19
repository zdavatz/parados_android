package com.ywesee.parados

import android.annotation.SuppressLint
import android.content.Intent
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
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs

class GameActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var fabBack: FloatingActionButton
    private val handler = Handler(Looper.getMainLooper())
    private val hideDelay = 3000L

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

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        // Swipe gesture detection on the WebView
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                // Left-to-right swipe from the left edge (start within 80px)
                if (e1.x < 80 && dx > 120 && abs(dy) < abs(dx) && velocityX > 300) {
                    goBackToMenu()
                    return true
                }
                return false
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                showFabTemporarily()
                return false
            }
        })

        webView.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            false // Let WebView handle the touch too
        }

        val filename = intent.getStringExtra("filename") ?: "index.html"
        val repository = GameRepository(this)
        webView.loadUrl(repository.getGameUri(filename))

        // Auto-hide the back button after initial delay
        handler.postDelayed(hideFab, hideDelay)
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
}
