package com.progcorp.unitedmessengers.ui.login.telegram

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.progcorp.unitedmessengers.R

class TelegramAuthFragment : Fragment() {

    companion object {
        fun newInstance() = TelegramAuthFragment()
    }

    private lateinit var viewModel: TelegramAuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_telegram_auth, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TelegramAuthViewModel::class.java)
        // TODO: Use the ViewModel
    }

}