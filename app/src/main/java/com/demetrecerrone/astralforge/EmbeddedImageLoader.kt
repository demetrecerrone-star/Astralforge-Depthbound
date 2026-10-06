package com.demetrecerrone.astralforge

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

object EmbeddedImageLoader {
    fun decodeNamed(
        resources: Resources,
        packageName: String,
        prefix: String,
        chunkCount: Int
    ): Bitmap? {
        val ids = IntArray(chunkCount) { index ->
            resources.getIdentifier(
                prefix + "_" + index.toString().padStart(2, '0'),
                "raw",
                packageName
            )
        }
        if (ids.any { it == 0 }) return null
        return decode(resources, ids)
    }

    fun decode(resources: Resources, chunks: IntArray): Bitmap {
        val encoded = buildString {
            chunks.forEach { id ->
                resources.openRawResource(id).bufferedReader().use { append(it.readText()) }
            }
        }
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        return requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)) {
            "Unable to decode embedded auth artwork"
        }
    }
}
