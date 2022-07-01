package com.pingfangx.plugin.templatex.creator

import com.intellij.ide.actions.CreateFileAction.MkDirs
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.impl.FileTemplateBase
import com.intellij.openapi.application.WriteAction
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
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
        val fileName = if (FileTemplateBase.isChild(template)) {
            // 如果是子模块，则使用 fileName
            template.fileName.ifEmpty {
                properties.getProperty(FileTemplate.ATTRIBUTE_NAME)
            }
        } else {
            // 否则使用模块名，将在 createFile 中解析文字
            template.name
        }
        return createFile(template, properties, dir, fileName)
    }

    /**
     * com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog.createFile
     */
    private fun createFile(
        template: FileTemplate,
        properties: Properties,
        dir: PsiDirectory,
        name: String,
    ): PsiElement {
        val fileName = TemplateXUtils.getFileNameFromTemplateName(name)
        // 此次合并会使用 properties 中定义的变量填充
        val newName = FileTemplateUtil.mergeTemplate(properties, fileName, false)
        val mkDirs = WriteAction.compute<MkDirs, RuntimeException> {
            MkDirs(newName, dir)
        }
        return FileTemplateUtil.createFromTemplate(template, mkDirs.newName, properties, mkDirs.directory)
    }
}

class TemplatesCreatorImpl : TemplatesCreator {
    override fun create(
        templates: Collection<FileTemplate>,
        properties: Properties,
        dir: PsiDirectory
    ): List<PsiElement?> {
        val files = mutableListOf<PsiElement?>()
        val templateCreator = TemplateCreatorImpl()
        for (template in templates) {
            files.add(templateCreator.create(template, properties, dir))
        }
        return files
    }
}