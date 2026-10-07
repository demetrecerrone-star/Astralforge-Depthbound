package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

object ButtonAssetStore {
    private const val PACK = "astralforge_button_pngs.zip"
    private val cache = mutableMapOf<String, Bitmap>()

    @Synchronized
    fun load(context: Context, entryName: String): Bitmap? {
        cache[entryName]?.let { return it }

        return runCatching {
            context.assets.open(PACK).use { raw ->
                ZipInputStream(BufferedInputStream(raw)).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val fileName = entry.name.substringAfterLast('/')
                        if (!entry.isDirectory && fileName == entryName) {
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(16 * 1024)
                            var read = zip.read(buffer)
                            while (read > 0) {
                                output.write(buffer, 0, read)
                                read = zip.read(buffer)
                            }
                            val bytes = output.toByteArray()
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bitmap != null) {
                                cache[entryName] = bitmap
                            }
                            return@runCatching bitmap
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            null
        }.getOrNull()
    }
}
