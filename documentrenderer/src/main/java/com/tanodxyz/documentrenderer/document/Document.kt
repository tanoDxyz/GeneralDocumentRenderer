package com.tanodxyz.documentrenderer.document

import android.util.SparseArray
import com.tanodxyz.documentrenderer.page.DocumentPage
import org.json.JSONArray
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class Document {
    private val documentMeta = HashMap<String, Any?>()
    private val documentPageData = mutableListOf<DocumentPage>()
    
    fun <T> getProperty(property: String): T? {
        val propertyValue = documentMeta[property]
        return if (propertyValue != null) propertyValue as T else null
    }


    companion object {
        val PROPERTY_DOOCUMENT_NAME = "com.gdr.documentName"
        val PROPERTY_DOOCUMENT_ROTATAION = "com.gdr.documentRotation"
        val PROPERTY_DOOCUMENT_SCROLL_STRATEGY = "com.gdr.documentScrollStrategy"
        val PROPERTY_DOOCUMENT_PAGE_FIT_POLICY = "com.gdr.documentPageFitPolicy"
        val PROPERTY_DOOCUMENT_VIEW_MODE = "com.gdr.documentViewMode"
    }


    enum class DocumentScrollStrategy {
        HORIZONTAL, VERTICAL, CUSTOM;

        fun strategy(name: String) {
            if (name.equals(HORIZONTAL.name, true)) {
                HORIZONTAL
            } else if (name.equals(VERTICAL.name, true)) {
                VERTICAL
            } else {
                CUSTOM
            }
        }
    }

    enum class PAGE_FIT_POLICY {
        FIT_WIDTH, FIT_PAGE , NONE;
        fun policy(name:String) {
            if (name.equals(FIT_WIDTH.name, true)) {
                FIT_WIDTH
            } else if (name.equals(FIT_PAGE.name, true)) {
                FIT_PAGE
            } else {
                NONE
            }
        }
    }

    enum class DocumentViewMode {
        DAY, NIGHT;
        fun mode(name:String) {
            if(name.equals(DAY.name,true)) {
                DAY
            } else if(name.equals(NIGHT.name,true)) {
                NIGHT
            } else {
                DAY
            }
        }
    }
}