package com.progcorp.unitedmessengers.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.util.forceHideKeyboard
import com.vk.api.sdk.VKTokenExpiredHandler

class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView
    private lateinit var mainProgressBar: ProgressBar
    private lateinit var vkBadge: BadgeDrawable
    private lateinit var telegramBadge: BadgeDrawable

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tokenTracker

        navView = findViewById(R.id.nav_view)
        mainProgressBar = findViewById(R.id.main_progressBar)

        vkBadge =
            navView.getOrCreateBadge(R.id.navigation_vk).apply { isVisible = false }

        telegramBadge =
            navView.getOrCreateBadge(R.id.navigation_telegram).apply { isVisible = false }

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.chatFragment -> navView.visibility = View.GONE
                R.id.dialogFragment -> navView.visibility = View.GONE
                else -> navView.visibility = View.VISIBLE
            }
            showGlobalProgressBar(false)
            currentFocus?.rootView?.forceHideKeyboard()
        }

        //val appBarConfiguration = AppBarConfiguration(
        //    setOf(
        //        R.id.navigation_telegram,
        //        R.id.navigation_vk
        //    )
        //)
        //
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            Log.i("VK Token", "Token expired.")
            val intent = intent
            finish()
            startActivity(intent)
        }
    }

    fun showGlobalProgressBar(isShow: Boolean) {
        if (isShow) mainProgressBar.visibility = View.VISIBLE
        else mainProgressBar.visibility = View.GONE
    }

    companion object {
        private const val TAG = "MainActivity"

        fun startFrom(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}