package com.osucad.plugin.progressbar.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.ui.ColorPanel
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton
import com.intellij.ui.components.textFieldWithHistoryWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import java.awt.Color
import javax.swing.JPanel


class OsuProgressBarSettingsComponent {
    val panel: JPanel

    private val colorPanel = ColorPanel()
    private var skinDirectory = ""

    var comboColor = Color(235, 99, 89)
        set(value) {
            field = value
            colorPanel.selectedColor = value
        }


    init {
        colorPanel.addActionListener { it.actionCommand }
        colorPanel.addActionListener { e ->
            if (e.actionCommand == "colorPanelChanged" && colorPanel.selectedColor != null)
                comboColor = colorPanel.selectedColor!!
        }

        panel = panel {
            row("Combo Color:") {
                cell(colorPanel)
                    .align(AlignX.FILL)
                    .component
            }
            row("Skin Location:") {
                cell(
                    textFieldWithHistoryWithBrowseButton(
                        project = null,
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    )
                ).bindText(::skinDirectory)
                    .applyToComponent { this }


            }
        }
    }
}

