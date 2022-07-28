package com.progcorp.unitedmessengers.ui.conversations.telegram

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.companions.Bot
import com.progcorp.unitedmessengers.data.model.companions.Chat
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.databinding.FragmentTelegramBinding
import com.progcorp.unitedmessengers.ui.conversation.ConversationActivity
import com.progcorp.unitedmessengers.ui.conversations.ConversationsListAdapter
import com.progcorp.unitedmessengers.util.functionalityNotAvailable
import java.lang.Exception

class TelegramFragment : Fragment(R.layout.fragment_telegram) {
    private var _viewDataBinding: FragmentTelegramBinding? = null
    private var _listAdapter: ConversationsListAdapter? = null
    private var _listAdapterObserver: RecyclerView.AdapterDataObserver? = null

    private val _viewModel: TelegramConversationsViewModel by viewModels { TelegramConversationsViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewDataBinding = FragmentTelegramBinding
            .inflate(inflater, container, false).apply { viewmodel = _viewModel }
        _viewDataBinding!!.lifecycleOwner = this.viewLifecycleOwner
        return _viewDataBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListAdapter()
        setupObservers()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.top_app_bar_conversations, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.search -> {
                        context?.let {
                            functionalityNotAvailable(it)
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
        _viewModel.notifyItemInsertedEvent.observe(viewLifecycleOwner, EventObserver {
            notifyWithoutScroll { _listAdapter?.notifyItemInserted(it) }
        })
        _viewModel.notifyItemChangedEvent.observe(viewLifecycleOwner, EventObserver {
            _listAdapter?.notifyItemChanged(it)
        })
        _viewModel.notifyItemMovedEvent.observe(viewLifecycleOwner, EventObserver {
            notifyWithoutScroll { _listAdapter?.notifyItemMoved(it.first, it.second) }
        })
        _viewModel.notifyItemRangeChangedEvent.observe(viewLifecycleOwner, EventObserver {
            notifyWithoutScroll { _listAdapter?.notifyItemRangeChanged(it.first, it.second) }
        })
        _viewModel.notifyDatasetChangedEvent.observe(viewLifecycleOwner, EventObserver {
            notifyWithoutScroll { _listAdapter?.notifyDataSetChanged() }
        })
    }

    private fun notifyWithoutScroll(notification: () -> Unit) {
        val recyclerViewState = _viewDataBinding?.recyclerView?.layoutManager?.onSaveInstanceState()
        notification()
        _viewDataBinding?.recyclerView?.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    override fun onResume() {
        super.onResume()
        _listAdapter?.notifyDataSetChanged()
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

    private fun goToMailing() {
        findNavController().navigate(R.id.action_navigation_chats_to_mailing_activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewDataBinding = null
        _listAdapter = null
        _listAdapterObserver = null
    }
}