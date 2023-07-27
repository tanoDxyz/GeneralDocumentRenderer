package com.tanodxyz.generaldocumentrenderer

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.tanodxyz.generaldocumentrenderer.fileReader.FileReadingActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert


class DocumentRenderViewTest {
    private lateinit var activityScenario: ActivityScenario<FileReadingActivity>
    private lateinit var activity: FileReadingActivity
    private lateinit var idlingResource: SimpleIdlingResource

    @Before
    fun setupIdlingResource() {
        activityScenario = ActivityScenario.launch(
            FileReadingActivity::class.java
        )
        activityScenario.onActivity {
            activity = it
            idlingResource = it.simpleIdleResource
            IdlingRegistry.getInstance().register(idlingResource)
        }
    }


    @Test
    fun jumpTo___pageTest() {
        onView(withId(R.id.renderView)).perform(JumpToPageTest())
    }

    @Test
    fun change___Swipe__mode() {
        sleep(5)
        val changeSwipeMode = ChangeSwipeMode()
        onView(withId(R.id.renderView)).perform(changeSwipeMode)
        // by default view is launched into vertical mode
        Assert.assertEquals(false, changeSwipeMode.swipeVertical)
        sleep(5)
        onView(withId(R.id.renderView)).perform(changeSwipeMode)
        Assert.assertEquals(true, changeSwipeMode.swipeVertical)
        sleep(5)
    }


    @Test
    fun nightMode__swap() {
        sleep(5)
        // by default mode is day
        val viewNightModeSwipe = ViewNightModeSwipe(night = true)
        onView(withId(R.id.renderView)).perform(viewNightModeSwipe)
        Assert.assertEquals(true, viewNightModeSwipe.isNightMode)
        sleep(5)
        viewNightModeSwipe.night = false
        onView(withId(R.id.renderView)).perform(viewNightModeSwipe)
        Assert.assertEquals(true, !viewNightModeSwipe.isNightMode)
        sleep(5)
    }

    @Test
    fun buzy__action() {
        sleep(3)
        val buzyAction = BusyFree(buzy = true)
        onView(withId(R.id.renderView)).perform(buzyAction)
        Assert.assertEquals(true, buzyAction.stateAfterAction)
        sleep(2)
    }


    @Test
    fun scroll____test() {
        sleep(2)
        val scrollTest = ScrollTest(vertical = true)
        onView(withId(R.id.renderView)).perform(scrollTest)
        sleep(10)
        assert(scrollTest.pageAfterScroll != scrollTest.startPageBeforeScroll)
        onView(withId(R.id.renderView)).perform(ChangeSwipeMode())
        sleep(3)
        val scrollTest1 = ScrollTest(vertical = false)
        onView(withId(R.id.renderView)).perform(scrollTest1)
        sleep(10)
        assert(scrollTest1.pageAfterScroll != scrollTest1.startPageBeforeScroll)
    }

    @Test
    fun pageFlingTest() {
        sleep(2)
        onView(withId(R.id.renderView)).perform(FlingTest())
        sleep(15)

    }
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }


}