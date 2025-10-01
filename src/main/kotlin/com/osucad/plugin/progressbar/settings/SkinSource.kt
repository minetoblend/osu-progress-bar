package com.osucad.plugin.progressbar.settings

import java.io.File
import javax.imageio.ImageIO

object SkinSource {
    private val defaultSkin = Skin(
        config = SkinConfig()
    ) {
        javaClass.getResource("/icons/$it.png")?.let(ImageIO::read)

    }

    private var _customSkin: Skin? = null
    private var _skinPath: String? = null

    private val customSkin: Skin?
        get() {
            val skinPath = OsuProgressBarSettings.getInstance().skinDirectory

            if (skinPath != _skinPath) {
                _skinPath = skinPath

                _customSkin = runCatching {
                    File(skinPath).takeIf { it.exists() && it.isDirectory }
                        ?.let { directory ->
                            Skin(
                                config = SkinConfig.fromFile(directory.resolve("skin.ini"))
                            ) { lookup ->
                                runCatching {
                                    val file = directory.resolve("$lookup.png").takeIf { it.exists() }
                                        ?: directory.resolve("$lookup@2x.png").takeIf { it.exists() }

                                    file?.let(ImageIO::read)
                                }.getOrNull()
                            }
                        }
                }.getOrNull()
            }

            return _customSkin
        }

    val allSources
        get() = sequence {
            customSkin?.let { yield(it) }

            yield(defaultSkin)
        }

    fun getProvider(predicate: (Skin) -> Boolean): Skin? =
        allSources.find(predicate)

    fun <T> getOrNull(lookup: Skin.() -> T?): T? {
        return customSkin?.lookup() ?: defaultSkin.lookup()
    }

    fun <T : Any> get(lookup: Skin.() -> T?): T = getOrNull(lookup)!!
}