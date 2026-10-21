package co.naliyapara.parivar

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File

class WebActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_URL = "url"
        const val EXTRA_TAB = "tab"
    }

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var errorBox: LinearLayout
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var startUrl = Config.BASE_URL
    private var tabLabel: String? = null
    private var hadError = false

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private val fileChooser =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            val data = res.data
            val results: Array<Uri>? = if (res.resultCode == RESULT_OK) {
                when {
                    data?.clipData != null -> Array(data.clipData!!.itemCount) { i -> data.clipData!!.getItemAt(i).uri }
                    data?.data != null -> arrayOf(data.data!!)
                    else -> null
                }
            } else null
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startUrl = intent.getStringExtra(EXTRA_URL) ?: Config.BASE_URL
        tabLabel = intent.getStringExtra(EXTRA_TAB)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.app_name)

        // ---------- Layout ----------
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE) }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(getColor(R.color.blue))
            setPadding(dp(4), 0, dp(16), 0)
        }
        val back = TextView(this).apply {
            text = "←"
            setTextColor(Color.WHITE)
            textSize = 26f
            gravity = Gravity.CENTER
            setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        bar.addView(back, LinearLayout.LayoutParams(dp(48), dp(56)))
        bar.addView(TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 19f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        root.addView(bar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
        }
        root.addView(progress, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)))

        val frame = FrameLayout(this)
        webView = WebView(this)
        frame.addView(webView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        errorBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }
        errorBox.addView(TextView(this).apply {
            text = "ઇન્ટરનેટ કનેક્શન ચેક કરો"
            textSize = 18f
            setTextColor(getColor(R.color.text_dark))
            gravity = Gravity.CENTER
        })
        errorBox.addView(Button(this).apply {
            text = "ફરી પ્રયત્ન કરો"
            setOnClickListener { hadError = false; errorBox.visibility = View.GONE; webView.loadUrl(startUrl) }
        })
        frame.addView(errorBox, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        root.addView(frame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)

        // ---------- WebView settings ----------
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = false
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        webView.addJavascriptInterface(Bridge(), "NpBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return handleUrl(request.url)
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                hadError = false
                progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String?) {
                progress.visibility = View.GONE
                if (!hadError) errorBox.visibility = View.GONE
                if (url != null && Uri.parse(url).host?.endsWith(Config.HOST) == true) {
                    view.evaluateJavascript(buildInjectScript(), null)
                }
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    hadError = true
                    errorBox.visibility = View.VISIBLE
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }

            override fun onShowFileChooser(
                webView: WebView,
                callback: ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                return try {
                    fileChooser.launch(params.createIntent())
                    true
                } catch (e: Exception) {
                    filePathCallback = null
                    false
                }
            }

            // window.open / target=_blank -> external app (WhatsApp, browser, ...)
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
                val temp = WebView(this@WebActivity)
                temp.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView, req: WebResourceRequest): Boolean {
                        openExternal(req.url)
                        v.destroy()
                        return true
                    }
                }
                (resultMsg.obj as WebView.WebViewTransport).webView = temp
                resultMsg.sendToTarget()
                return true
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            if (url.startsWith("http")) {
                try {
                    val req = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(mimeType)
                        addRequestHeader("User-Agent", userAgent)
                        addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url) ?: "")
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            URLUtil.guessFileName(url, contentDisposition, mimeType)
                        )
                    }
                    (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
                    Toast.makeText(this, "ડાઉનલોડ શરૂ થયું", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    openExternal(Uri.parse(url))
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        if (savedInstanceState == null) {
            if (isOnline()) webView.loadUrl(startUrl) else { hadError = true; errorBox.visibility = View.VISIBLE }
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onPause() { super.onPause(); CookieManager.getInstance().flush() }

    override fun onDestroy() {
        webView.stopLoading()
        (webView.parent as? ViewGroup)?.removeView(webView)
        webView.destroy()
        super.onDestroy()
    }

    // ---------------------------------------------------------------
    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun handleUrl(uri: Uri): Boolean {
        val scheme = uri.scheme ?: return false
        val host = uri.host ?: ""
        if ((scheme == "http" || scheme == "https") && host.endsWith(Config.HOST)) return false
        openExternal(uri)
        return true
    }

    private fun openExternal(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "આ લિંક ખોલી શકાઈ નહીં", Toast.LENGTH_SHORT).show()
        }
    }

    /** Website nu header chhupave, ane jo tile mate tab hoy to te tab khole. Download/Share bridge pan joDe. */
    private fun buildInjectScript(): String {
        val tab = tabLabel
        val label = if (tab == null) "null" else JSONObject.quote(tab)
        val hideHeader = tab != null
        return """
(function(){
  var label = $label;
  var hideHeader = $hideHeader;

  if (hideHeader && !document.getElementById('np-app-style')) {
    var st = document.createElement('style');
    st.id = 'np-app-style';
    st.textContent = 'header{display:none!important}.mobile-menu-trigger{display:none!important}';
    document.head.appendChild(st);
  }

  if (!window.__npBridgeInit) {
    window.__npBridgeInit = true;
    function b64(blob){return new Promise(function(res,rej){var r=new FileReader();r.onloadend=function(){res(String(r.result).split(',')[1]||'')};r.onerror=rej;r.readAsDataURL(blob);});}
    var origClick = HTMLAnchorElement.prototype.click;
    HTMLAnchorElement.prototype.click = function(){
      var a = this;
      if (a.hasAttribute('download') && /^(blob:|data:)/.test(a.href)) {
        fetch(a.href).then(function(r){return r.blob();}).then(function(bl){
          return b64(bl).then(function(d){ NpBridge.saveFile(a.download||'file', bl.type||'application/octet-stream', d); });
        }).catch(function(){});
        return;
      }
      return origClick.apply(this, arguments);
    };
    try {
      Object.defineProperty(navigator, 'canShare', {value: function(){return true;}, configurable: true});
      Object.defineProperty(navigator, 'share', {configurable: true, value: function(data){
        data = data || {};
        var files = data.files || [];
        var extra = ((data.text||data.title||'') + (data.url ? (' ' + data.url) : '')).trim();
        if (files.length) {
          return b64(files[0]).then(function(d){ NpBridge.shareFile(files[0].name||'file', files[0].type||'application/octet-stream', d, extra); });
        }
        NpBridge.shareText(((data.title||'') + '\n' + (data.text||'') + '\n' + (data.url||'')).trim());
        return Promise.resolve();
      }});
    } catch(e) {}
  }

  if (!label || window.__npGoLabel === label) return;
  var tries = 0;
  var t = setInterval(function(){
    tries++;
    var btns = document.querySelectorAll('button[data-label]');
    for (var i = 0; i < btns.length; i++) {
      if (btns[i].getAttribute('data-label') === label) {
        btns[i].click();
        window.__npGoLabel = label;
        clearInterval(t);
        return;
      }
    }
    if (tries > 120) clearInterval(t);
  }, 150);
})();
"""
    }

    // ---------------------------------------------------------------
    // JS -> Android bridge (download / share for poster maker etc.)
    inner class Bridge {
        private fun safeName(n: String) = n.replace(Regex("[^\\w.\\- ]"), "_").ifBlank { "file" }

        @JavascriptInterface
        fun saveFile(name: String, mime: String, base64: String) {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val fname = safeName(name)
            runOnUiThread {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, fname)
                            put(MediaStore.Downloads.MIME_TYPE, mime)
                            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        if (uri != null) {
                            contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                            Toast.makeText(this@WebActivity, "Downloads ફોલ્ડરમાં સેવ થયું", Toast.LENGTH_LONG).show()
                            return@runOnUiThread
                        }
                    }
                    shareBytes(bytes, fname, mime, "")
                } catch (e: Exception) {
                    Toast.makeText(this@WebActivity, "સેવ થઈ શક્યું નહીં", Toast.LENGTH_SHORT).show()
                }
            }
        }

        @JavascriptInterface
        fun shareFile(name: String, mime: String, base64: String, text: String) {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            runOnUiThread { shareBytes(bytes, safeName(name), mime, text) }
        }

        @JavascriptInterface
        fun shareText(text: String) {
            runOnUiThread {
                val i = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
                startActivity(Intent.createChooser(i, "શેર કરો"))
            }
        }
    }

    private fun shareBytes(bytes: ByteArray, fname: String, mime: String, text: String) {
        val dir = File(cacheDir, "shared").apply { mkdirs() }
        val f = File(dir, fname).apply { writeBytes(bytes) }
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
        val i = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            if (text.isNotBlank()) putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(i, "શેર કરો"))
    }
}
