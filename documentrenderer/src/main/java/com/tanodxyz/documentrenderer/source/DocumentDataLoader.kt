package com.tanodxyz.documentrenderer.source

import android.util.JsonReader
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage

interface DocumentDataLoader {
    fun loadDocumentMeta(): HashMap<String, String>
    fun loadDocument(withPages: Boolean = false): Document
    fun loadPage(position: Int): DocumentPage
    fun loadNextPage(): DocumentPage
    fun loadPreviousPage(): DocumentPage
    fun close()
}