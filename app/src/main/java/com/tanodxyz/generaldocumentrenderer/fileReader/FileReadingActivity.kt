package com.tanodxyz.generaldocumentrenderer.fileReader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.generaldocumentrenderer.ProgressDialog
import com.tanodxyz.generaldocumentrenderer.R
import com.tanodxyz.generaldocumentrenderer.SimpleIdlingResource

/**
 * This example shows how to read files from assets or any other path and load the
 * content in the form of pages without creating ANR.
 * the algorithm used is based on line parsing, and you may want to use other technique to read file
 * based on your needs.
 *
 *### text file is created by gpt and it is around 2+ MBs.
 */
class FileReadingActivity : AppCompatActivity(), DocumentRenderView.IdleStateCallback {

    lateinit var simpleIdleResource: SimpleIdlingResource
    private lateinit var fileReader: FileReader
    private lateinit var progressDialog: ProgressDialog
    private lateinit var documentRenderView: DocumentRenderView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_reading)
        init()
    }

    private fun init() {
        simpleIdleResource = SimpleIdlingResource()

        documentRenderView = findViewById(R.id.renderView)

        progressDialog = ProgressDialog(this)
        progressDialog.show()

        fileReader = FileReader(documentRenderView)

        documentRenderView.setBusyStateIndicator(DefaultCircularProgressBarElement(this))

        fileReader.prepareDocument(true) {
            documentRenderView.setScrollHandler(DefaultScrollHandle(this))
            progressDialog.hide()
            documentRenderView.idleStateCallback = this

        }
    }

    override fun renderViewState(idle: Boolean) {
        simpleIdleResource.setIdleState(idle)
    }
}