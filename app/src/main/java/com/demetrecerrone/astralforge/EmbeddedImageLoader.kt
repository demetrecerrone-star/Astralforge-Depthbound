package com.demetrecerrone.astralforge

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

object EmbeddedImageLoader {
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
