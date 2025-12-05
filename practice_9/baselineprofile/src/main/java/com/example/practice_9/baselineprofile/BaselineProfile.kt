package com.example.practice_9.baselineprofile

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.benchmark.macro.junit4.collectBaselineProfile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfile {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @OptIn(ExperimentalBaselineProfilesApi::class)
    @Test
    fun generate() = baselineRule.collectBaselineProfile(
        packageName = "com.example.practice_9"
    ) {
        // Launch the main activity and wait for idle
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        // Open Profile screen
        device.wait(Until.hasObject(By.text("Open Profile")), 2_000)
        device.findObject(By.text("Open Profile"))?.click()
        device.waitForIdle()

        // Tap edit then return
        device.wait(Until.hasObject(By.text("Edit profile")), 2_000)
        device.findObject(By.text("Edit profile"))?.click()
        device.waitForIdle()
        device.pressBack()

        // Open Feeds screen
        device.wait(Until.hasObject(By.text("Open Feeds")), 2_000)
        device.findObject(By.text("Open Feeds"))?.click()
        device.waitForIdle()
    }
}

