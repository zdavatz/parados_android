package com.ywesee.parados

import android.app.Application
import android.webkit.WebView

/**
 * Application-level singleton that holds WebView instances so they survive
 * Activity destruction (e.g. when Android kills a backgrounded Activity
 * due to memory pressure or connectivity changes).
 */
class ParadosApp : Application() {

    /** One WebView per game, keyed by filename. Survives activity lifecycle. */
    val webViews = mutableMapOf<String, WebView>()
}
