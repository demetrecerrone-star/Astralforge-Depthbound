package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream

data class FullBodyMeshActorAssets(
    val entityId: String,
    val defaultFacing: String,
    val body: Bitmap,
    val cape: Bitmap? = null,
    val sword: Bitmap? = null,
    val shield: Bitmap? = null
)

object FullBodyMeshAssetStore {

    private const val ZIP_ASSET =
        "mesh_battle_prototype_v1_mobile.zip"

    private val actorCache =
        ConcurrentHashMap<String, FullBodyMeshActorAssets>()
    private val entryCache =
        ConcurrentHashMap<String, ByteArray>()

    fun supports(entityId: String): Boolean {
        return entityId == "knight" ||
            entityId == "dire_wolf"
    }

    fun load(
        context: Context,
        entityId: String
    ): FullBodyMeshActorAssets? {
        actorCache[entityId]?.let { return it }
        if (!supports(entityId)) return null

        val loaded = when (entityId) {
            "knight" -> {
                val body =
                    decode(context, "knight_body.webp")
                        ?: return null
                FullBodyMeshActorAssets(
                    entityId = entityId,
                    defaultFacing = "right",
                    body = body,
                    cape = decode(
                        context,
                        "knight_cape.webp"
                    ),
                    sword = decode(
                        context,
                        "knight_sword.webp"
                    ),
                    shield = decode(
                        context,
                        "knight_shield.webp"
                    )
                )
            }

            "dire_wolf" -> {
                val body =
                    decode(context, "wolf_body.webp")
                        ?: return null
                FullBodyMeshActorAssets(
                    entityId = entityId,
                    defaultFacing = "left",
                    body = body
                )
            }

            else -> return null
        }

        actorCache[entityId] = loaded
        return loaded
    }

    private fun decode(
        context: Context,
        name: String
    ): Bitmap? {
        val bytes =
            entryCache[name] ?: readEntry(context, name)
                ?.also { entryCache[name] = it }
                ?: return null

        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size
        )
    }

    private fun readEntry(
        context: Context,
        target: String
    ): ByteArray? {
        return runCatching {
            context.assets.open(ZIP_ASSET).use { raw ->
                ZipInputStream(raw.buffered()).use { zip ->
                    var entry = zip.nextEntry

                    while (entry != null) {
                        if (
                            !entry.isDirectory &&
                            entry.name == target
                        ) {
                            val output =
                                ByteArrayOutputStream()
                            val buffer = ByteArray(16 * 1024)
                            var count = zip.read(buffer)

                            while (count > 0) {
                                output.write(
                                    buffer,
                                    0,
                                    count
                                )
                                count = zip.read(buffer)
                            }

                            return@runCatching output
                                .toByteArray()
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
