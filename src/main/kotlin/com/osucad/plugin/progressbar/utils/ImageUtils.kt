package com.osucad.plugin.progressbar.utils

import org.jdesktop.swingx.graphics.GraphicsUtilities
import java.awt.Color
import java.awt.image.BufferedImage


fun BufferedImage.withTint(
    tint: Color
): BufferedImage {
    val src = this
    val dst = BufferedImage(width, height, type)

    val width = src.width
    val height = src.height

    val pixels = IntArray(width * height)

    GraphicsUtilities.getPixels(src, 0, 0, width, height, pixels)

    for (i in 0 until pixels.size)
        pixels[i] = Color(pixels[i], true).let { color ->

            Color(
                withTint(color.red, tint.red, color.alpha),
                withTint(color.green, tint.green, color.alpha),
                withTint(color.blue, tint.blue, color.alpha),
                color.alpha,
            ).rgb
        }

    GraphicsUtilities.setPixels(dst, 0, 0, width, height, pixels)

    return dst
}


private fun withTint(src: Int, dst: Int, alpha: Int) =
    ((src / 255f) * (dst / 255f) * (alpha / 255f) * 255f).toInt().coerceIn(0, 255)