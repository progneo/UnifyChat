package com.progcorp.unitedmessengers.ui.login.telegram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.databinding.ActivityTelegramAuthBinding

class TelegramAuthActivity : AppCompatActivity() {

    private val viewModel: TelegramAuthViewModel by viewModels { TelegramAuthViewModelFactory() }

    private lateinit var viewDataBinding: ActivityTelegramAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = ActivityTelegramAuthBinding.inflate(layoutInflater).apply { viewmodel = viewModel }
        val view = viewDataBinding.root
        setContentView(view)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.restartEvent.observe(this, EventObserver { triggerRebirth(this.baseContext) })
    }

    private fun triggerRebirth(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}