package com.pingfangx.plugin.templatex

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.pingfangx.plugin.templatex.creator.TemplatesCreatorImpl
import com.pingfangx.plugin.templatex.view.dialog.TemplateXDialog
import com.pingfangx.plugin.util.EditorUtils

/**
 * 添加模板
 *
 * @author pingfangx
 * @date 2022/7/1
 */
class TemplateXAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedDir = e.getData(LangDataKeys.IDE_VIEW)?.orChooseDirectory ?: return

        val dialog = TemplateXDialog(project, selectedDir)
        if (dialog.showAndGet()) {
            val (fileTemplates, properties) = dialog.templatesAndProperties
            val openFilesAfterCreation = dialog.openFilesAfterCreation
            val psiElements = TemplatesCreatorImpl().create(fileTemplates, properties, selectedDir)
            if (openFilesAfterCreation) {
                EditorUtils.openEditors(project, psiElements.mapNotNull { it?.containingFile?.virtualFile }, true)
            }
        }
    }
}