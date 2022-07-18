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
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.companions.Bot
import com.progcorp.unitedmessengers.data.model.companions.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.databinding.FragmentTelegramBinding
import com.progcorp.unitedmessengers.ui.conversation.ConversationActivity
import com.progcorp.unitedmessengers.ui.conversations.ConversationsListAdapter
import java.lang.Exception

class TelegramFragment : Fragment() {
    private var _viewDataBinding: FragmentTelegramBinding? = null
    private var _listAdapter: ConversationsListAdapter? = null
    private var _listAdapterObserver: RecyclerView.AdapterDataObserver? = null

    private val viewModel: TelegramConversationsViewModel by viewModels { TelegramConversationsViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewDataBinding = FragmentTelegramBinding
            .inflate(inflater, container, false).apply { viewmodel = viewModel }
        _viewDataBinding!!.lifecycleOwner = this.viewLifecycleOwner
        return _viewDataBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListAdapter()
        setupObservers()
    }

    private fun setupListAdapter() {
        val viewModel = _viewDataBinding!!.viewmodel
        if (viewModel != null) {
            _listAdapterObserver = (object : RecyclerView.AdapterDataObserver(){
            })
            _listAdapter = ConversationsListAdapter(viewModel)
            _listAdapter!!.registerAdapterDataObserver(_listAdapterObserver!!)
            _viewDataBinding!!.recyclerView.adapter = _listAdapter

            _viewDataBinding!!.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(-1)) {
                        if (_viewDataBinding!!.floatButton.isShown) {
                            _viewDataBinding!!.floatButton.hide()
                        }
                    }

                    if (dy > 0) {
                        if (_viewDataBinding!!.floatButton.isShown) {
                            _viewDataBinding!!.floatButton.hide()
                        }
                    }
                    else if (dy < 0) {
                        if (!_viewDataBinding!!.floatButton.isShown) {
                            _viewDataBinding!!.floatButton.show()
                        }
                    }
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
        viewModel.toTopPressed.observe(viewLifecycleOwner, EventObserver { goToTop() })
        viewModel.notifyItemInsertedEvent.observe(viewLifecycleOwner, EventObserver {
            _listAdapter?.notifyItemInserted(it)
        })
        viewModel.notifyItemChangedEvent.observe(viewLifecycleOwner, EventObserver {
            _listAdapter?.notifyItemChanged(it)
        })
        viewModel.notifyItemMovedEvent.observe(viewLifecycleOwner, EventObserver {
            _listAdapter?.notifyItemMoved(it.first, it.second)
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

    private fun goToTop() {
        _viewDataBinding!!.recyclerView.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewDataBinding = null
        _listAdapter = null
        _listAdapterObserver = null
    }
}