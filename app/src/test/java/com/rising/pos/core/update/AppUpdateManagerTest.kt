package com.rising.pos.core.update

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class AppUpdateManagerTest {

    private lateinit var context: Context
    private lateinit var appUpdateManager: AppUpdateManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        appUpdateManager = AppUpdateManager(context)
    }

    @Test
    fun isNewerVersion_newerPatch_returnsTrue() {
        assertThat(appUpdateManager.isNewerVersion("1.0.1", "1.0.0")).isTrue()
        assertThat(appUpdateManager.isNewerVersion("v1.0.1", "1.0.0")).isTrue()
    }

    @Test
    fun isNewerVersion_newerMinor_returnsTrue() {
        assertThat(appUpdateManager.isNewerVersion("1.1.0", "1.0.9")).isTrue()
        assertThat(appUpdateManager.isNewerVersion("v1.2.0", "1.1.5")).isTrue()
    }

    @Test
    fun isNewerVersion_newerMajor_returnsTrue() {
        assertThat(appUpdateManager.isNewerVersion("2.0.0", "1.9.9")).isTrue()
    }

    @Test
    fun isNewerVersion_sameVersion_returnsFalse() {
        assertThat(appUpdateManager.isNewerVersion("1.0.0", "1.0.0")).isFalse()
        assertThat(appUpdateManager.isNewerVersion("v1.0.0", "1.0.0")).isFalse()
    }

    @Test
    fun isNewerVersion_olderVersion_returnsFalse() {
        assertThat(appUpdateManager.isNewerVersion("0.9.9", "1.0.0")).isFalse()
        assertThat(appUpdateManager.isNewerVersion("1.0.0", "1.0.1")).isFalse()
    }
}
