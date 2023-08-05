package com.tanodxyz.generaldocumentrenderer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import com.tanodxyz.documentrenderer.*
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.DefaultCircularProgressBarElement

import com.tanodxyz.documentrenderer.extensions.DefaultScrollHandle
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.generaldocumentrenderer.canvas.SimpleDrawingSurfaceActivity
import com.tanodxyz.generaldocumentrenderer.fileReader.FileReadingActivity
import com.tanodxyz.generaldocumentrenderer.pdfRenderer.PdfViewActivity
import com.tanodxyz.generaldocumentrenderer.photoslider.PhotoSliderActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        supportActionBar?.title = getString(R.string.examples)
        findViewById<View>(R.id.pdfReaderButton).setOnClickListener {
            launch<PdfViewActivity>()
        }
        findViewById<View>(R.id.fileReaderButton).setOnClickListener {
            launch<FileReadingActivity>()
        }
        findViewById<View>(R.id.photoSlideShowButton).setOnClickListener {
            launch<PhotoSliderActivity>()
        }

        findViewById<View>(R.id.canvasButton).setOnClickListener {
            launch<SimpleDrawingSurfaceActivity>()
        }

        findViewById<View>(R.id.customElement).setOnClickListener {
            launch<CustomElementActivity>()
        }

        findViewById<View>(R.id.customElementInteractive).setOnClickListener {
            launch<CustomElementInteractiveActivity>()
        }
    }


    companion object {
         inline fun <reified T:AppCompatActivity> AppCompatActivity.launch() {
            Intent(this,T::class.java).apply {
                this@launch.startActivity(this)
            }
        }
    }
}
