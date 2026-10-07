package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

object ButtonAssetStore {
    private const val DEFAULT_PACK = "astralforge_button_pngs.zip"
    private val cache = mutableMapOf<String, Bitmap>()

    @Synchronized
    fun load(context: Context, entryName: String): Bitmap? =
        load(context, DEFAULT_PACK, entryName)

    @Synchronized
    fun load(context: Context, packName: String, entryName: String): Bitmap? {
        val key = "$packName::$entryName"
        cache[key]?.let { return it }

        return runCatching {
            context.assets.open(packName).use { raw ->
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
                            if (bitmap != null) cache[key] = bitmap
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
