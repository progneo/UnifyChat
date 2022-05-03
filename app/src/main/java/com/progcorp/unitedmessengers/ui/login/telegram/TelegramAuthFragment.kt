package com.progcorp.unitedmessengers.ui.login.telegram

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.progcorp.unitedmessengers.databinding.FragmentTelegramAuthBinding

class TelegramAuthFragment : Fragment() {

    private val viewModel: TelegramAuthViewModel by viewModels { TelegramAuthViewModelFactory() }

    private lateinit var viewDataBinding: FragmentTelegramAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding =
            FragmentTelegramAuthBinding.inflate(inflater, container, false).apply { viewmodel = viewModel }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }
}