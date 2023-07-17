package com.tanodxyz.generaldocumentrenderer

import android.content.Context
import com.tanodxyz.documentrenderer.misc.DialogHandle

class ProgressDialog(windowContext: Context) : DialogHandle(windowContext) {
        init {
            createDialog()
        }

        override fun getContainerLayout(): Int {
            return R.layout.progress_dialog
        }
    }