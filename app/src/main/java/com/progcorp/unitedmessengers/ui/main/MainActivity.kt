package com.progcorp.unitedmessengers.ui.main

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView
    private lateinit var vkBadge: BadgeDrawable
    private lateinit var telegramBadge: BadgeDrawable

    private lateinit var viewDataBinding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding = ActivityMainBinding.inflate(layoutInflater).apply { viewmodel = viewModel }
        val view = viewDataBinding.root
        setContentView(view)

        navView = view.nav_view

        vkBadge =
            navView.getOrCreateBadge(R.id.navigation_vk).apply { isVisible = false }

        telegramBadge =
            navView.getOrCreateBadge(R.id.navigation_telegram).apply { isVisible = false }

        supportActionBar?.setDisplayShowTitleEnabled(false)


        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}