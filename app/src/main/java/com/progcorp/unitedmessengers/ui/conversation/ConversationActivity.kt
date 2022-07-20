package com.progcorp.unitedmessengers.ui.conversation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.ActivityConversationBinding
import com.progcorp.unitedmessengers.ui.conversation.bottomsheet.BottomSheetFragment
import com.progcorp.unitedmessengers.util.functionalityNotAvailable
import kotlinx.android.synthetic.main.activity_conversation.*
import kotlinx.android.synthetic.main.fragment_telegram.view.*

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

    private var _viewDataBinding: ActivityConversationBinding? = null
    private var _listAdapter: MessagesListAdapter? = null
    private var _listAdapterObserver: RecyclerView.AdapterDataObserver? = null
    private var _toolbar: MaterialToolbar? = null
    private var _bottomSheet: BottomSheetFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewDataBinding = ActivityConversationBinding.inflate(layoutInflater)
            .apply { viewmodel = viewModel }
        _viewDataBinding?.lifecycleOwner = this
        val view = _viewDataBinding?.root
        _toolbar = view?.toolbar
        setSupportActionBar(_toolbar)
        setContentView(view)
        setupListAdapter()
        setupObservers()
    }

    private fun setupObservers() {
        _toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        viewModel.addAttachmentPressed.observe(this, EventObserver {
            functionalityNotAvailable(this)
        })
        viewModel.toBottomPressed.observe(this, EventObserver {
            _viewDataBinding?.recyclerView?.scrollToPosition(0)
        })
        viewModel.onMessagePressed.observe(this, EventObserver {
            showBottomSheet(viewModel)
        })
        viewModel.messageToReply.observe(this, EventObserver {
            _bottomSheet!!.dismiss()
            _bottomSheet = null
        })
        viewModel.messagesToForward.observe(this, EventObserver {
            functionalityNotAvailable(this)
        })
        viewModel.textToCopy.observe(this, EventObserver {
            copyTextToClipboard(it)
        })
        viewModel.messageToDelete.observe(this, EventObserver {
            functionalityNotAvailable(this)
        })
        viewModel.messageToEdit.observe(this, EventObserver {
            functionalityNotAvailable(this)
        })
    }

    private fun setupListAdapter() {
        val viewModel = _viewDataBinding?.viewmodel
        if (viewModel != null) {
            _listAdapterObserver = (object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        recycler_view.scrollToPosition(positionStart)
                    }
                }
            })
            _listAdapter = MessagesListAdapter(viewModel)
            _listAdapter?.registerAdapterDataObserver(_listAdapterObserver!!)
            _viewDataBinding?.recyclerView?.adapter = _listAdapter

            _viewDataBinding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(-1)) {
                        viewModel.loadMoreMessages()
                    }

                    if (!recyclerView.canScrollVertically(1)) {
                        if (_viewDataBinding?.floatButton!!.isShown) {
                            _viewDataBinding?.floatButton!!.hide()
                        }
                    }

                    if (dy > 0) {
                        if (!_viewDataBinding?.floatButton!!.isShown) {
                            _viewDataBinding?.floatButton!!.show()
                        }
                    }
                    else if (dy < 0) {
                        if (_viewDataBinding?.floatButton!!.isShown) {
                            _viewDataBinding?.floatButton!!.hide()
                        }
                    }
                }
            })
        }
        else {
            throw Exception("The viewmodel is not initialized")
        }
    }

    private fun showBottomSheet(viewModel: ConversationViewModel) {
        _bottomSheet = BottomSheetFragment(viewModel)
        _bottomSheet!!.show(supportFragmentManager, "Message bottom sheet")
    }

    private fun copyTextToClipboard(text: String) {
        _bottomSheet!!.dismiss()
        _bottomSheet = null
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Message text", text)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(applicationContext, R.string.copied_toast, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopListeners()
        _listAdapter?.unregisterAdapterDataObserver(_listAdapterObserver!!)
        _viewDataBinding = null
        _listAdapter = null
        _listAdapterObserver = null
        _toolbar = null
    }
}