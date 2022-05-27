package com.progcorp.unitedmessengers.ui.conversation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.ActivityConversationBinding
import kotlinx.android.synthetic.main.activity_conversation.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding = ActivityConversationBinding.inflate(layoutInflater)
            .apply { viewmodel = viewModel }
        viewDataBinding.lifecycleOwner = this
        val view = viewDataBinding.root
        setContentView(view)
        setupListAdapter()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.backEvent.observe(this, EventObserver { close() } )
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
                    if (!recyclerView.canScrollVertically(-1)) {
                        viewModel.loadMoreMessages()
                    }
                    super.onScrolled(recyclerView, dx, dy)
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