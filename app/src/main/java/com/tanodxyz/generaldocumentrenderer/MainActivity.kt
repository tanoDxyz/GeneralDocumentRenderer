package com.tanodxyz.generaldocumentrenderer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tanodxyz.documentrenderer.DocumentRendererView
import com.tanodxyz.documentrenderer.DocumentRenderingSurfaceView
import com.tanodxyz.documentrenderer.document.Document
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val findViewById = findViewById<DocumentRendererView>(R.id.asd)
        findViewById.addDummyPages()
    }
}