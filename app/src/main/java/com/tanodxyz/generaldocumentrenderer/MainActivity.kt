package com.tanodxyz.generaldocumentrenderer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tanodxyz.documentrenderer.DocumentRenderingView
import com.tanodxyz.documentrenderer.document.Document

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val document = Document()
        document[Document.DOCUMENT_NAME] = 23
        document.getProperty<String>("alksdjfl")
    }
}