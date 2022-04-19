package com.progcorp.unitedmessengers.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.Message
import com.progcorp.unitedmessenges.databinding.FragmentDialogBinding
import com.progcorp.unitedmessenges.databinding.ToolbarAddonDialogBinding
import kotlinx.android.synthetic.main.fragment_chat.*
import java.lang.Exception

class DialogFragment : Fragment() {

    companion object {
        const val ARGS_CONVERSATION = "conversation"
        const val TAG = "DialogFragment"
    }

    private val viewModel: DialogViewModel by viewModels {
        DialogViewModelFactory(
            requireArguments().getParcelable<Conversation>(ARGS_CONVERSATION)!!
        )
    }

    private lateinit var viewDataBinding: FragmentDialogBinding
    private lateinit var listAdapter: MessagesListAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver
    private lateinit var toolbarAddonDialogBinding: ToolbarAddonDialogBinding

    override fun onDestroy() {
        super.onDestroy()
        removeCustomToolbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding =
            FragmentDialogBinding.inflate(inflater, container, false)
                .apply { viewmodel = viewmodel }
            viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setHasOptionsMenu(true)

        toolbarAddonDialogBinding =
            ToolbarAddonDialogBinding.inflate(inflater, container, false)
                .apply { viewmodel = viewmodel }
        toolbarAddonDialogBinding.lifecycleOwner = this.viewLifecycleOwner

        return viewDataBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCustomToolbar()
        setupListAdapter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupCustomToolbar() {
        val supportActionBar = (activity as AppCompatActivity?)!!.supportActionBar
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar.customView = toolbarAddonDialogBinding.root
    }

    private fun removeCustomToolbar() {
        val supportActionBar = (activity as AppCompatActivity?)!!.supportActionBar
        supportActionBar!!.setDisplayShowCustomEnabled(false)
        supportActionBar.customView = null
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
                MessagesListAdapter(viewModel, requireArguments().getParcelableArrayList<Message>("")!!)
            listAdapter.registerAdapterDataObserver(listAdapterObserver)
            viewDataBinding.recyclerView.adapter = listAdapter
        }
        else {
            throw Exception("The viewmodel is not initialized")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter.unregisterAdapterDataObserver(listAdapterObserver)
    }

    //private fun initRecyclerView() {
    //    recyclerView!!.adapter = ChatAdapter(arrayListOf())
    //    recyclerView!!.setHasFixedSize(true)
    //
    //    recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    //        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    //            if (!recyclerView.canScrollVertically(-1)) {
    //                presenter!!.addMessages()
    //            }
    //            super.onScrolled(recyclerView, dx, dy)
    //        }
    //    })
    //}
}