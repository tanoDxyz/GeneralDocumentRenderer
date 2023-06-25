package com.tanodxyz.generaldocumentrenderer

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.elements.PageElementImpl
import com.tanodxyz.documentrenderer.page.DocumentPage
import org.hamcrest.Matcher
import org.junit.Assert
import java.security.SecureRandom
import kotlin.concurrent.thread


class BuzyFree(val buzy: Boolean = true) :
    ViewAction {
    var stateAfterAction: Boolean = false
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun getDescription(): String {
        return "setTheViewStateToBuzyOrFree"
    }

    override fun perform(uiController: UiController?, view: View) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        if (buzy) {
            renderView.buzy()
            stateAfterAction = !renderView.isFree()
        } else {
            renderView.free()
            stateAfterAction = renderView.isFree()
        }
    }
}


class ChangeSwipeMode() :
    ViewAction {
    var swipeVertical: Boolean = false
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun getDescription(): String {
        return "ifModeIsVerticalChangeToHorizontalViceVersa"
    }

    override fun perform(uiController: UiController?, view: View) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        renderView.changeSwipeMode()
        swipeVertical = renderView.isSwipeVertical()
    }
}


class ViewNightModeSwipe(var night: Boolean = true) :
    ViewAction {
    var isNightMode: Boolean = false
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun getDescription(): String {
        return "changeNightToDayAndViceVersa"
    }

    override fun perform(uiController: UiController?, view: View) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        renderView.nightMode(night)
        isNightMode = renderView.isNightMode()
    }
}

class PageElementImplCoordinatesAction : ViewAction {
    private lateinit var documentPage: DocumentPage
    private lateinit var pageElementImpl: PageElementImpl


    override fun getDescription(): String {
        return "Performs various actions on PageElement"
    }

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        val document = renderView.__getDocument()
        documentPage = document.getPage(0)!!
        pageElementImpl = documentPage.elements[0]
        pageElementImpl.layoutParams.widthSpec = 200
        pageElementImpl.layoutParams.heightSpec = 200
        pageElementImpl.layoutParams.leftMargin = 1F
        pageElementImpl.layoutParams.topMargin = 1F
        renderView.redraw()
    }

    private val TAG = "ElmntDrwnRelativeTest"
    fun pageElementDrawnRelativeToPageCoordinates(): Boolean {
        Log.i(
            TAG,
            "pageElementDrawnRelativeToPageCoordinates: PageBounds = ${documentPage.pageBounds} " +
                    "|||||| elementBoundsRelativeToPage = ${pageElementImpl.elementBoundsRelativeToPage}"
        )
        return pageElementImpl.elementBoundsRelativeToPage.left >= documentPage.pageBounds.left &&
                pageElementImpl.elementBoundsRelativeToPage.top >= documentPage.pageBounds.top
    }
}


class PageElementImplScaleAction : ViewAction {
    private lateinit var documentPage: DocumentPage
    private lateinit var pageElementImpl: PageElementImpl


    override fun getDescription(): String {
        return "Performs Scale Actions on view"
    }

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        val document = renderView.__getDocument()
        documentPage = document.getPage(0)!!
        pageElementImpl = documentPage.elements[0]

        val xBeforeZoom = pageElementImpl.elementBoundsRelativeToPage.left
        val yBeforeZoom = pageElementImpl.elementBoundsRelativeToPage.top
        val xPage = documentPage.pageBounds.left
        val yPage = documentPage.pageBounds.top
        if(xBeforeZoom < xPage || yBeforeZoom < yPage) {
            throw java.lang.Exception("Test failed ->>> pageElement Bounds are not relative to page. ")
        }
        renderView.zoomTo(DocumentRenderView.MAXIMUM_ZOOM)
        renderView.redraw()
        val xAfterZoom = pageElementImpl.elementBoundsRelativeToPage.left
        val yAfterZoom = pageElementImpl.elementBoundsRelativeToPage.top
        val xPageAfterZoom = documentPage.pageBounds.left
        val yPageAfterZoom = documentPage.pageBounds.top
        if(xAfterZoom < xPageAfterZoom || yAfterZoom < yPageAfterZoom) {
            throw java.lang.Exception("Test failed ->>> pageElement Bounds are not relative to page. ")
        }

    }

    private val TAG = "ElmntDrwnRelativeTest"
    fun pageElementDrawnRelativeToPageCoordinates(): Boolean {
        Log.i(
            TAG,
            "pageElementDrawnRelativeToPageCoordinates: PageBounds = ${documentPage.pageBounds} " +
                    "|||||| elementBoundsRelativeToPage = ${pageElementImpl.elementBoundsRelativeToPage}"
        )
        return pageElementImpl.elementBoundsRelativeToPage.left >= documentPage.pageBounds.left &&
                pageElementImpl.elementBoundsRelativeToPage.top >= documentPage.pageBounds.top
    }
}


class JumpToPageTest() :
    ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun getDescription(): String {
        return "jumpToSpecificPage"
    }

    override fun perform(uiController: UiController?, view: View) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        val pageNumber = SecureRandom().nextInt(100) + 10
        renderView.jumpToPage(pageNumber, withAnimation = false)
        Assert.assertEquals(pageNumber, renderView.getCurrentPageIndex())
    }
}


class ScrollTest(val vertical: Boolean = true) :
    ViewAction {
    var startPageBeforeScroll = 0
    var pageAfterScroll = 0
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun getDescription(): String {
        return "vertical/horizontalScrollTest"
    }

    override fun perform(uiController: UiController?, view: View) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        val handler = Handler(Looper.getMainLooper())
        startPageBeforeScroll = renderView.getCurrentPageIndex()
        thread {
            var i = 10
            while (true) {
                i += 300
                var x = renderView.getCurrentX()
                var y = renderView.getCurrentY()
                handler.post {
                    if (vertical) {
                        y -= i
                    } else {
                        x += i
                    }
                    renderView.moveTo(x, y)
                    pageAfterScroll = renderView.getCurrentPageIndex()
                }
                Thread.sleep(1000)
            }
        }
    }
}


class FlingTest() :
    ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun getDescription(): String {
        return "singlePageFlingTest"
    }

    override fun perform(uiController: UiController?, view: View) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        val handler = Handler(Looper.getMainLooper())
        val pageFlingRunnable = {
            renderView.__pageFling(13000F, 2000F)
        }
        renderView.jumpToPage(100)
        renderView.changeSwipeMode()
        thread {
            while (true) {
                Thread.sleep(1500)
                handler.post(pageFlingRunnable)
            }
        }
    }
}

fun sleep(seconds: Int) {
    Thread.sleep(seconds * 1000L)
}