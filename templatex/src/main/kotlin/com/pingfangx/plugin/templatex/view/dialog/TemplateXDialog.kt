package com.pingfangx.plugin.templatex.view.dialog

import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiDirectory
import com.pingfangx.plugin.templatex.TemplateXBundle
import com.pingfangx.plugin.templatex.model.data.TemplateXStateService
import com.pingfangx.plugin.templatex.view.ui.TemplateXPanel
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

/**
 * 选择模板弹窗
 *
 * @author pingfangx
 * @date 2022/7/4
 */
class TemplateXDialog(
    private val project: Project,
    private val selectedDir: PsiDirectory,
) : DialogWrapper(project) {
    /**
     * 配置
     *
     * 如果需要点确定再保存，可以考虑传一个 copy，然后点确定再用 copy 赋值回去
     */
    private val config by lazy { TemplateXStateService.getInstance(project).config }
    private lateinit var templateXPanel: TemplateXPanel

    /** 模板和属性 */
    val templatesAndProperties
        get() = Pair(templateXPanel.selectionTemplates, templateXPanel.properties)

    /** 是否在创建后打开文件 */
    val openFilesAfterCreation
        get() = templateXPanel.openFilesAfterCreation

    init {
        title = TemplateXBundle.message("action.com.pingfangx.TemplateX.text")
        // 注意要在属性初始化以后
        init()
    }

    override fun createCenterPanel(): JComponent = TemplateXPanel(project, config, selectedDir).also {
        templateXPanel = it
    }

    override fun createActions(): Array<Action> {
        val applyAction = object : AbstractAction(CommonBundle.getApplyButtonText()) {
            override fun actionPerformed(e: ActionEvent) {
                templateXPanel.apply()
                close(NEXT_USER_EXIT_CODE)
            }
        }
        return arrayOf(okAction, applyAction, cancelAction)
    }

    override fun doOKAction() {
        if (templateXPanel.applyAndClose()) {
            super.doOKAction()
        }
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return templateXPanel.preferredFocusedComponent
    }
}