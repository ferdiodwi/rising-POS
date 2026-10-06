package com.rising.pos.feature.settings

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

/** Copy a picked logo into app storage so it remains readable after restart. */
internal fun importStoreLogo(context: Context, uri: Uri): Result<String> = runCatching {
    val limit = 2 * 1024 * 1024
    val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (output.size() <= limit) {
            val count = input.read(buffer, 0, minOf(buffer.size, limit + 1 - output.size()))
            if (count < 0) break
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    } ?: error("Logo tidak dapat dibaca.")
    require(bytes.size <= limit) { "Ukuran logo maksimal 2 MB." }
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    require(options.outMimeType in listOf("image/png", "image/jpeg") && options.outWidth > 0 && options.outHeight > 0) {
        "Pilih gambar PNG atau JPG."
    }
    val directory = File(context.filesDir, "store-logos").apply { mkdirs() }
    File(directory, "${UUID.randomUUID()}.img").apply { writeBytes(bytes) }.absolutePath
}
