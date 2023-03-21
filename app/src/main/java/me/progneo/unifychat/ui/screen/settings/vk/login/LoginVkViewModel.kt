package me.progneo.unifychat.ui.screen.settings.vk.login

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.progneo.unifychat.data.enums.VKAuthStatus
import me.progneo.unifychat.data.model.clients.VKClient
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class LoginVkViewModel @Inject constructor(
    private val _client: VKClient
) : ViewModel() {
    fun onStatusChange(webView: WebView, status: VKAuthStatus) {
        if (_client.token == null) {
            when (status) {
                VKAuthStatus.AUTH -> {

                }
                VKAuthStatus.CONFIRM -> {

                }
                VKAuthStatus.ERROR -> {

                }
                VKAuthStatus.BLOCKED -> {

                }
                VKAuthStatus.SUCCESS -> {
                    val url = webView.url!!
                    val tokenMather = Pattern.compile("access_token=[^&]*").matcher(url)
                    val userIdMather = Pattern.compile("user_id=\\w+").matcher(url)
                    if (tokenMather.find() && userIdMather.find()) {
                        val token = tokenMather.group().replace(
                            regex = "access_token=".toRegex(),
                            replacement = ""
                        )
                        if (token.isNotBlank()) {
                            _client.login(token)
                        }
                    }
                }
            }
        }
    }
}