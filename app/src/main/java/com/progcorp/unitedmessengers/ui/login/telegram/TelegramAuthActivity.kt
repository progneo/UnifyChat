package com.progcorp.unitedmessengers.ui.login.telegram

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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

        viewModel.showCodeEvent.observe(this, EventObserver {
            viewDataBinding.securityCode.visibility = View.VISIBLE
            viewDataBinding.password.visibility = View.INVISIBLE
            viewDataBinding.phone.visibility = View.INVISIBLE
            viewDataBinding.logout.visibility = View.VISIBLE
        })
        viewModel.showPhoneEvent.observe(this, EventObserver {
            viewDataBinding.phone.visibility = View.VISIBLE
            viewDataBinding.securityCode.visibility = View.INVISIBLE
            viewDataBinding.password.visibility = View.INVISIBLE
            viewDataBinding.logout.visibility = View.INVISIBLE
        })
        viewModel.showPasswordEvent.observe(this, EventObserver {
            viewDataBinding.password.visibility = View.VISIBLE
            viewDataBinding.securityCode.visibility = View.INVISIBLE
            viewDataBinding.phone.visibility = View.INVISIBLE
            viewDataBinding.logout.visibility = View.VISIBLE
        })
        viewModel.hideAllEvent.observe(this, EventObserver {
            viewDataBinding.securityCode.visibility = View.INVISIBLE
            viewDataBinding.password.visibility = View.INVISIBLE
            viewDataBinding.phone.visibility = View.INVISIBLE
            viewDataBinding.logout.visibility = View.INVISIBLE
        })
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