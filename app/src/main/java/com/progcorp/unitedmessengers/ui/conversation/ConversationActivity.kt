package com.progcorp.unitedmessengers.ui.conversation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.ActivityConversationBinding
import com.progcorp.unitedmessengers.util.functionalityNotAvailable
import kotlinx.android.synthetic.main.activity_conversation.*
import kotlinx.android.synthetic.main.fragment_telegram.view.*
import java.lang.Exception

class ConversationActivity : AppCompatActivity() {
    companion object {
        const val ARGS_CONVERSATION = "conversation"
        const val TAG = "ConversationFragment"
    }

    private val viewModel: ConversationViewModel by viewModels {
        ConversationViewModelFactory(
            intent.getSerializableExtra(ARGS_CONVERSATION) as Conversation
        )
    }

    private lateinit var viewDataBinding: ActivityConversationBinding
    private lateinit var listAdapter: MessagesListAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding = ActivityConversationBinding.inflate(layoutInflater)
            .apply { viewmodel = viewModel }
        viewDataBinding.lifecycleOwner = this
        val view = viewDataBinding.root
        toolbar = view.toolbar
        setSupportActionBar(toolbar)
        setContentView(view)
        setupListAdapter()
        setupObservers()
    }

    private fun setupObservers() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        viewModel.addAttachmentPressed.observe(this, EventObserver {
            functionalityNotAvailable(this)
        })
        viewModel.toBottomPressed.observe(this, EventObserver {
            viewDataBinding.recyclerView.scrollToPosition(0)
        })
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapterObserver = (object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        recycler_view.scrollToPosition(positionStart)
                    }
                }
            })
            listAdapter = MessagesListAdapter(viewModel)
            listAdapter.registerAdapterDataObserver(listAdapterObserver)
            viewDataBinding.recyclerView.adapter = listAdapter

            viewDataBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(-1)) {
                        viewModel.loadMoreMessages()
                    }

                    if (!recyclerView.canScrollVertically(1)) {
                        if (viewDataBinding.floatButton.isShown) {
                            viewDataBinding.floatButton.hide()
                        }
                    }

                    if (dy > 0) {
                        if (!viewDataBinding.floatButton.isShown) {
                            viewDataBinding.floatButton.show()
                        }
                    }
                    else if (dy < 0) {
                        if (viewDataBinding.floatButton.isShown) {
                            viewDataBinding.floatButton.hide()
                        }
                    }
                }
            })
        }
        else {
            throw Exception("The viewmodel is not initialized")
        }
    }

    private fun close() {
        onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopListeners()
        listAdapter.unregisterAdapterDataObserver(listAdapterObserver)
    }
}