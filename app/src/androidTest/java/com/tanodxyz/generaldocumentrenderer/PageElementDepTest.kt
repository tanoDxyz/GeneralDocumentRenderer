package com.tanodxyz.generaldocumentrenderer

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.ViewMatchers
import com.tanodxyz.generaldocumentrenderer.fileReader.FileReadingActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before

import org.junit.Test

class PageElementDepTest {
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
            idlingResource = it.simpleIdleResource!!
            IdlingRegistry.getInstance().register(idlingResource)
        }
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun pageElement__Coordinates__should__be__relative__to__page() {
        sleep(5)
        val pageElementCoordinatesAction = PageElementDepCoordinatesAction()
        Espresso.onView(ViewMatchers.withId(R.id.documentRenderView)).perform(pageElementCoordinatesAction)
        sleep(5)
        Assert.assertEquals(true, pageElementCoordinatesAction.pageElementDrawnRelativeToPageCoordinates())
        sleep(5)
    }

    @Test
    fun pageElement__zoomInZoomOut___relative__to__pageSize() {
        sleep(5)
        val pageElementScaleAction = PageElementDepScaleAction()
        Espresso.onView(ViewMatchers.withId(R.id.documentRenderView)).perform(pageElementScaleAction)
        sleep(5)
    }
}