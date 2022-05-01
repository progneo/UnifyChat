package com.progcorp.unitedmessengers.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.util.VKAccountService
import com.progcorp.unitedmessengers.util.VKAuthStatus
import java.net.URLEncoder
import java.util.regex.Pattern


class VKAuthFragment : Fragment() {
    private val webView by lazy { WebView(requireContext()) }
    private val _authParams = StringBuilder("https://oauth.vk.com/authorize?").apply {
        append(String.format("%s=%s", URLEncoder.encode("client_id", "UTF-8"), URLEncoder.encode("2685278", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("redirect_uri", "UTF-8"), URLEncoder.encode("https://oauth.vk.com/blank.html", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("display", "UTF-8"), URLEncoder.encode("mobile", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("scope", "UTF-8"), URLEncoder.encode(VKAccountService.SCOPE, "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("response_type", "UTF-8"), URLEncoder.encode("token", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("v", "UTF-8"), URLEncoder.encode("5.131", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("state", "UTF-8"), URLEncoder.encode("12345", "UTF-8")) + "&")
        append(String.format("%s=%s", URLEncoder.encode("revoke", "UTF-8"), URLEncoder.encode("1", "UTF-8")))
    }.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = webView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (App.application.vkAccountService.token == null) {
            webView.webViewClient = AuthWebViewClient(requireContext()) { status ->
                when(status) {
                    VKAuthStatus.AUTH -> {

                    }
                    VKAuthStatus.CONFIRM -> {

                    }
                    VKAuthStatus.ERROR -> {
                        Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
                    }
                    VKAuthStatus.BLOCKED -> {
                        showAuthWindow()
                        Toast.makeText(context, "Аккаунт заблокирован", Toast.LENGTH_LONG).show()
                    }
                    VKAuthStatus.SUCCESS -> {
                        val url = webView.url!!
                        val tokenMather = Pattern.compile("access_token=\\w+").matcher(url)
                        val userIdMather = Pattern.compile("user_id=\\w+").matcher(url)
                        if (tokenMather.find() && userIdMather.find()) {
                            val token = tokenMather.group().replace("access_token=".toRegex(), "")
                            val userId = userIdMather.group().replace("user_id=".toRegex(), "")
                            if (token.isNotEmpty() && userId.isNotEmpty()) {
                                App.application.vkAccountService.token = token
                                App.application.vkAccountService.userId = userId
                            }
                        }
                        Handler().post {
                            val intent = requireActivity().intent
                            intent.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                        or Intent.FLAG_ACTIVITY_NO_ANIMATION
                            )
                            requireActivity().overridePendingTransition(0, 0)
                            requireActivity().finish()
                            requireActivity().overridePendingTransition(0, 0)
                            startActivity(intent)
                        }
                    }
                }
            }
        } else {
            activity?.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        if (App.application.vkAccountService.token == null) {
            showAuthWindow()
        }
    }

    private fun showAuthWindow() {
        CookieManager.getInstance().removeAllCookies(null)
        webView.loadUrl(_authParams)
    }
}