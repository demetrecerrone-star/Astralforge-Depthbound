package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.util.zip.ZipInputStream

object PortraitAssetStore {
    private const val PACK = "astral_portraits.zip"
    private val cache = LruCache<String, Bitmap>(20)

    @Synchronized
    fun load(context: Context, portraitId: String): Bitmap? {
        cache.get(portraitId)?.let { return it }

        val target = "$portraitId.webp"
        return try {
            context.assets.open(PACK).use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && entry.name == target) {
                            val bitmap = BitmapFactory.decodeStream(zip)
                            if (bitmap != null) {
                                cache.put(portraitId, bitmap)
                            }
                            return bitmap
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}
