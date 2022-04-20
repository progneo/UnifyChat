package com.progcorp.unitedmessengers.ui.conversations.vk

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.FragmentVkBinding
import com.progcorp.unitedmessengers.ui.chat.ChatFragment
import com.progcorp.unitedmessengers.ui.dialog.DialogFragment
import java.lang.Exception

class ConversationsFragment : Fragment() {

    private val viewModel: ConversationsViewModel by viewModels { ConversationViewModelFactory() }

    private lateinit var viewDataBinding: FragmentVkBinding
    private lateinit var listAdapter: ConversationsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        when (conversation.type) {
            "chat" -> {
                val bundle = bundleOf(
                    ChatFragment.ARGS_CONVERSATION to conversation
                )
                findNavController().navigate(R.id.action_navigation_chats_to_chatFragment, bundle)
            }
            "user" -> {
                val bundle = bundleOf(
                    DialogFragment.ARGS_CONVERSATION to conversation
                )
                findNavController().navigate(R.id.action_navigation_chats_to_dialogFragment, bundle)
            }
            else -> {
                Toast.makeText(
                    context, "Dialogs with groups are not supported", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_navigation_chats_to_loginActivity)
    }
}