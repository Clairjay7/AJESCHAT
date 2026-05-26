package com.example.ajeschat.ui.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ajeschat.data.ApiModule
import com.example.ajeschat.session.SessionStore
import com.example.ajeschat.ui.theme.ajesLogoTopAppBarColors
import com.example.ajeschat.ui.theme.ajesScreenBackground
import java.net.URI

/**
 * Loads AJES principal modules in embed mode (content only — no site topbar / main sidebar).
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjesWebScreen(
    webPath: String,
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val token = remember { SessionStore(context.applicationContext).load()?.token.orEmpty() }
    val baseUrl = remember { ApiModule.baseUrl.trimEnd('/') + "/" }
    val startUrl = remember(webPath) {
        val path = webPath.trim().trimStart('/')
        val withEmbed = if (path.contains("embed=1")) path else {
            if (path.contains("?")) "$path&embed=1" else "$path?embed=1"
        }
        baseUrl + withEmbed
    }
    val baseHost = remember(startUrl) {
        runCatching { URI(startUrl).host?.lowercase().orEmpty() }.getOrDefault("")
    }
    val authHeaders = remember(token) {
        buildMap {
            put("Accept", "text/html,application/xhtml+xml")
            put("X-AJES-Embed", "1")
            if (token.isNotBlank()) {
                put("Authorization", "Bearer $token")
                put("X-Bearer-Token", token)
            }
        }
    }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun appendEmbed(url: String): String {
        if (url.contains("embed=1")) return url
        return if (url.contains("?")) "$url&embed=1" else "$url?embed=1"
    }

    fun isAjesAdminModule(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("/admin/sections") || lower.contains("/admin/academic-years")
    }

    Scaffold(
        modifier = Modifier.ajesScreenBackground(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = ajesLogoTopAppBarColors()
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (error != null) {
                Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            setBackgroundColor(AndroidColor.parseColor("#E8F5E9"))
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    loading = true
                                    error = null
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    loading = false
                                    view?.evaluateJavascript(EMBED_CHROME_JS, null)
                                }

                                @Deprecated("Deprecated in Java")
                                override fun onReceivedError(
                                    view: WebView?,
                                    errorCode: Int,
                                    description: String?,
                                    failingUrl: String?
                                ) {
                                    loading = false
                                    error = description ?: "Could not load page"
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    val host = runCatching {
                                        URI(url).host?.lowercase().orEmpty()
                                    }.getOrDefault("")
                                    if (host.isNotEmpty() && host == baseHost && isAjesAdminModule(url)) {
                                        view?.loadUrl(appendEmbed(url), authHeaders)
                                        return true
                                    }
                                    return false
                                }
                            }
                            if (token.isBlank()) {
                                error = "Not signed in. Log in again."
                                loading = false
                            } else {
                                loadUrl(startUrl, authHeaders)
                            }
                        }
                    }
                )
                if (loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

/** Fallback: hide AJES site chrome if a sub-page loads without embed view. */
private const val EMBED_CHROME_JS = """
(function(){
  var s=document.getElementById('ajes-mobile-embed-style');
  if(!s){
    s=document.createElement('style');
    s.id='ajes-mobile-embed-style';
    s.textContent='.topbar,.sidebar,.layout>.sidebar,aside.sidebar{display:none!important}' +
      '.layout{display:block!important}main.content,.content{margin:0!important;padding:0!important;width:100%!important}' +
      'body.mobile-embed{background:#e8f5e9!important;color:#000!important}';
    document.head.appendChild(s);
  }
  if(!document.body.classList.contains('mobile-embed')) document.body.classList.add('mobile-embed');
})();
"""
