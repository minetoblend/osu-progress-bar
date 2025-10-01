package com.osucad.plugin.progressbar.settings

import java.awt.Color
import java.io.File

class SkinConfig(
    val sliderBorder: Color = Color.WHITE,
    val sliderTrackOverride: Color? = null,
    val allowSliderBallTint: Boolean = true
) {
    companion object {
        fun fromFile(file: File): SkinConfig = runCatching {
            if (!file.exists()) return SkinConfig()

            parse(file.readText())
        }.getOrElse { SkinConfig() }

        fun parse(skinIni: String): SkinConfig = runCatching {
            var sliderBorder: Color = Color.WHITE
            var sliderTrackOverride: Color? = null
            var allowSliderBallTint = false

            for (line in skinIni.lines().map { it.trim() }) {
                if (line.startsWith("SliderBorder")) {
                    line.split(":").getOrNull(1)?.toColorOrNull()?.let {
                        sliderBorder = it
                    }
                }

                if (line.startsWith("SliderTrackOverride"))
                    sliderTrackOverride = line.split(":").getOrNull(1)?.toColorOrNull()

                if (line.startsWith("AllowSliderBallTint"))
                    allowSliderBallTint = line.split(":").getOrNull(1)?.trim() == "1"
            }

            return SkinConfig(
                sliderBorder = sliderBorder,
                sliderTrackOverride = sliderTrackOverride,
                allowSliderBallTint = allowSliderBallTint,
            )
        }.getOrElse { SkinConfig() }

        private fun String.toColorOrNull(): Color? {
            runCatching {
                val parts = split(",").mapNotNull { it.trim().toIntOrNull() }

                if (parts.size != 3) return null

                val (red, green, blue) = parts.map { it.coerceIn(0, 255) }

                return Color(red, green, blue)
            }
            return null
        }
    }
}