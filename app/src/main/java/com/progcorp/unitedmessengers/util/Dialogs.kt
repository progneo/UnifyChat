package com.progcorp.unitedmessengers.util

import android.content.Context
import com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.progcorp.unitedmessengers.R

fun functionalityNotAvailable(context: Context) {
    MaterialAlertDialogBuilder(context, ThemeOverlay_Material3_Dialog_Alert)
        .setIcon(R.drawable.ic_developer_mode)
        .setTitle(R.string.unavailable)
        .setMessage(R.string.in_develop)
        .setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.cancel()
        }
        .show()
}