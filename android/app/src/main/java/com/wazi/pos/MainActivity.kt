package com.wazi.pos

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {

    companion object {
        private const val APP_URL = "https://wazi-pos.vercel.app"
        private const val BRIDGE_NAME = "Sunmi"
    }

    private lateinit var webView: WebView
    private lateinit var printer: SunmiPrinterManager
    private lateinit var bridge: SunmiBridge

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        webView = WebView(this)
        setContentView(webView)

        printer = SunmiPrinterManager(this)
        bridge = SunmiBridge(printer)
        printer.connect()

        configureWebView()
        webView.addJavascriptInterface(bridge, BRIDGE_NAME)
        webView.loadUrl(APP_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.setSupportZoom(false)
        webView.settings.builtInZoomControls = false
        webView.settings.displayZoomControls = false

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_OFF)
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val host = request.url.host ?: return true
                return if (
                    host == "wazi-pos.vercel.app" ||
                    host.endsWith(".vercel.app")
                ) {
                    false
                } else {
                    Toast.makeText(this@MainActivity, "Blocked external navigation", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::printer.isInitialized && !printer.connected) {
            printer.connect()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.removeJavascriptInterface(BRIDGE_NAME)
        if (::printer.isInitialized) printer.disconnect()
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
