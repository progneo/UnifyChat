package com.progcorp.unitedmessengers.ui.login.vk

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.progcorp.unitedmessengers.util.VKAccountService
import com.progcorp.unitedmessengers.enums.VKAuthStatus
import java.net.URLEncoder

class AuthWebViewClient(
    private val context: Context,
    private val onStatusChange: (status: VKAuthStatus) -> Unit
) : WebViewClient() {
    private var _currentUrl = ""

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(wv: WebView, url: String): Boolean {
        wv.loadUrl(url)
        return true
    }

    override fun onPageFinished(wv: WebView, url: String) {
        if (_currentUrl != url) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (url.contains("https://oauth.vk.com/authorize")) {
                val scope = URLEncoder.encode(VKAccountService.SCOPE, "UTF-8")
                if (url.contains(scope)) {
                    imm.showSoftInput(wv, 0)
                    wv.visibility = View.VISIBLE
                    onStatusChange(VKAuthStatus.AUTH)
                }
                if (url.contains("q_hash")) {
                    onStatusChange(VKAuthStatus.CONFIRM)
                }
                if (url.contains("email")) {
                    onStatusChange(VKAuthStatus.ERROR)
                }
            }
            if (url.contains("https://m.vk.com/login\\?act=blocked")) {
                onStatusChange(VKAuthStatus.BLOCKED)
            }
            if (url.contains("https://oauth.vk.com/blank.html")) {
                wv.visibility = View.INVISIBLE
                onStatusChange(VKAuthStatus.SUCCESS)
            }
        }
        _currentUrl = url
    }
}