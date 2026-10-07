package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache

object PortraitAssetStore {
    private const val TILE_SIZE = 128
    private const val GRID_COLUMNS = 4
    private const val CHUNK_COUNT = 7

    private val portraitIds = listOf(
        "skeleton",
        "warrior_m",
        "mage_m",
        "elf_m",
        "ranger_f",
        "sorceress_f",
        "knight_f",
        "orc",
        "goblin",
        "demon",
        "wolfkin",
        "lich",
        "dragonkin",
        "masked"
    )

    private val portraitCache = LruCache<String, Bitmap>(20)
    private var atlasBitmap: Bitmap? = null

    @Synchronized
    fun load(context: Context, portraitId: String): Bitmap? {
        portraitCache.get(portraitId)?.let { return it }

        val index = portraitIds.indexOf(portraitId)
        if (index < 0) return null

        val atlas = loadAtlas(context) ?: return null
        val column = index % GRID_COLUMNS
        val row = index / GRID_COLUMNS
        val left = column * TILE_SIZE
        val top = row * TILE_SIZE

        if (
            left < 0 ||
            top < 0 ||
            left + TILE_SIZE > atlas.width ||
            top + TILE_SIZE > atlas.height
        ) {
            return null
        }

        return try {
            Bitmap.createBitmap(
                atlas,
                left,
                top,
                TILE_SIZE,
                TILE_SIZE
            ).also { portrait ->
                portraitCache.put(portraitId, portrait)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun loadAtlas(context: Context): Bitmap? {
        atlasBitmap?.let { return it }

        return try {
            val encoded = buildString {
                repeat(CHUNK_COUNT) { index ->
                    val fileName =
                        "portrait_atlas/part" +
                            index.toString().padStart(2, '0') +
                            ".txt"

                    context.assets.open(fileName).use { input ->
                        input.bufferedReader(Charsets.US_ASCII).use { reader ->
                            append(reader.readText().trim())
                        }
                    }
                }
            }

            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            val decoded = BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )

            if (
                decoded != null &&
                decoded.width >= TILE_SIZE * GRID_COLUMNS &&
                decoded.height >= TILE_SIZE * GRID_COLUMNS
            ) {
                atlasBitmap = decoded
                decoded
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
