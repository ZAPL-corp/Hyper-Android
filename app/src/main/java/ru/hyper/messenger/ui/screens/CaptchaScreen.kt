package ru.hyper.messenger.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ru.hyper.messenger.R
import ru.hyper.messenger.ui.components.HyperBackTopBar
import ru.hyper.messenger.ui.components.SystemBarsForTheme

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun CaptchaScreen(url: String, onBack: () -> Unit, onToken: (String) -> Unit) {
    SystemBarsForTheme()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var delivered by remember { mutableStateOf(false) }

    BackHandler { onBack() }
    DisposableEffect(Unit) {
        onDispose { webView?.stopLoading(); webView?.destroy() }
    }

    fun deliver(token: String) {
        if (delivered || token.isBlank()) return
        delivered = true
        mainHandler.post { onToken(token) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { HyperBackTopBar(title = stringResource(R.string.captcha_title), onBack = onBack) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = stringResource(R.string.captcha_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                            addJavascriptInterface(
                                object {
                                    @JavascriptInterface
                                    fun onToken(token: String) = deliver(token)
                                },
                                "HyperCaptcha",
                            )
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                    val target = url ?: return false
                                    if (target.startsWith("hyper://captcha")) {
                                        deliver(Uri.parse(target).getQueryParameter("token").orEmpty())
                                        return true
                                    }
                                    return false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    view?.evaluateJavascript(
                                        """
                                        (function(){
                                          if(window.__hyperInstalled) return;
                                          window.__hyperInstalled=true;
                                          var sent=false;
                                          function hide(){
                                            var w=document.querySelector('.cf-turnstile');
                                            if(!w) return;
                                            var keep=w;
                                            while(keep.parentElement && keep.parentElement!==document.body) keep=keep.parentElement;
                                            Array.prototype.forEach.call(document.body.children,function(el){ if(el!==keep) el.style.display='none'; });
                                            document.body.style.background='#0b1020';
                                            w.scrollIntoView({block:'center'});
                                          }
                                          setInterval(function(){
                                            hide();
                                            if(sent) return;
                                            var el=document.querySelector('[name="cf-turnstile-response"]');
                                            var t=(el&&el.value)||'';
                                            if(!t && window.turnstile && window.turnstile.getResponse){ try{ t=window.turnstile.getResponse()||''; }catch(e){} }
                                            if(t){ sent=true; HyperCaptcha.onToken(t); }
                                          },800);
                                        })();
                                        """.trimIndent(),
                                        null,
                                    )
                                }
                            }
                            webView = this
                            loadUrl(url)
                        }
                    },
                )
                Text(
                    text = stringResource(R.string.captcha_wait),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                )
            }
        }
    }
}
