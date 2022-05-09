package com.progcorp.unitedmessengers.ui.main

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.databinding.ActivityMainBinding
import com.progcorp.unitedmessengers.util.forceHideKeyboard
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView
    private lateinit var mainProgressBar: ProgressBar
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
        mainProgressBar = view.main_progressBar

        vkBadge =
            navView.getOrCreateBadge(R.id.navigation_vk).apply { isVisible = false }

        telegramBadge =
            navView.getOrCreateBadge(R.id.navigation_telegram).apply { isVisible = false }

        supportActionBar?.setDisplayShowTitleEnabled(false)


        val navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.chatFragment -> {
                    navView.visibility = View.GONE
                }
                R.id.dialogFragment -> {
                    navView.visibility = View.GONE
                }
                R.id.vkAuthFragment -> {
                    navView.visibility = View.GONE
                }
                R.id.telegramAuthFragment -> {
                    navView.visibility = View.GONE
                }
                else -> {
                    navView.visibility = View.VISIBLE
                }
            }
            showGlobalProgressBar(false)
            currentFocus?.rootView?.forceHideKeyboard()
        }

        navView.setupWithNavController(navController)
    }

    fun showGlobalProgressBar(isShow: Boolean) {
        if (isShow) mainProgressBar.visibility = View.VISIBLE
        else mainProgressBar.visibility = View.GONE
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}