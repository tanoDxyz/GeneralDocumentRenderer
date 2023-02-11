package com.tanodxyz.generaldocumentrenderer.pagesizecalculator

import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.pagesizecalculator.FixPageSizeCalculator

import com.tanodxyz.documentrenderer.pagesizecalculator.PageSizeCalculator.Companion.VIEW_SIZE
import org.junit.Before
import org.junit.Test


internal class FixPageSizeCalculatorTest {

    private lateinit var fixPageSizeCalculator: FixPageSizeCalculator
    @Before
    fun setup() {
        fixPageSizeCalculator = FixPageSizeCalculator()
    }

    @Test
    fun setupCalculatorWithExtraUnknownArgs__shouldNotAffectCalculator() {
        hashMapOf<String, Any>().apply {
            put(VIEW_SIZE, Size(0, 0))
            put("unknownKey", Int.MAX_VALUE)
            fixPageSizeCalculator.setup(this)
        }
    }

    @Test
    fun settingViewSizeViaSetup___shouldBeReflectedInCalculator() {
        hashMapOf<String, Any>().apply {
            put(VIEW_SIZE, Size(720, 1280))
            put("unknownKey", Int.MAX_VALUE)
            fixPageSizeCalculator.setup(this)
            assert(fixPageSizeCalculator.viewSize.width == 720)
            assert(fixPageSizeCalculator.viewSize.height == 1280)
        }
    }

    @Test
    fun calculatedPageSizedBasedOnViewSize___shouldNotExceedMaxSize() {
        hashMapOf<String, Any>().apply {
            put(VIEW_SIZE, Size(720, 1280))
            put("unknownKey", Int.MAX_VALUE)
            fixPageSizeCalculator.setup(this)
            assert(fixPageSizeCalculator.viewSize.width == 720)
            assert(fixPageSizeCalculator.viewSize.height == 1280)
            val calculatedPageSize = fixPageSizeCalculator.calculate(
                Size(1300, 14000)
            )
            assert(calculatedPageSize.width <= 720)
            assert(calculatedPageSize.height <= 1280)
        }
    }
}