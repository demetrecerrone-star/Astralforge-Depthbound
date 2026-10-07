package com.demetrecerrone.astralforge

import android.graphics.Bitmap

data class RigBoneDefinition(
    val name: String,
    val parent: String?,
    val x: Float,
    val y: Float
)

data class RigAttachmentDefinition(
    val role: String,
    val bone: String,
    val file: String,
    val pivotX: Float,
    val pivotY: Float,
    val restX: Float,
    val restY: Float,
    val restRotation: Float,
    val restScale: Float,
    val maxWidth: Float,
    val maxHeight: Float,
    val z: Int
)

data class RigBoneTransform(
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f
)

data class RigAnimationKey(
    val timeMs: Long,
    val transforms: Map<String, RigBoneTransform>
)

data class RigAnimationClip(
    val name: String,
    val durationMs: Long,
    val loop: Boolean,
    val keys: List<RigAnimationKey>
)

data class RigDefinition(
    val schema: Int,
    val id: String,
    val displayName: String,
    val kind: String,
    val defaultFacing: String,
    val canvasWidth: Float,
    val canvasHeight: Float,
    val bones: List<RigBoneDefinition>,
    val attachments: List<RigAttachmentDefinition>,
    val animations: Map<String, RigAnimationClip>
)

data class LoadedRig(
    val definition: RigDefinition,
    val bitmaps: Map<String, Bitmap>
)
