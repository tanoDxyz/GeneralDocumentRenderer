package com.tanodxyz.generaldocumentrenderer.pdfRenderer


import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.generaldocumentrenderer.R
import com.tanodxyz.generaldocumentrenderer.ProgressDialog

/**
 * Loads and renders pdf file using android default framework apis.
 */
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

        documentRenderView.setBusyStateIndicator(DefaultCircularProgressBarElement(this))

        pdfLoader.loadFromAssets(assets, "ad.pdf")

        pdfLoader.prepareDocument(true) {
            documentRenderView.setScrollHandler(DefaultScrollHandle(this))
            progressDialog.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfLoader.close()
    }

}