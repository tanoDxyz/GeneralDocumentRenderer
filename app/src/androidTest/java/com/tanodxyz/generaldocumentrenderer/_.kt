package com.tanodxyz.generaldocumentrenderer

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.tanodxyz.documentrenderer.DocumentRenderView
import org.hamcrest.Matcher


class BuzyFree(val buzy:Boolean = true) :
    ViewAction {
    var stateAfterAction:Boolean = false
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(DocumentRenderView::class.java)
    }

    override fun getDescription(): String {
        return "setTheViewStateToBuzyOrFree"
    }

    override fun perform(uiController: UiController?, view: View) {
        val renderView: DocumentRenderView = view as DocumentRenderView
        if(buzy) {
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
    var swipeVertical:Boolean = false
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


class ViewNightModeSwipe(var night:Boolean = true) :
    ViewAction {
    var isNightMode:Boolean = false
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
