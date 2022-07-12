package com.pingfangx.plugin.templatex.view.dialog

import com.intellij.CommonBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiDirectory
import com.pingfangx.plugin.templatex.TemplateXBundle
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

    override fun createCenterPanel(): JComponent = TemplateXPanel(project, selectedDir).also {
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