package com.progcorp.unitedmessengers.ui.conversation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.FragmentDialogBinding
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModelFactory
import com.progcorp.unitedmessengers.ui.conversation.MessagesListAdapter
import kotlinx.android.synthetic.main.fragment_chat.*
import java.lang.Exception

class DialogFragment : Fragment() {
    companion object {
        const val ARGS_CONVERSATION = "conversation"
        const val TAG = "DialogFragment"
    }

    private val viewModel: ConversationViewModel by viewModels {
        ConversationViewModelFactory(
            requireArguments().getParcelable<Conversation>(ARGS_CONVERSATION)!!
        )
    }

    private lateinit var viewDataBinding: FragmentDialogBinding
    private lateinit var listAdapter: MessagesListAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding =
            FragmentDialogBinding.inflate(inflater, container, false)
                .apply { viewmodel = viewModel }
            viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListAdapter()
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapterObserver = (object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    recycler_view.scrollToPosition(positionStart)
                }
            })
            listAdapter =
                MessagesListAdapter(viewModel)
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

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopListeners()
        listAdapter.unregisterAdapterDataObserver(listAdapterObserver)
    }
}