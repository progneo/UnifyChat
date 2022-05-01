package com.progcorp.unitedmessengers.ui.login.vk

import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.progcorp.unitedmessengers.databinding.ActivityVkLoginBinding
import com.progcorp.unitedmessengers.ui.main.MainActivity

import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException

class VKLoginActivity : AppCompatActivity(){

    private lateinit var binding: ActivityVkLoginBinding
    private lateinit var authLauncher: ActivityResultLauncher<Collection<VKScope>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (VK.isLoggedIn()) {
            MainActivity.startFrom(this)
            finish()
            return
        }

        binding = ActivityVkLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        authLauncher = VK.login(this) { result : VKAuthenticationResult ->
            when (result) {
                is VKAuthenticationResult.Success -> onLogin()
                is VKAuthenticationResult.Failed -> onLoginFailed(result.exception)
            }
        }
        binding.login.setOnClickListener {
            authLauncher.launch(arrayListOf(VKScope.MESSAGES))
        }
    }

    private fun onLogin() {
        Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show()

        finish()
    }

    private fun onLoginFailed(exception: VKAuthException) {
        if (!exception.isCanceled) {
            if (exception.webViewError == WebViewClient.ERROR_HOST_LOOKUP) {
                Toast.makeText(this, "Connection error", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}