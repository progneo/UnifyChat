package com.progcorp.unitedmessengers.ui.login.vk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.clients.VKClient
import com.progcorp.unitedmessengers.databinding.ActivityVkAuthBinding
import com.progcorp.unitedmessengers.enums.VKAuthStatus
import kotlinx.android.synthetic.main.activity_vk_auth.view.*
import java.net.URLEncoder
import java.util.regex.Pattern


class VKAuthActivity : AppCompatActivity() {
    private lateinit var viewDataBinding: ActivityVkAuthBinding
    private lateinit var webView: WebView

    private val _client = App.application.vkClient
    private val _authParams = StringBuilder("https://oauth.vk.com/authorize?").apply {
        append(String.format("%s=%s", URLEncoder.encode("client_id", "UTF-8"), URLEncoder.encode("2685278", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("redirect_uri", "UTF-8"), URLEncoder.encode("https://oauth.vk.com/blank.html", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("display", "UTF-8"), URLEncoder.encode("mobile", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("scope", "UTF-8"), URLEncoder.encode(VKClient.SCOPE, "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("response_type", "UTF-8"), URLEncoder.encode("token", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("v", "UTF-8"), URLEncoder.encode("5.131", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("state", "UTF-8"), URLEncoder.encode("12345", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("revoke", "UTF-8"), URLEncoder.encode("1", "UTF-8")))
    }.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = ActivityVkAuthBinding.inflate(layoutInflater)
        val view = viewDataBinding.root
        setContentView(view)
        webView = view.webView
        if (_client.token == null) {
            webView.webViewClient = AuthWebViewClient(baseContext) { status ->
                when(status) {
                    VKAuthStatus.AUTH -> {

                    }
                    VKAuthStatus.CONFIRM -> {

                    }
                    VKAuthStatus.ERROR -> {
                        Toast.makeText(baseContext, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
                    }
                    VKAuthStatus.BLOCKED -> {
                        showAuthWindow()
                        Toast.makeText(baseContext, "Аккаунт заблокирован", Toast.LENGTH_LONG).show()
                    }
                    VKAuthStatus.SUCCESS -> {
                        val url = webView.url!!
                        val tokenMather = Pattern.compile("access_token=[^&]*").matcher(url)
                        val userIdMather = Pattern.compile("user_id=\\w+").matcher(url)
                        if (tokenMather.find() && userIdMather.find()) {
                            val token = tokenMather.group().replace("access_token=".toRegex(), "")
                            val userId = userIdMather.group().replace("user_id=".toRegex(), "")
                            if (token.isNotEmpty() && userId.isNotEmpty()) {
                                App.application.vkClient.token = token
                                App.application.vkClient.userId = userId
                            }
                        }
                        Thread.sleep(4000)
                        triggerRebirth(baseContext)
                    }
                }
            }
        } else {
            onBackPressed()
        }
    }

    private fun triggerRebirth(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    override fun onStart() {
        super.onStart()
        if (App.application.vkClient.token == null) {
            showAuthWindow()
        }
    }

    private fun showAuthWindow() {
        CookieManager.getInstance().removeAllCookies(null)
        webView.loadUrl(_authParams)
    }
}