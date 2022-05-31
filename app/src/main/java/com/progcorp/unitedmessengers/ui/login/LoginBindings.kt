package com.progcorp.unitedmessengers.ui.login

import android.view.View
import androidx.databinding.BindingAdapter
import com.progcorp.unitedmessengers.ui.login.telegram.TelegramAuthViewModel

@BindingAdapter("bind_viewModel_layouyState", "bind_login_elements_visibility")
fun View.bindElementsVisibility(
    currentState: TelegramAuthViewModel.LayoutState, layoutState: TelegramAuthViewModel.LayoutState
) {
    if (currentState == layoutState) {
        this.visibility = View.VISIBLE
    }
    else {
        this.visibility = View.INVISIBLE
    }
}