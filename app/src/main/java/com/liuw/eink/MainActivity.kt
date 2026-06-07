package com.liuw.eink

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "eink_prefs"
        private const val KEY_URL = "web_url"
        private const val DEFAULT_URL = "file:///android_asset/index.html"
        private const val TAP_TIMEOUT_MS = 3000L
        private const val TAP_COUNT_TO_OPEN = 5
    }

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences

    private var tapCount = 0
    private var lastTapTime = 0L
    private val tapHandler = Handler(Looper.getMainLooper())
    private val tapResetRunnable = Runnable { tapCount = 0 }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupFullScreen()
        setupKeepScreenOn()
        setupWebView()
        setupHiddenButton()

        val url = prefs.getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL
        webView.loadUrl(url)
    }

    private fun setupFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
        }
    }

    private fun setupKeepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = findViewById(R.id.webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(AndroidBridge(this), "Android")
    }

    private fun setupHiddenButton() {
        val hiddenBtn = findViewById<View>(R.id.hiddenSettingsBtn)
        hiddenBtn.setOnClickListener {
            val now = System.currentTimeMillis()

            if (now - lastTapTime > TAP_TIMEOUT_MS) {
                tapCount = 0
            }
            lastTapTime = now
            tapCount++

            tapHandler.removeCallbacks(tapResetRunnable)
            tapHandler.postDelayed(tapResetRunnable, TAP_TIMEOUT_MS)

            if (tapCount >= TAP_COUNT_TO_OPEN) {
                tapCount = 0
                tapHandler.removeCallbacks(tapResetRunnable)
                showSettingsDialog()
            }
        }
    }

    private fun showSettingsDialog() {
        val currentUrl = prefs.getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL
        val input = EditText(this).apply {
            setText(currentUrl)
            setSelectAllOnFocus(true)
            hint = "输入网址 (http:// 或 https://)"
        }

        AlertDialog.Builder(this)
            .setTitle("设置")
            .setView(input)
            .setPositiveButton("保存并加载") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotEmpty()) {
                    prefs.edit().putString(KEY_URL, url).apply()
                    webView.loadUrl(url)
                }
            }
            .setNeutralButton("重新加载") { _, _ ->
                webView.reload()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private class AndroidBridge(private val activity: MainActivity) {

        @JavascriptInterface
        fun getBatteryLevel(): Int {
            val bm = activity.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }

        @JavascriptInterface
        fun setKeepScreenOn(enabled: Boolean) {
            activity.runOnUiThread {
                if (enabled) {
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        @JavascriptInterface
        fun setScreenBrightness(value: Float) {
            activity.runOnUiThread {
                val lp = activity.window.attributes
                lp.screenBrightness = value.coerceIn(0.01f, 1.0f)
                activity.window.attributes = lp
            }
        }

        @JavascriptInterface
        fun vibrate(durationMs: Int) {
            val ctx = activity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(VibrationEffect.createOneShot(durationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    @Deprecated("Use OnBackPressedCallback for API 33+")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
