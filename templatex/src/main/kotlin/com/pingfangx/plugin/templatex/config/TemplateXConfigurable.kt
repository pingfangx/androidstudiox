package com.pingfangx.plugin.templatex.config

import com.intellij.openapi.options.Configurable
import com.pingfangx.plugin.templatex.TemplateXBundle
import com.pingfangx.plugin.templatex.view.ui.TemplateXPanel
import com.pingfangx.plugin.util.ProjectUtils
import javax.swing.JComponent

/**
 * 配置
 *
 * @author pingfangx
 * @date 2022/7/7
 */
class TemplateXConfigurable : Configurable {
    private lateinit var templateXPanel: TemplateXPanel
    override fun createComponent(): JComponent? {
        val project = ProjectUtils.currentProject ?: return null
        return TemplateXPanel(project).also {
            templateXPanel = it
        }
    }

    override fun isModified(): Boolean =
        templateXPanel.isModified

    override fun reset() {
        templateXPanel.reset()
    }

    override fun apply() {
        templateXPanel.apply()
    }

    override fun getDisplayName(): String =
        TemplateXBundle.message("action.com.pingfangx.TemplateX.text")
}