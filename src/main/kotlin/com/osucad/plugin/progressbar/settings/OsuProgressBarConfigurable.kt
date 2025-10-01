package com.osucad.plugin.progressbar.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.*
import java.awt.Color

class OsuProgressBarConfigurable : BoundConfigurable("osu! Progress Bar") {

    override fun createPanel(): DialogPanel {
        val skinDirectoryField = createSkinDirectoryField()

        val settings = OsuProgressBarSettings.getInstance()

        return panel {
            group(displayName) {
                row("Skin directory:") {
                    cell(skinDirectoryField)
                        .setupSkinDirectoryField()
                        .align(AlignX.FILL)
                }
                row("Combo color:") {
                    cell(createComboColorPicker())
                        .bind(
                            componentGet = { it.selectedColor ?: Color(235, 99, 89) },
                            componentSet = ColorPanel::setSelectedColor,
                            prop = settings::comboColor.toMutableProperty()
                        )
                }
            }
        }
    }

    private fun createSkinDirectoryField(): TextFieldWithBrowseButton {
        return textFieldWithBrowseButton(
            project = null,
            fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),

            )
    }

    private fun createComboColorPicker() = ColorPanel().apply {
        addActionListener { e ->
            if (e.actionCommand == "colorPanelChanged") selectedColor?.let { color ->
                OsuProgressBarSettings.getInstance().comboColor = color
            }
        }
    }
}

private fun Cell<TextFieldWithBrowseButton>.setupSkinDirectoryField(): Cell<TextFieldWithBrowseButton> =
    apply {
        val settings = OsuProgressBarSettings.getInstance()

        bindText(settings::skinDirectory)
    }