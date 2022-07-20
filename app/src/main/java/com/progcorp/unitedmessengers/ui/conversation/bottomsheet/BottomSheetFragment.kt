package com.progcorp.unitedmessengers.ui.conversation.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.progcorp.unitedmessengers.databinding.BottomSheetMessageBinding
import com.progcorp.unitedmessengers.ui.conversation.ConversationViewModel

class BottomSheetFragment(val viewModel: ConversationViewModel? = null): BottomSheetDialogFragment() {

    private var _viewDataBinding: BottomSheetMessageBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (viewModel == null) {
            dismiss()
        }
        else {
            _viewDataBinding = BottomSheetMessageBinding.inflate(layoutInflater).apply { viewmodel = viewModel }
            _viewDataBinding!!.lifecycleOwner = this.viewLifecycleOwner
            return _viewDataBinding!!.root
        }
        return null
    }
}