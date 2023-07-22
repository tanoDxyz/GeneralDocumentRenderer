package com.tanodxyz.generaldocumentrenderer.fileReader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.generaldocumentrenderer.ProgressDialog
import com.tanodxyz.generaldocumentrenderer.R
import com.tanodxyz.generaldocumentrenderer.photoslider.PhotoLoader

class FileReadingActivity : AppCompatActivity() {

    private lateinit var fileReader: FileReader
    private lateinit var progressDialog: ProgressDialog
    private lateinit var documentRenderView: DocumentRenderView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_reading)
        init()
    }

    private fun init() {
        documentRenderView = findViewById(R.id.documentRenderView)

        progressDialog = ProgressDialog(this)
        progressDialog.show()
        fileReader = FileReader(documentRenderView)
        documentRenderView.setBusyStateIndicator(DefaultCircularProgressBarElement(this))
        fileReader.prepareDocument(true) {
            documentRenderView.setScrollHandler(DefaultScrollHandle(this))
            progressDialog.hide()
        }
    }

}