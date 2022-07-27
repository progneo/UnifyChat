package com.progcorp.unitedmessengers.ui.mailing

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.databinding.ActivityMailingBinding
import com.progcorp.unitedmessengers.enums.MailingState
import kotlinx.android.synthetic.main.fragment_telegram.view.*

class MailingActivity : AppCompatActivity() {
    private val _viewModel: MailingViewModel by viewModels {
        MailingViewModelFactory()
    }

    private var _viewDataBinding: ActivityMailingBinding? = null
    private var _listAdapter: MailingListAdapter? = null
    private var _listAdapterObserver: RecyclerView.AdapterDataObserver? = null
    private var _toolbar: MaterialToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewDataBinding = ActivityMailingBinding.inflate(layoutInflater)
            .apply { viewmodel = _viewModel }
        _viewDataBinding?.lifecycleOwner = this
        val view = _viewDataBinding?.root
        _toolbar = view?.toolbar
        setSupportActionBar(_toolbar)
        setContentView(view)
        setupListAdapter()
        setupObservers()
    }

    private fun setupListAdapter() {
        val viewModel = _viewDataBinding?.viewmodel
        if (viewModel != null) {
            _listAdapter = MailingListAdapter(viewModel)
            _viewDataBinding?.recyclerView?.adapter = _listAdapter
        }
        else {
            throw Exception("The view model is not initialized")
        }
    }

    private fun setupObservers() {
        _toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        _viewModel.confirmConversationsListEvent.observe(this, EventObserver{
            val animationIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in_animation)
            val animationOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out_animation)
            _viewDataBinding?.let {
                it.conversationsList.startAnimation(animationOut)
                Handler(Looper.getMainLooper()).postDelayed({
                    it.conversationsList.visibility = View.GONE
                }, 300)
                it.messageInput.startAnimation(animationIn)
                it.messageInput.visibility = View.VISIBLE
            }
        })
        _viewModel.sendMessageEvent.observe(this, EventObserver{
            _viewModel.startMailing()
        })
        _viewModel.notifyItemRemovedEvent.observe(this, EventObserver{ index ->
            _listAdapter?.notifyItemRemoved(index)
        })
    }

    override fun onBackPressed() {
        if (_viewModel.currentState.value == MailingState.Message) {
            _viewModel.setState(MailingState.Conversations)
            val animationIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in_animation)
            val animationOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out_animation)
            _viewDataBinding?.let {
                it.messageInput.startAnimation(animationOut)
                Handler(Looper.getMainLooper()).postDelayed({
                    it.messageInput.visibility = View.GONE
                }, 300)
                it.conversationsList.startAnimation(animationIn)
                it.conversationsList.visibility = View.VISIBLE
            }
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewDataBinding = null
        _listAdapter = null
        _listAdapterObserver = null
        _toolbar = null
    }
}