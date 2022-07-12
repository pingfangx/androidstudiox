package com.pingfangx.plugin.templatex.creator

import com.intellij.ide.actions.CreateFileAction.MkDirs
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.WriteAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.pingfangx.plugin.templatex.android.AndroidFileTemplateUtils
import com.pingfangx.plugin.templatex.android.AndroidLayoutResourceFileTemplateCreator
import com.pingfangx.plugin.templatex.util.TemplateXUtils
import java.util.*

/**
 * 创建模板
 *
 * @author pingfangx
 * @date 2022/7/6
 */
class TemplateCreatorImpl : TemplateCreator {
    /**
     * 参考 com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog.doCreate
     * 只要不遍历 children 就只会创建自身
     */
    override fun create(template: FileTemplate, properties: Properties, dir: PsiDirectory): PsiElement {
        val fileName = TemplateXUtils.getFileNameFromTemplate(template, properties)
        val mkDirs = WriteAction.compute<MkDirs, RuntimeException> {
            MkDirs(fileName, dir)
        }
        return FileTemplateUtil.createFromTemplate(template, mkDirs.newName, properties, mkDirs.directory)
    }
}

class TemplatesCreatorImpl(
    private val dataContext: DataContext,
) : TemplatesCreator {
    override fun create(
        templates: Collection<FileTemplate>,
        properties: Properties,
        dir: PsiDirectory
    ): List<PsiElement?> {
        val files = mutableListOf<PsiElement?>()
        val templateCreator = TemplateCreatorImpl()
        for (template in templates) {
            if (AndroidFileTemplateUtils.isAndroidLayoutResourceFileTemplate(template)) {
                files.add(AndroidLayoutResourceFileTemplateCreator(dataContext).create(template, properties, dir))
            } else {
                files.add(templateCreator.create(template, properties, dir))
            }
        }
        return files
    }
}