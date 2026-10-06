package com.demetrecerrone.astralforge

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import kotlin.math.max
import kotlin.math.roundToInt

class ReferenceOverlayLayout(
    context: Context,
    private val referenceWidth: Float = 864f,
    private val referenceHeight: Float = 1536f,
    private val stretchToFit: Boolean = false
) : FrameLayout(context) {

    data class ReferenceRect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    )

    private val mappedRects = mutableMapOf<View, ReferenceRect>()

    fun addMappedView(
        view: View,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        mappedRects[view] = ReferenceRect(left, top, right, bottom)
        addView(view, LayoutParams(1, 1))
    }

    private fun transform(rect: ReferenceRect): IntArray {
        if (stretchToFit) {
            val scaleX = width / referenceWidth
            val scaleY = height / referenceHeight
            return intArrayOf(
                (rect.left * scaleX).roundToInt(),
                (rect.top * scaleY).roundToInt(),
                (rect.right * scaleX).roundToInt(),
                (rect.bottom * scaleY).roundToInt()
            )
        }

        val scale = max(width / referenceWidth, height / referenceHeight)
        val renderedWidth = referenceWidth * scale
        val renderedHeight = referenceHeight * scale
        val offsetX = (width - renderedWidth) / 2f
        val offsetY = (height - renderedHeight) / 2f
        return intArrayOf(
            (offsetX + rect.left * scale).roundToInt(),
            (offsetY + rect.top * scale).roundToInt(),
            (offsetX + rect.right * scale).roundToInt(),
            (offsetY + rect.bottom * scale).roundToInt()
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, h)

        mappedRects.forEach { (child, rect) ->
            val cw: Int
            val ch: Int

            if (stretchToFit) {
                val scaleX = w / referenceWidth
                val scaleY = h / referenceHeight
                cw = ((rect.right - rect.left) * scaleX)
                    .roundToInt()
                    .coerceAtLeast(1)
                ch = ((rect.bottom - rect.top) * scaleY)
                    .roundToInt()
                    .coerceAtLeast(1)
            } else {
                val scale = max(w / referenceWidth, h / referenceHeight)
                cw = ((rect.right - rect.left) * scale)
                    .roundToInt()
                    .coerceAtLeast(1)
                ch = ((rect.bottom - rect.top) * scale)
                    .roundToInt()
                    .coerceAtLeast(1)
            }

            child.measure(
                MeasureSpec.makeMeasureSpec(cw, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(ch, MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mappedRects.forEach { (child, rect) ->
            val p = transform(rect)
            child.layout(p[0], p[1], p[2], p[3])
        }
    }
}
