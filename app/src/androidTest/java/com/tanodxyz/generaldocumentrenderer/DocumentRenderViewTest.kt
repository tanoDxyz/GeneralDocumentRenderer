package com.tanodxyz.generaldocumentrenderer

import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate.NightMode
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert
import java.security.SecureRandom


class DocumentRenderViewTest {
    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var activity: MainActivity
    private lateinit var idlingResource: SimpleIdlingResource

    @Before
    fun setupIdlingResource() {
        activityScenario = ActivityScenario.launch(
            MainActivity::class.java
        )
        activityScenario.onActivity {
            activity = it
            idlingResource = it.simpleIdleResource!!
            IdlingRegistry.getInstance().register(idlingResource)
        }
    }


    @Test
    fun jumpTo___pageTest() {
        val pageNumber = SecureRandom().nextInt(100) + 10
        activity.renderView.apply {
            jumpToPage(pageNumber, withAnimation = false)
            Thread.sleep(3000)
            Assert.assertEquals(pageNumber, getCurrentPageIndex())
        }
    }

    @Test
    fun change___Swipe__mode() {
        Thread.sleep(5_000)
        val changeSwipeMode = ChangeSwipeMode()
        onView(withId(R.id.documentRenderView)).perform(changeSwipeMode)
        // by default view is launched into vertical mode
        Assert.assertEquals(false, changeSwipeMode.swipeVertical)
        Thread.sleep(5_000)
        onView(withId(R.id.documentRenderView)).perform(changeSwipeMode)
        Assert.assertEquals(true, changeSwipeMode.swipeVertical)
        Thread.sleep(5_000)
    }


    @Test
    fun nightMode__swap() {
        Thread.sleep(5_000)
        // by default mode is day
        val viewNightModeSwipe = ViewNightModeSwipe(night = true)
        onView(withId(R.id.documentRenderView)).perform(viewNightModeSwipe)
        Assert.assertEquals(true,viewNightModeSwipe.isNightMode)
        Thread.sleep(5000)
        viewNightModeSwipe.night = false
        onView(withId(R.id.documentRenderView)).perform(viewNightModeSwipe)
        Assert.assertEquals(true,!viewNightModeSwipe.isNightMode)
        Thread.sleep(5000)
    }

    @Test
    fun buzy__action() {
        Thread.sleep(3_000)
        val buzyAction = BuzyFree(buzy = true)
        onView(withId(R.id.documentRenderView)).perform(buzyAction)
        Assert.assertEquals(true, buzyAction.stateAfterAction)
        Thread.sleep(2_000)
    }


    @Test fun scroll____test() {
        Thread.sleep(5_000)
        activity.renderView.apply {

        }
    }
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }


}