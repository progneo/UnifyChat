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

    private var _viewDataBinding: ActivityTelegramAuthBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _viewDataBinding = ActivityTelegramAuthBinding.inflate(layoutInflater).apply { viewmodel = viewModel }
        val view = _viewDataBinding?.root
        setContentView(view)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.restartEvent.observe(this, EventObserver { triggerRebirth(this.baseContext) })

        viewModel.authEvent.observe(this, EventObserver { onBackPressed() })

        viewModel.showCodeEvent.observe(this, EventObserver {
            _viewDataBinding?.let {
                it.securityCode.visibility = View.VISIBLE
                it.password.visibility = View.INVISIBLE
                it.phone.visibility = View.INVISIBLE
                it.logout.visibility = View.VISIBLE
            }
        })
        viewModel.showPhoneEvent.observe(this, EventObserver {
            _viewDataBinding?.let {
                it.phone.visibility = View.VISIBLE
                it.securityCode.visibility = View.INVISIBLE
                it.password.visibility = View.INVISIBLE
                it.logout.visibility = View.INVISIBLE
            }
        })
        viewModel.showPasswordEvent.observe(this, EventObserver {
            _viewDataBinding?.let {
                it.password.visibility = View.VISIBLE
                it.securityCode.visibility = View.INVISIBLE
                it.phone.visibility = View.INVISIBLE
                it.logout.visibility = View.VISIBLE
            }
        })
        viewModel.hideAllEvent.observe(this, EventObserver {
            _viewDataBinding?.let {
                it.securityCode.visibility = View.INVISIBLE
                it.password.visibility = View.INVISIBLE
                it.phone.visibility = View.INVISIBLE
                it.logout.visibility = View.INVISIBLE
            }
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