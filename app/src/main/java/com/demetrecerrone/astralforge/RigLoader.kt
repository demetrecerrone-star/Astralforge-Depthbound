package com.demetrecerrone.astralforge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream

object RigLoader {

    private const val DIRECT_ROOT = "skeletal_runtime"
    private const val ZIP_ASSET = "astralforge_skeletal_runtime_v2.zip"

    private val rigCache = ConcurrentHashMap<String, LoadedRig>()
    private val zipEntryCache = ConcurrentHashMap<String, ByteArray>()

    fun hasRig(context: Context, entityId: String): Boolean {
        return readAssetBytes(context, DIRECT_ROOT + "/" + entityId + "/rig.json") != null ||
            readZipEntryBySuffix(context, "/" + entityId + "/rig.json") != null ||
            readZipEntryBySuffix(context, entityId + "/rig.json") != null
    }

    fun load(context: Context, entityId: String): LoadedRig? {
        rigCache[entityId]?.let { return it }

        val rigBytes =
            readAssetBytes(context, DIRECT_ROOT + "/" + entityId + "/rig.json")
                ?: readZipEntryBySuffix(context, "/" + entityId + "/rig.json")
                ?: readZipEntryBySuffix(context, entityId + "/rig.json")
                ?: return null

        val rigJson = JSONObject(rigBytes.toString(Charsets.UTF_8))

        val schema = rigJson.optInt("schema", 2)
        val displayName =
            rigJson.optString(
                "display_name",
                entityId.replace('_', ' ')
            )
        val kind = rigJson.optString("kind", "biped")
        val defaultFacing =
            rigJson.optString("default_facing", "right")
                .lowercase()

        val canvas = rigJson.optJSONObject("virtual_canvas")
        val canvasWidth =
            canvas?.optDouble("width", 1000.0)?.toFloat() ?: 1000f
        val canvasHeight =
            canvas?.optDouble("height", 1000.0)?.toFloat() ?: 1000f

        val bones = mutableListOf<RigBoneDefinition>()
        val boneArray = rigJson.optJSONArray("bones")
        if (boneArray != null) {
            for (i in 0 until boneArray.length()) {
                val bone = boneArray.getJSONObject(i)
                bones += RigBoneDefinition(
                    name = bone.getString("name"),
                    parent = if (bone.isNull("parent")) {
                        null
                    } else {
                        bone.optString("parent")
                            .takeIf { it.isNotBlank() }
                    },
                    x = bone.optDouble("x", 0.5).toFloat(),
                    y = bone.optDouble("y", 0.5).toFloat()
                )
            }
        }

        val attachments = mutableListOf<RigAttachmentDefinition>()
        val attachmentArray = rigJson.optJSONArray("attachments")
        if (attachmentArray != null) {
            for (i in 0 until attachmentArray.length()) {
                val attachment = attachmentArray.getJSONObject(i)
                val pivot = attachment.optJSONArray("pivot")
                val rest = attachment.optJSONObject("rest")
                val maxSize = attachment.optJSONObject("max_size")

                attachments += RigAttachmentDefinition(
                    role = attachment.optString("role", "part_" + i),
                    bone = attachment.optString(
                        "bone",
                        attachment.optString("role", "root")
                    ),
                    file = attachment.getString("file"),
                    pivotX = pivot?.optDouble(0, 0.5)?.toFloat() ?: 0.5f,
                    pivotY = pivot?.optDouble(1, 0.5)?.toFloat() ?: 0.5f,
                    restX = rest?.optDouble(
                        "x",
                        canvasWidth / 2.0
                    )?.toFloat() ?: canvasWidth / 2f,
                    restY = rest?.optDouble(
                        "y",
                        canvasHeight / 2.0
                    )?.toFloat() ?: canvasHeight / 2f,
                    restRotation =
                        rest?.optDouble("rotation", 0.0)?.toFloat() ?: 0f,
                    restScale =
                        rest?.optDouble("scale", 1.0)?.toFloat() ?: 1f,
                    maxWidth =
                        maxSize?.optDouble("w", 240.0)?.toFloat() ?: 240f,
                    maxHeight =
                        maxSize?.optDouble("h", 320.0)?.toFloat() ?: 320f,
                    z = attachment.optInt("z", 0)
                )
            }
        }

        val animations = linkedMapOf<String, RigAnimationClip>()
        listOf("idle", "attack", "hit", "death").forEach { name ->
            val bytes =
                readAssetBytes(
                    context,
                    DIRECT_ROOT + "/" + entityId + "/animations/" + name + ".json"
                )
                    ?: readZipEntryBySuffix(
                        context,
                        "/" + entityId + "/animations/" + name + ".json"
                    )
                    ?: readZipEntryBySuffix(
                        context,
                        entityId + "/animations/" + name + ".json"
                    )

            if (bytes != null) {
                parseAnimation(name, JSONObject(bytes.toString(Charsets.UTF_8)))
                    ?.let { animations[name] = it }
            }
        }

        val bitmaps = linkedMapOf<String, Bitmap>()
        attachments.forEach { attachment ->
            val relative = entityId + "/" + attachment.file
            val bytes =
                readAssetBytes(
                    context,
                    DIRECT_ROOT + "/" + relative
                )
                    ?: readZipEntryBySuffix(
                        context,
                        "/" + relative
                    )
                    ?: readZipEntryBySuffix(
                        context,
                        relative
                    )

            if (bytes != null) {
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?.let { bitmap ->
                        bitmaps[attachment.role] = bitmap
                    }
            }
        }

        if (bones.isEmpty() || attachments.isEmpty() || bitmaps.isEmpty()) {
            return null
        }

        val loaded = LoadedRig(
            definition = RigDefinition(
                schema = schema,
                id = entityId,
                displayName = displayName,
                kind = kind,
                defaultFacing = defaultFacing,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                bones = bones,
                attachments = attachments,
                animations = animations
            ),
            bitmaps = bitmaps
        )

        rigCache[entityId] = loaded
        return loaded
    }

    fun clear() {
        rigCache.values.forEach { loaded ->
            loaded.bitmaps.values.forEach { bitmap ->
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        }
        rigCache.clear()
        zipEntryCache.clear()
    }

    private fun parseAnimation(
        name: String,
        json: JSONObject
    ): RigAnimationClip? {
        val keysJson = json.optJSONArray("keys") ?: return null
        val keys = mutableListOf<RigAnimationKey>()

        for (i in 0 until keysJson.length()) {
            val keyJson = keysJson.getJSONObject(i)
            val transforms = linkedMapOf<String, RigBoneTransform>()
            val iterator = keyJson.keys()

            while (iterator.hasNext()) {
                val key = iterator.next()
                if (key == "t") continue

                val transformJson = keyJson.optJSONObject(key) ?: continue
                transforms[key] = RigBoneTransform(
                    x = transformJson.optDouble("x", 0.0).toFloat(),
                    y = transformJson.optDouble("y", 0.0).toFloat(),
                    rotation = transformJson.optDouble("r", 0.0).toFloat(),
                    scaleX = transformJson.optDouble("sx", 1.0).toFloat(),
                    scaleY = transformJson.optDouble("sy", 1.0).toFloat()
                )
            }

            keys += RigAnimationKey(
                timeMs = keyJson.optLong("t", 0L),
                transforms = transforms
            )
        }

        return RigAnimationClip(
            name = name,
            durationMs = json.optLong(
                "duration_ms",
                keys.maxOfOrNull { it.timeMs } ?: 0L
            ).coerceAtLeast(1L),
            loop = json.optBoolean("loop", false),
            keys = keys.sortedBy { it.timeMs }
        )
    }

    private fun readAssetBytes(
        context: Context,
        path: String
    ): ByteArray? {
        return runCatching {
            context.assets.open(path).use { it.readBytes() }
        }.getOrNull()
    }

    private fun readZipEntryBySuffix(
        context: Context,
        suffix: String
    ): ByteArray? {
        val cacheKey = suffix.trimStart('/')
        zipEntryCache[cacheKey]?.let { return it }

        return runCatching {
            context.assets.open(ZIP_ASSET).use { raw ->
                ZipInputStream(raw.buffered()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (
                            !entry.isDirectory &&
                            (
                                entry.name == cacheKey ||
                                    entry.name.endsWith("/" + cacheKey)
                            )
                        ) {
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(16 * 1024)
                            var count = zip.read(buffer)

                            while (count > 0) {
                                output.write(buffer, 0, count)
                                count = zip.read(buffer)
                            }

                            val bytes = output.toByteArray()
                            zipEntryCache[cacheKey] = bytes
                            return@runCatching bytes
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
