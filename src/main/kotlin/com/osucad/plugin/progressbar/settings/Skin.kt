package com.osucad.plugin.progressbar.settings

import com.osucad.plugin.progressbar.utils.withTint
import java.awt.Color
import java.awt.image.BufferedImage

class Skin(
    val config: SkinConfig,
    loadTexture: (name: String) -> BufferedImage?,
) {
    val hitCircle = loadTexture("hitcircle")
    val hitCircleOverlay = loadTexture("hitcircleoverlay")
    val sliderbND = loadTexture("sliderb-nd")?.withTint(Color(5, 5, 5, 255))
    val sliderFollowCircle = loadTexture("sliderfollowcircle")
    val reverseArrow = loadTexture("reversearrow")
    val sliderEndCircle = loadTexture("sliderendcircle")
    val sliderEndCircleOverlay = loadTexture("sliderendcircleoverlay")

    val sliderb: List<BufferedImage>? = run {
        loadTexture("sliderb")?.let { return@run listOf(it) }

        var index = 0

        val textures = mutableListOf<BufferedImage>()

        while (true) {
            textures += loadTexture("sliderb$index") ?: break

            index++
        }

        textures.takeIf { it.isNotEmpty() }
    }

}