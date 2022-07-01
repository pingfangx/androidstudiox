package com.pingfangx.plugin.templatex.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.pingfangx.plugin.templatex.TemplateXBundle
import com.pingfangx.plugin.templatex.model.data.TemplateXStateService
import com.pingfangx.plugin.templatex.view.ui.TemplateXPanel
import javax.swing.JComponent

/**
 * 配置
 *
 * @author pingfangx
 * @date 2022/7/7
 */
class TemplateXConfigurable(private val project: Project) : Configurable {
    private val config by lazy { TemplateXStateService.getInstance(project).config }
    private lateinit var templateXPanel: TemplateXPanel
    override fun createComponent(): JComponent = TemplateXPanel(project, config).also {
        templateXPanel = it
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