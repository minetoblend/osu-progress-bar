package com.osucad.plugin.progressbar

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import com.osucad.plugin.progressbar.ui.OsuProgressbarUI
import javax.swing.UIManager


class OsuApplicationComponent : LafManagerListener, ApplicationActivationListener {
    override fun lookAndFeelChanged(p0: LafManager) = updateProgressBarUi()

    override fun applicationActivated(ideFrame: IdeFrame) = updateProgressBarUi()

    private fun updateProgressBarUi() {
        UIManager.put("ProgressBarUI", OsuProgressbarUI::class.java.getName())
        UIManager.getDefaults()[OsuProgressbarUI::class.java.getName()] = OsuProgressbarUI::class.java
    }
}