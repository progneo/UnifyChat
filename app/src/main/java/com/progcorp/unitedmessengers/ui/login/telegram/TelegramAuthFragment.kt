package com.progcorp.unitedmessengers.ui.login.telegram

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.progcorp.unitedmessengers.data.EventObserver
import com.progcorp.unitedmessengers.databinding.FragmentTelegramAuthBinding

class TelegramAuthFragment : Fragment() {

    private val viewModel: TelegramAuthViewModel by viewModels { TelegramAuthViewModelFactory() }

    private lateinit var viewDataBinding: FragmentTelegramAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding =
            FragmentTelegramAuthBinding.inflate(inflater, container, false).apply { viewmodel = viewModel }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.restartEvent.observe(viewLifecycleOwner, EventObserver { restartActivity() })
    }

    private fun restartActivity() {
        Handler().post {
            val intent = requireActivity().intent
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
            requireActivity().overridePendingTransition(0, 0)
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }
}