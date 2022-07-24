package com.progcorp.unitedmessengers.ui.conversation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.databinding.ActivityConversationBinding
import com.progcorp.unitedmessengers.ui.conversation.bottomsheet.BottomSheetFragment
import com.progcorp.unitedmessengers.ui.conversation.swipecontroller.MessageSwipeController
import com.progcorp.unitedmessengers.interfaces.IMessageSwipeControllerActions
import com.progcorp.unitedmessengers.util.functionalityNotAvailable
import kotlinx.android.synthetic.main.activity_conversation.*
import kotlinx.android.synthetic.main.fragment_telegram.view.*

class ConversationActivity : AppCompatActivity() {
    companion object {
        const val ARGS_CONVERSATION = "conversation"
        const val TAG = "ConversationFragment"
    }

    private val _viewModel: ConversationViewModel by viewModels {
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
            .apply { viewmodel = _viewModel }
        _viewDataBinding?.lifecycleOwner = this
        val view = _viewDataBinding?.root
        _toolbar = view?.toolbar
        setSupportActionBar(_toolbar)
        setContentView(view)
        setupListAdapter()
        setupObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_conversation, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupObservers() {
        _toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
        _toolbar?.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    functionalityNotAvailable(this)
                    true
                }
                R.id.options -> {
                    functionalityNotAvailable(this)
                    true
                }
                else -> false
            }
        }
        _viewModel.addAttachmentPressed.observe(this, EventObserver {
            functionalityNotAvailable(this)
        })
        _viewModel.toBottomPressed.observe(this, EventObserver {
            _viewDataBinding?.recyclerView?.scrollToPosition(0)
        })
        _viewModel.onMessagePressed.observe(this, EventObserver {
            showBottomSheet(_viewModel)
        })
        _viewModel.messageToReply.observe(this, EventObserver {
            _bottomSheet!!.dismiss()
            _bottomSheet = null
            val editText = _viewDataBinding?.messageInput
            editText?.requestFocus()
            val imm: InputMethodManager? = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(editText, 0)
        })
        _viewModel.messagesToForward.observe(this, EventObserver {
            functionalityNotAvailable(this)
        })
        _viewModel.textToCopy.observe(this, EventObserver {
            copyTextToClipboard(it)
        })
        _viewModel.messageToDelete.observe(this, EventObserver {
            _bottomSheet!!.dismiss()
            _bottomSheet = null
            if (it.canBeDeletedForAllUsers && it.canBeDeletedOnlyForSelf) {
                showDeleteWithSelectDialog()
            }
            else if (it.canBeDeletedOnlyForSelf) {
                showDeleteForSelfDialog()
            }
            else {
                showDeleteForAllDialog()
            }
        })
        _viewModel.messageToEdit.observe(this, EventObserver {
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

            if (viewModel.chat.value!!.canWrite) {
                val messagesSwipeController = MessageSwipeController(this, object :
                    IMessageSwipeControllerActions {
                    override fun replyToMessage(position: Int) {
                        viewModel.replyMessage.value = viewModel.messagesList.value!![position]
                    }
                }, _viewDataBinding!!.messageInput)

                val itemTouchHelper = ItemTouchHelper(messagesSwipeController)
                itemTouchHelper.attachToRecyclerView(_viewDataBinding?.recyclerView)
            }
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
        try {
            _bottomSheet!!.dismiss()
            _bottomSheet = null
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Message text", text)
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(applicationContext, R.string.copied_toast, Toast.LENGTH_SHORT).show()
        }
        catch (exception: NullPointerException) {
            Log.e("${this.javaClass.simpleName}.copyTextToClipBoard", "Wrong text: $text")
        }
    }

    private fun showDeleteWithSelectDialog() {
        val items = arrayOf(applicationContext.getString(R.string.delete_for_all))
        val checked = booleanArrayOf(true)
        var deleteForAll = true
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert)
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_message)
            .setMultiChoiceItems(items, checked) { _, _, _->
                deleteForAll = !deleteForAll
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                Log.e(this.javaClass.simpleName, deleteForAll.toString())
                _viewModel.delete(deleteForAll)
                dialog.cancel()
            }
            .show()
    }

    private fun showDeleteForSelfDialog() {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert)
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_message)
            .setMessage(R.string.delete_one_message_confirm)
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                _viewModel.delete(false)
                dialog.cancel()
            }
            .show()
    }

    private fun showDeleteForAllDialog() {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert)
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_message)
            .setMessage(R.string.delete_one_message_for_all_confirmation)
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.yes) { dialog, _ ->
                _viewModel.delete(true)
                dialog.cancel()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.stopListeners()
        _listAdapter?.unregisterAdapterDataObserver(_listAdapterObserver!!)
        _viewDataBinding = null
        _listAdapter = null
        _listAdapterObserver = null
        _toolbar = null
    }
}