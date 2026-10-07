package com.demetrecerrone.astralforge

import android.content.Context
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.GZIPInputStream

internal object EmbeddedDabskyVideo {
    fun materialize(context: Context): File {
        val out = File(context.cacheDir, "dabsky_intro.mp4")
        if (out.exists() && out.length() > 8_000L) {
            return out
        }

        val encoded = buildString(22_000) {
            append(EmbeddedDabskyPart0.DATA)
            append(EmbeddedDabskyPart1.DATA)
            append(EmbeddedDabskyPart2.DATA)
            append(EmbeddedDabskyPart3.DATA)
        }

        val compressed = Base64.decode(encoded, Base64.NO_WRAP)
        GZIPInputStream(ByteArrayInputStream(compressed)).use { input ->
            out.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        check(out.length() > 8_000L) {
            "Embedded DABSKY intro could not be materialized."
        }
        return out
    }
}
