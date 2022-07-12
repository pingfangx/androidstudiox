package com.pingfangx.plugin.templatex.handler

import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.psi.PsiDirectory
import com.pingfangx.plugin.templatex.model.data.TemplateXStateService
import com.pingfangx.plugin.templatex.util.TemplateXUtils

/**
 * 用于过滤模板
 *
 * @author pingfangx
 * @date 2022/7/7
 */
class TemplateXTemplateHandler : DefaultCreateFromTemplateHandler() {
    private val config by lazy { TemplateXStateService.config }

    override fun handlesTemplate(template: FileTemplate): Boolean {
        return if (!config.showTemplatesContainingSeparatorInNewGroup) {
            // 如果不显示，则需要判断是否包含分隔符
            TemplateXUtils.containsSeparator(template.name)
        } else {
            true
        }
    }

    override fun canCreate(dirs: Array<out PsiDirectory>): Boolean {
        return config.showTemplatesContainingSeparatorInNewGroup
    }
}