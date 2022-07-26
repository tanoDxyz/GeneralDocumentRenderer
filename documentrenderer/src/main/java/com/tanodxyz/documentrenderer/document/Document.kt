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

    fun <T> get(property: String): T? {
        val propertyValue = documentMeta[property]
        return if (propertyValue != null) propertyValue as T else null
    }

    operator fun set(property: String, value: Any?) {
        documentMeta[property] = value
    }

    var documentName: String?
        get() {
            return get<String>(PROPERTY_DOOCUMENT_NAME)
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_NAME] = value
        }

    var documentRotation: DocumentRotation
        get() {
            val str = get<String>(PROPERTY_DOOCUMENT_ROTATAION)
            return DocumentRotation.rotation(str)
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_ROTATAION] = value
        }

    var documentScrollStrategy: DocumentScrollStrategy
        get() {
            val str = get<String>(PROPERTY_DOOCUMENT_SCROLL_STRATEGY)
            return DocumentScrollStrategy.strategy(str)
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_SCROLL_STRATEGY] = value
        }

    var documentFitPagePolicly: PAGE_FIT_POLICY
        get() {
            val str = get<String>(PROPERTY_DOOCUMENT_PAGE_FIT_POLICY)
            return PAGE_FIT_POLICY.policy(str)
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_PAGE_FIT_POLICY] = value
        }

    var documentViewMode: DocumentViewMode
        get() {
            val str = get<String>(PROPERTY_DOOCUMENT_VIEW_MODE)
            return DocumentViewMode.mode(str)
        }
        set(value) {
            this[PROPERTY_DOOCUMENT_VIEW_MODE] = value
        }

    var documentPath: String?
        get() {
            return get<String>(PROPERTY_DOCUMENT_PATH)
        }
        set(value) {
            this[PROPERTY_DOCUMENT_PATH] = value
        }

    companion object {
        val PROPERTY_DOOCUMENT_NAME = "com.gdr.documentName"
        val PROPERTY_DOOCUMENT_ROTATAION = "com.gdr.documentRotation"
        val PROPERTY_DOOCUMENT_SCROLL_STRATEGY = "com.gdr.documentScrollStrategy"
        val PROPERTY_DOOCUMENT_PAGE_FIT_POLICY = "com.gdr.documentPageFitPolicy"
        val PROPERTY_DOOCUMENT_VIEW_MODE = "com.gdr.documentViewMode"
        val PROPERTY_DOCUMENT_PATH = "com.gdr.documentPath"
    }


    enum class DocumentRotation {
        PORTRAIT, LANDSCAPE;

        companion object {
            fun rotation(name: String?): DocumentRotation {
                return if (name.equals(LANDSCAPE.name, true)) {
                    LANDSCAPE
                } else if (name.equals(PORTRAIT.name, true)) {
                    PORTRAIT
                } else {
                    PORTRAIT
                }
            }
        }

    }

    enum class DocumentScrollStrategy {
        HORIZONTAL, VERTICAL, CUSTOM;

        companion object {
            fun strategy(name: String?): DocumentScrollStrategy {
                return if (name.equals(HORIZONTAL.name, true)) {
                    HORIZONTAL
                } else if (name.equals(VERTICAL.name, true)) {
                    VERTICAL
                } else {
                    CUSTOM
                }
            }
        }
    }

    enum class PAGE_FIT_POLICY {
        FIT_WIDTH, FIT_PAGE, NONE;

        companion object {
            fun policy(name: String?): PAGE_FIT_POLICY {
                return if (name.equals(FIT_WIDTH.name, true)) {
                    FIT_WIDTH
                } else if (name.equals(FIT_PAGE.name, true)) {
                    FIT_PAGE
                } else {
                    NONE
                }
            }
        }
    }

    enum class DocumentViewMode {
        DAY, NIGHT;

        companion object {
            fun mode(name: String?): DocumentViewMode {
                return if (name.equals(DAY.name, true)) {
                    DAY
                } else if (name.equals(NIGHT.name, true)) {
                    NIGHT
                } else {
                    DAY
                }
            }
        }
    }
}