package com.tanodxyz.generaldocumentrenderer.pdfRenderer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.misc.DialogHandle
import com.tanodxyz.generaldocumentrenderer.R

class PdfViewActivity : AppCompatActivity() {
    private lateinit var pdfLoader: PdfLoader
    private lateinit var progressDialog: ProgressDialog
    private lateinit var documentRenderView: DocumentRenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        init()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        documentRenderView = findViewById(R.id.documentRenderView)
        progressDialog = ProgressDialog(this)
        progressDialog.show()
        pdfLoader = PdfLoader(documentRenderView)
        pdfLoader.loadFromAssets(assets,"example.pdf")
        pdfLoader.prepareDocument(true) {
            progressDialog.hide()
        }
    }

    class ProgressDialog(windowContext: Context): DialogHandle(windowContext) {
        init {
            createDialog()
        }
        override fun getContainerLayout(): Int {
            return R.layout.progress_dialog
        }
    }
    companion object {
        fun launchActivity(activity: Activity) {
            Intent(activity,PdfViewActivity::class.java).apply {
                activity.startActivity(this)
            }
        }
    }
}