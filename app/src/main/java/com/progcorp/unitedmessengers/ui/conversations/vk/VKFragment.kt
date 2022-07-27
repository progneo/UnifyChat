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
import com.progcorp.unitedmessengers.data.model.companions.Bot
import com.progcorp.unitedmessengers.data.model.companions.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.databinding.FragmentVkBinding
import com.progcorp.unitedmessengers.ui.conversation.ConversationActivity
import com.progcorp.unitedmessengers.ui.conversations.ConversationsListAdapter
import java.lang.Exception

class VKFragment : Fragment() {

    private val _viewModel: VKConversationsViewModel by viewModels { VKConversationsViewModelFactory() }

    private var _viewDataBinding: FragmentVkBinding? = null
    private var _listAdapter: ConversationsListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewDataBinding =
            FragmentVkBinding.inflate(inflater, container, false).apply { viewmodel = _viewModel }
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
            _listAdapter = ConversationsListAdapter(viewModel)
            _viewDataBinding!!.recyclerView.adapter = _listAdapter

            _viewDataBinding!!.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        viewModel.loadMoreConversations()
                    }

                    if (!recyclerView.canScrollVertically(-1)) {
                        if (_viewDataBinding!!.floatButton.isShown) {
                            _viewDataBinding!!.floatButton.hide()
                        }
                        if (!_viewDataBinding!!.mailingFloatButton.isShown) {
                            _viewDataBinding!!.mailingFloatButton.show()
                        }
                    }

                    if (dy > 0) {
                        if (_viewDataBinding!!.floatButton.isShown) {
                            _viewDataBinding!!.floatButton.hide()
                        }
                        if (_viewDataBinding!!.mailingFloatButton.isShown) {
                            _viewDataBinding!!.mailingFloatButton.hide()
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
        _viewModel.selectedConversation.observe(viewLifecycleOwner, EventObserver { navigateToChat(it) } )
        _viewModel.loginEvent.observe(viewLifecycleOwner, EventObserver { navigateToLogin() })
        _viewModel.toTopPressed.observe(viewLifecycleOwner, EventObserver { goToTop() })
        _viewModel.toMailingPressed.observe(viewLifecycleOwner, EventObserver { goToMailing() })
    }

    private fun notifyWithoutScroll(notification: () -> Unit) {
        val recyclerViewState = _viewDataBinding?.recyclerView?.layoutManager?.onSaveInstanceState()
        notification()
        _viewDataBinding?.recyclerView?.layoutManager?.onRestoreInstanceState(recyclerViewState)
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
                Toast.makeText(context, "This conversation are not supported", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_navigation_chats_to_vkAuthFragment)
    }

    private fun goToTop() {
        _viewDataBinding!!.recyclerView.scrollToPosition(0)
    }

    private fun goToMailing() {
        findNavController().navigate(R.id.action_navigation_chats_to_mailing_activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewDataBinding = null
        _listAdapter = null
    }
}