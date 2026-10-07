package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.ZipInputStream

object SpriteZipStore {

    data class SpritePick(
        val entry: String?,
        val bitmap: Bitmap?,
        val hint: String
    )

    private val entryCache = mutableMapOf<String, List<String>>()
    private val bitmapCache = mutableMapOf<String, Bitmap>()

    @Synchronized
    fun listPngEntries(context: Context, packName: String): List<String> {
        entryCache[packName]?.let { return it }

        val entries = runCatching {
            val result = mutableListOf<String>()
            context.assets.open(packName).use { raw ->
                ZipInputStream(BufferedInputStream(raw)).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory &&
                            entry.name.lowercase(Locale.US).endsWith(".png")
                        ) {
                            result += entry.name
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            result.sortedWith(naturalEntryComparator())
        }.getOrDefault(emptyList())

        entryCache[packName] = entries
        return entries
    }

    @Synchronized
    fun loadEntry(
        context: Context,
        packName: String,
        entryName: String
    ): Bitmap? {
        val key = "$packName::$entryName"
        bitmapCache[key]?.let { return it }

        return runCatching {
            context.assets.open(packName).use { raw ->
                ZipInputStream(BufferedInputStream(raw)).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val exact = entry.name == entryName
                        val sameBase =
                            entry.name.substringAfterLast('/') ==
                                entryName.substringAfterLast('/')

                        if (!entry.isDirectory && (exact || sameBase)) {
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(16 * 1024)
                            var read = zip.read(buffer)
                            while (read > 0) {
                                output.write(buffer, 0, read)
                                read = zip.read(buffer)
                            }

                            val bytes = output.toByteArray()
                            val bitmap = BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size
                            )
                            if (bitmap != null) {
                                bitmapCache[key] = bitmap
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

    fun pickIdle(
        context: Context,
        packName: String
    ): SpritePick {
        val entries = listPngEntries(context, packName)
        if (entries.isEmpty()) {
            return SpritePick(null, null, "")
        }

        val entry =
            entries.firstOrNull { it.lowercase(Locale.US).contains("idle") }
                ?: entries.firstOrNull {
                    val lower = it.lowercase(Locale.US)
                    !lower.contains("attack") &&
                        !lower.contains("death") &&
                        !lower.contains("hurt") &&
                        !lower.contains("hit")
                }
                ?: entries.first()

        return SpritePick(
            entry = entry,
            bitmap = loadEntry(context, packName, entry),
            hint = deriveHint(entry)
        )
    }

    fun loadActionFrames(
        context: Context,
        packName: String,
        action: String,
        hint: String,
        limit: Int = 10
    ): List<Bitmap> {
        val aliases = when (action.lowercase(Locale.US)) {
            "attack" -> listOf("attack", "atk", "strike", "slash")
            "hit" -> listOf("hit", "hurt", "damage", "attacked")
            "death" -> listOf("death", "die", "dead", "defeat")
            else -> listOf(action.lowercase(Locale.US))
        }

        val all = listPngEntries(context, packName)
        var candidates = all.filter { entry ->
            val lower = entry.lowercase(Locale.US)
            aliases.any { lower.contains(it) }
        }

        if (candidates.isEmpty()) {
            candidates = all
        }

        val hintTokens = hint
            .lowercase(Locale.US)
            .split('_', '-', ' ', '/', '.')
            .filter { it.length >= 4 }
            .filterNot {
                it in setOf(
                    "idle",
                    "frame",
                    "sprite",
                    "character",
                    "monster",
                    "attack",
                    "death"
                )
            }

        if (hintTokens.isNotEmpty()) {
            val hinted = candidates.filter { entry ->
                val lower = entry.lowercase(Locale.US)
                hintTokens.count { lower.contains(it) } >=
                    minOf(2, hintTokens.size)
            }
            if (hinted.isNotEmpty()) {
                candidates = hinted
            }
        }

        return candidates
            .sortedWith(naturalEntryComparator())
            .take(limit.coerceAtLeast(1))
            .mapNotNull { loadEntry(context, packName, it) }
    }

    fun displayNameFromHint(
        hint: String,
        fallback: String
    ): String {
        if (hint.isBlank()) return fallback

        val ignored = setOf(
            "idle",
            "frame",
            "sprite",
            "character",
            "monster",
            "png"
        )

        val words = hint
            .split('_', '-', ' ')
            .filter { it.isNotBlank() }
            .filterNot { it.lowercase(Locale.US) in ignored }
            .take(3)

        if (words.isEmpty()) return fallback

        return words.joinToString(" ") { word ->
            word.lowercase(Locale.US)
                .replaceFirstChar { c ->
                    if (c.isLowerCase()) c.titlecase(Locale.US)
                    else c.toString()
                }
        }
    }

    private fun deriveHint(entry: String): String {
        val pathParts = entry.split('/')
        val parent = pathParts
            .dropLast(1)
            .lastOrNull()
            ?.takeIf {
                val lower = it.lowercase(Locale.US)
                lower !in setOf(
                    "idle",
                    "attack",
                    "hit",
                    "hurt",
                    "death",
                    "frames",
                    "sprites"
                )
            }

        if (!parent.isNullOrBlank()) return parent

        return entry
            .substringAfterLast('/')
            .substringBeforeLast('.')
            .replace(
                Regex(
                    "(?i)(idle|attack|atk|hit|hurt|death|die|frame|sprite)"
                ),
                ""
            )
            .replace(Regex("[0-9]+"), "")
            .trim('_', '-', ' ')
    }

    private fun naturalEntryComparator(): Comparator<String> {
        return Comparator { a, b ->
            val regex = Regex("(\\d+)|(\\D+)")
            val aa = regex.findAll(a.lowercase(Locale.US))
                .map { it.value }
                .toList()
            val bb = regex.findAll(b.lowercase(Locale.US))
                .map { it.value }
                .toList()

            val count = minOf(aa.size, bb.size)
            for (i in 0 until count) {
                val av = aa[i]
                val bv = bb[i]
                val ai = av.toIntOrNull()
                val bi = bv.toIntOrNull()
                val cmp = if (ai != null && bi != null) {
                    ai.compareTo(bi)
                } else {
                    av.compareTo(bv)
                }
                if (cmp != 0) return@Comparator cmp
            }
            aa.size.compareTo(bb.size)
        }
    }
}
