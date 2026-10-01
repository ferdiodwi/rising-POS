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

    @Test
    fun extractMinVersionCode_variousFormats_parsedCorrectly() {
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nmin_version_code: 5")).isEqualTo(5)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nmin_version_code:5")).isEqualTo(5)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nmin_version_code = 5")).isEqualTo(5)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nmin version code: 5")).isEqualTo(5)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nmin_code: 5")).isEqualTo(5)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nmin_version_code 5")).isEqualTo(5)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nMIN_VERSION_CODE: 10")).isEqualTo(10)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru\nmin-version-code: 3")).isEqualTo(3)
        assertThat(appUpdateManager.extractMinVersionCode("Fitur baru tanpa directive")).isNull()
    }

    @Test
    fun extractMinVersionName_variousFormats_parsedCorrectly() {
        assertThat(appUpdateManager.extractMinVersionName("min_version: 1.2.0")).isEqualTo("1.2.0")
        assertThat(appUpdateManager.extractMinVersionName("MIN_VERSION = v2.0.0")).isEqualTo("v2.0.0")
        assertThat(appUpdateManager.extractMinVersionName("min_version_code: 5")).isNull()
    }

    @Test
    fun cleanReleaseNotes_removesDirectivesCleanly() {
        val notes = """
            Perbaikan bug kasir
            min_version_code: 5
            Peningkatan performa cetak
        """.trimIndent()

        val cleaned = appUpdateManager.cleanReleaseNotes(notes)
        assertThat(cleaned).doesNotContain("min_version_code")
        assertThat(cleaned).contains("Perbaikan bug kasir")
        assertThat(cleaned).contains("Peningkatan performa cetak")
    }
}
