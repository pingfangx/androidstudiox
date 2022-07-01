package com.pingfangx.plugin.templatex.creator

import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import java.util.*

/**
 * 创建模板
 *
 * @author pingfangx
 * @date 2022/7/6
 */
fun interface TemplateCreator {
    fun create(template: FileTemplate, properties: Properties, dir: PsiDirectory): PsiElement?
}

fun interface TemplatesCreator {
    fun create(templates: Collection<FileTemplate>, properties: Properties, dir: PsiDirectory): List<PsiElement?>
}