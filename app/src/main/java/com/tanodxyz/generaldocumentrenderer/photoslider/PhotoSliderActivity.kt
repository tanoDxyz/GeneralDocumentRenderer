package com.tanodxyz.generaldocumentrenderer.photoslider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement
import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.generaldocumentrenderer.ProgressDialog
import com.tanodxyz.generaldocumentrenderer.R

/**
 * Creates a simple slide show from images in assets using pageFling feature that library provides.
 */
class PhotoSliderActivity : AppCompatActivity() {
    private lateinit var photoLoader: PhotoLoader
    private lateinit var progressDialog: ProgressDialog
    private lateinit var documentRenderView: DocumentRenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_slider)
        init()
    }

    private fun init() {
        documentRenderView = findViewById(R.id.documentRenderView)

        progressDialog = ProgressDialog(this)
        progressDialog.show()

        photoLoader = PhotoLoader(documentRenderView)

        documentRenderView.setBusyStateIndicator(DefaultCircularProgressBarElement(this))
        photoLoader.prepareDocument(true) {
            documentRenderView.setScrollHandler(DefaultScrollHandle(this))
            progressDialog.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        photoLoader.close()
    }
}