package com.osucad.plugin.progressbar.settings

import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.jetbrains.Service
import java.awt.Color

@Service
@State(
    name = "com.osucad.plugin.progressbar.settings.OsuProgressBarSettingsState",
    storages = [Storage("OsuProgressBarSettings.xml")]
)
class OsuProgressBarSettings : SerializablePersistentStateComponent<OsuProgressBarSettings.State>(
    State(
        comboColor = Color(235, 99, 89).hexString,
        skinDirectory = "",
    )
) {
    companion object {
        fun getInstance() = service<OsuProgressBarSettings>()
    }

    var comboColor: Color
        get() = Color.decode(state.comboColor)
        set(value) {
            updateState { it.copy(comboColor = value.hexString) }
        }

    var skinDirectory: String
        get() = state.skinDirectory
        set(value) {
            updateState { it.copy(skinDirectory = value) }
        }

    data class State(
        @JvmField val comboColor: String,
        @JvmField val skinDirectory: String,
    )
}

private val Color.hexString get() = "#%06X".format(rgb and 0xFFFFFF)