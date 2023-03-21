package me.progneo.unifychat.ui.screen.settings.vk.login

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.progneo.unifychat.data.enums.VKAuthStatus
import me.progneo.unifychat.data.model.clients.VKClient
import java.net.URLEncoder

@Composable
fun LoginVkScreen(
    navController: NavHostController,
    viewModel: LoginVkViewModel = hiltViewModel()
) {
    val authParams = StringBuilder("https://oauth.vk.com/authorize?").apply {
        append(String.format("%s=%s", URLEncoder.encode("client_id", "UTF-8"), URLEncoder.encode("2685278", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("redirect_uri", "UTF-8"), URLEncoder.encode("https://oauth.vk.com/blank.html", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("display", "UTF-8"), URLEncoder.encode("mobile", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("scope", "UTF-8"), URLEncoder.encode(
            VKClient.SCOPE, "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("response_type", "UTF-8"), URLEncoder.encode("token", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("v", "UTF-8"), URLEncoder.encode("5.131", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("state", "UTF-8"), URLEncoder.encode("12345", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("revoke", "UTF-8"), URLEncoder.encode("1", "UTF-8")))
    }.toString()

    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.statusBarsPadding(),
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
    
                webViewClient = object : WebViewClient() {
                    var currentUrl = ""
    
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        view.loadUrl(url)
                        return true
                    }
    
                    override fun onPageFinished(view: WebView, url: String) {
                        if (currentUrl != url) {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            if (url.contains("https://oauth.vk.com/authorize")) {
                                val scope = URLEncoder.encode(VKClient.SCOPE, "UTF-8")
                                if (url.contains(scope)) {
                                    imm.showSoftInput(view, 0)
                                    view.visibility = View.VISIBLE
                                    viewModel.onStatusChange(view, VKAuthStatus.AUTH)
                                }
                                if (url.contains("q_hash")) {
                                    viewModel.onStatusChange(view, VKAuthStatus.CONFIRM)
                                }
                                if (url.contains("email")) {
                                    Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
                                    viewModel.onStatusChange(view, VKAuthStatus.ERROR)
                                }
                            }
                            if (url.contains("https://m.vk.com/login\\?act=blocked")) {
                                Toast.makeText(context, "Аккаунт заблокирован", Toast.LENGTH_LONG).show()
                                viewModel.onStatusChange(view, VKAuthStatus.BLOCKED)
                                CookieManager.getInstance().removeAllCookies(null)
                                loadUrl(authParams)
                            }
                            if (url.contains("https://oauth.vk.com/blank.html")) {
                                view.visibility = View.INVISIBLE
                                viewModel.onStatusChange(view, VKAuthStatus.SUCCESS)
                                navController.popBackStack()
                            }
                        }
    
                        currentUrl = url
                    }
                }
    
                loadUrl(authParams)
            }
        },
        update = {
            it.loadUrl(authParams)
        }
    )
}