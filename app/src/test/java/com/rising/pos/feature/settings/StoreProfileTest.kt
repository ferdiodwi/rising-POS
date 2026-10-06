package com.rising.pos.feature.settings

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.model.BusinessType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StoreProfileTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    @Test fun logoIsCopiedAndOversizedOrInvalidImagesAreRejected() {
        val original = File(context.cacheDir, "logo.png")
        // Use a real PNG fixture; legacy Robolectric bitmaps can produce synthetic
        // bytes that cannot pass the importer's actual image decoder.
        original.writeBytes(File("src/test/resources/products/tea.png").readBytes())
        val imported = importStoreLogo(context, Uri.fromFile(original)).getOrThrow()
        assertArrayEquals(original.readBytes(), File(imported).readBytes())
        original.delete()
        assertTrue(File(imported).exists())
        val oversized = File(context.cacheDir, "large.png").apply { writeBytes(ByteArray(2 * 1024 * 1024 + 1)) }
        assertEquals("Ukuran logo maksimal 2 MB.", importStoreLogo(context, Uri.fromFile(oversized)).exceptionOrNull()?.message)
        val invalid = File(context.cacheDir, "invalid.png").apply { writeText("not a PNG") }
        assertTrue(importStoreLogo(context, Uri.fromFile(invalid)).isFailure)
    }
    @Test fun emailAndLogoPersistAndExistingProfileUpdatesPreserveThem() = runBlocking {
        val preferences = AppPreferences(context)
        preferences.updateBusinessProfile("Toko Logo", BusinessType.WARUNG, "08123456789", "Jakarta", "Rp", "Terima kasih", "A01",
            email = "toko@example.com", logoUrl = "/saved/logo.png")
        val saved = preferences.settingsFlow.first()
        assertEquals("toko@example.com", saved.email)
        assertEquals("/saved/logo.png", saved.logoUrl)
        preferences.updateBusinessProfile("Nama Baru", BusinessType.WARUNG, "08123456789", "Jakarta", "Rp", "Terima kasih", "A01")
        val reloaded = AppPreferences(context).settingsFlow.first()
        assertEquals("Nama Baru", reloaded.name)
        assertEquals(saved.email, reloaded.email)
        assertEquals(saved.logoUrl, reloaded.logoUrl)
    }
}
