package com.tanodxyz.documentrenderer.misc

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window

abstract class DialogHandle(
    protected val windowContext: Context,
    protected val cancelableOnTouchOutside: Boolean = false
) {
    private var dialog: AlertDialog? = null
    private lateinit var dialogView: View

    open fun getContainerLayout(): Int = -1

    open fun getContainerView(): View? = null

    protected fun createDialog() {
        dialogView = getContainerView() ?: LayoutInflater.from(windowContext)
            .inflate(getContainerLayout(), null, true)
        this.dialog = AlertDialog.Builder(windowContext).setView(dialogView).create()
        this.dialog?.apply {
            setCanceledOnTouchOutside(cancelableOnTouchOutside)
            setCancelable(cancelableOnTouchOutside)
            this.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                requestFeature(Window.FEATURE_NO_TITLE)
                setDimAmount(0.75f)
            }
        }
    }

    fun setOnShowListener(callback: androidx.core.util.Consumer<DialogInterface?>) {
        dialog?.setOnShowListener {
            callback.accept(it)
        }
    }

    fun setOnDismissListener(callback: androidx.core.util.Consumer<DialogInterface?>) {
        dialog?.setOnDismissListener { callback.accept(it) }
    }

    open fun show(): Pair<Boolean, Throwable?> {
        val showResult = kotlin.runCatching {
            dialog?.show()
        }
        return Pair(showResult.isSuccess, showResult.exceptionOrNull())
    }

    open fun hide(): Pair<Boolean, Throwable?> {
        val hideResult = kotlin.runCatching {
            dialog?.dismiss()
        }
        return Pair(hideResult.isSuccess, hideResult.exceptionOrNull())
    }

    open fun isShown(): Boolean {
        return dialog?.isShowing ?: false
    }

    fun <T : View> findViewViaId(viewID: Int): T? {
        return dialogView.findViewById(viewID)
    }
} 