package com.progcorp.unitedmessengers.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.databinding.ActivityMainBinding
import com.progcorp.unitedmessengers.enums.TelegramAuthStatus
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    private var _navView: BottomNavigationView? = null
    private var _vkBadge: BadgeDrawable? = null
    private var _tgBadge: BadgeDrawable? = null

    private var _viewDataBinding: ActivityMainBinding? = null

    private val _viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewDataBinding = ActivityMainBinding.inflate(layoutInflater).apply { viewmodel = _viewModel }
        val view = _viewDataBinding?.root
        setContentView(view)

        _navView = view?.nav_view

        _vkBadge = _navView!!.getOrCreateBadge(R.id.navigation_vk).apply { isVisible = false }
        _tgBadge = _navView!!.getOrCreateBadge(R.id.navigation_telegram).apply { isVisible = false }
        setupObservers()

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navController = findNavController(R.id.nav_host_fragment)
        _navView!!.setupWithNavController(navController)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (App.application.tgClient.authState.value == TelegramAuthStatus.AUTHENTICATED) {
                        if (App.application.tgClient.isLoaded.value == true) {
                            content.viewTreeObserver.removeOnPreDrawListener(this)
                            true
                        }
                        else {
                            false
                        }
                    } else {
                        true
                    }
                }
            }
        )
    }

    private fun setupObservers() {
        _viewModel.vkUnreadCount.observe(this) {
            it?.let {
                _vkBadge?.isVisible = true
                _vkBadge?.number = _viewModel.vkUnreadCount.value ?: 0
            } ?: run {
                _vkBadge?.isVisible = false
            }
        }
        _viewModel.tgUnreadCount.observe(this) {
            it?.let {
                _tgBadge?.isVisible = true
                _tgBadge?.number = _viewModel.tgUnreadCount.value ?: 0
            } ?: run {
                _tgBadge?.isVisible = false
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}