package com.tanodxyz.generaldocumentrenderer.pagesizecalculator

import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.documentrenderer.pagesizecalculator.DefaultPageSizeCalculator
import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DefaultPageSizeCalculatorTest {
    private lateinit var defaultPageSizeCalculator: DefaultPageSizeCalculator
    @Before
    fun setup() {
        defaultPageSizeCalculator = DefaultPageSizeCalculator()
    }


    @Test
    fun setupCalculatorWithExtraUnknownArgs__shouldNotAffectCalculator() {
        hashMapOf<String, Any>().apply {
            put(PageSizeCalculator.VIEW_SIZE, Size(0, 0))
            put(PageSizeCalculator.FIT_POLICY,Document.PageFitPolicy.FIT_HEIGHT)
            put(PageSizeCalculator.MAX_HEIGHT_PAGE_SIZE,Size(23,23))
            put(PageSizeCalculator.MAX_WIDTH_PAGE_SIZE,Size(23,23))
            put(PageSizeCalculator.FIT_EACH_PAGE,true)
            put("unknownKey", Int.MAX_VALUE)
            defaultPageSizeCalculator.setup(this)
        }
    }

    @Test
    fun ifSetupCalculator___withNoPageFitPolicyAndFitEachPage__should__use__Default_values() {
        hashMapOf<String, Any>().apply {
            put(PageSizeCalculator.VIEW_SIZE, Size(0, 0))
            put(PageSizeCalculator.MAX_HEIGHT_PAGE_SIZE,Size(23,23))
            put(PageSizeCalculator.MAX_WIDTH_PAGE_SIZE,Size(23,23))
            put("unknownKey", Int.MAX_VALUE)
            defaultPageSizeCalculator.setup(this)
        }
    }

    @Test
    fun fit__each__page____should___not__exceed__view_width_height() {
        hashMapOf<String, Any>().apply {
            put(PageSizeCalculator.VIEW_SIZE, Size(1200, 1700))
            put(PageSizeCalculator.FIT_EACH_PAGE,true)
            put(PageSizeCalculator.MAX_HEIGHT_PAGE_SIZE,Size(2300,15000))
            put(PageSizeCalculator.MAX_WIDTH_PAGE_SIZE,Size(2900,1300000))
            put("unknownKey", Int.MAX_VALUE)
            defaultPageSizeCalculator.setup(this)

            val calculatedSize = defaultPageSizeCalculator.calculate(Size(100000, 100000))
            assert(calculatedSize.width <= 1200)
            assert(calculatedSize.height <= 1700)
        }
    }
}