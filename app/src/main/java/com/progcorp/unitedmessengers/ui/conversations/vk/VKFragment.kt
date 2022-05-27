package com.progcorp.unitedmessengers.ui.conversations.vk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.Bot
import com.progcorp.unitedmessengers.data.model.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.User
import com.progcorp.unitedmessengers.databinding.FragmentVkBinding
import com.progcorp.unitedmessengers.ui.conversation.ConversationActivity
import com.progcorp.unitedmessengers.ui.conversations.ConversationsListAdapter
import java.lang.Exception

class VKFragment : Fragment() {

    private val viewModel: VKConversationsViewModel by viewModels { VKConversationsViewModelFactory() }

    private lateinit var viewDataBinding: FragmentVkBinding
    private lateinit var listAdapter: ConversationsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding =
            FragmentVkBinding.inflate(inflater, container, false).apply { viewmodel = viewModel }
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
            listAdapter = ConversationsListAdapter(viewModel)
            viewDataBinding.recyclerView.adapter = listAdapter

            viewDataBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!recyclerView.canScrollVertically(1)) {
                        viewModel.loadMoreConversations()
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }
            })
        }
        else {
            throw Exception("The viewmodel is not initialized")
        }
    }

    private fun setupObservers() {
        viewModel.selectedConversation.observe(viewLifecycleOwner, EventObserver { navigateToChat(it) } )
        viewModel.loginEvent.observe(viewLifecycleOwner, EventObserver { navigateToLogin() })
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
        findNavController().navigate(R.id.action_navigation_chats_to_vkAuthFragment)
    }
}