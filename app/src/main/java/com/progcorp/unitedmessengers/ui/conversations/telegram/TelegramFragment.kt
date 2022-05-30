package com.progcorp.unitedmessengers.ui.conversations.telegram

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.Bot
import com.progcorp.unitedmessengers.data.model.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.databinding.FragmentTelegramBinding
import com.progcorp.unitedmessengers.ui.conversation.ConversationActivity
import com.progcorp.unitedmessengers.ui.conversations.ConversationsListAdapter
import kotlinx.android.synthetic.main.activity_conversation.*
import java.lang.Exception

class TelegramFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentTelegramBinding
    private lateinit var listAdapter: ConversationsListAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver

    private val viewModel: TelegramConversationsViewModel by viewModels { TelegramConversationsViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding =
            FragmentTelegramBinding.inflate(inflater, container, false).apply { viewmodel = viewModel }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListAdapter()
        setupObservers()
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapterObserver = (object : RecyclerView.AdapterDataObserver(){
            })
            listAdapter = ConversationsListAdapter(viewModel)
            listAdapter.registerAdapterDataObserver(listAdapterObserver)
            viewDataBinding.recyclerView.adapter = listAdapter
        }
        else {
            throw Exception("The viewmodel is not initialized")
        }
    }

    private fun setupObservers() {
        viewModel.selectedConversation.observe(viewLifecycleOwner, EventObserver { navigateToChat(it) } )
        viewModel.loginEvent.observe(viewLifecycleOwner, EventObserver { navigateToLogin() })
        viewModel.notifyItemInsertedEvent.observe(viewLifecycleOwner, EventObserver {
            listAdapter.notifyItemInserted(it)
        })
        viewModel.notifyItemChangedEvent.observe(viewLifecycleOwner, EventObserver {
            listAdapter.notifyItemChanged(it)
        })
        viewModel.notifyItemMovedEvent.observe(viewLifecycleOwner, EventObserver {
            listAdapter.notifyItemMoved(it.first, it.second)
        })
    }

    private fun navigateToChat(conversation: Conversation) {
        when (conversation.companion) {
            is User, is Bot, is Chat -> {
                val bundle = bundleOf(
                    ConversationActivity.ARGS_CONVERSATION to conversation
                )
                findNavController().navigate(R.id.action_navigation_chats_to_conversation_activity, bundle)
            }
            else -> {
                Toast.makeText(
                    context, "This conversation are not supported", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_navigation_chats_to_tg_auth_activity)
    }
}