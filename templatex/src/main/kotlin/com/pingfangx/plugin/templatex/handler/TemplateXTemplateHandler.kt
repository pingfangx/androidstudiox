package com.pingfangx.plugin.templatex.handler

import com.intellij.ide.DataManager
import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiDirectory
import com.pingfangx.plugin.templatex.model.data.TemplateXConfigData
import com.pingfangx.plugin.templatex.model.data.TemplateXStateService
import com.pingfangx.plugin.templatex.util.TemplateXUtils

/**
 * 用于过滤模板
 *
 * @author pingfangx
 * @date 2022/7/7
 */
class TemplateXTemplateHandler : DefaultCreateFromTemplateHandler() {
    private var _config: TemplateXConfigData? = null
    private val config: TemplateXConfigData?
        get() =
            _config ?: run {
                val dataContext = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(2000)
                dataContext?.getData(CommonDataKeys.PROJECT)?.let { project ->
                    TemplateXStateService.getInstance(project).config.also {
                        _config = it
                    }
                }
            }

    override fun handlesTemplate(template: FileTemplate): Boolean {
        return if (config?.showTemplatesContainingSeparatorInNewGroup == false) {
            // 如果不显示，则需要判断是否包含分隔符
            TemplateXUtils.containsSeparator(template.name)
        } else {
            true
        }
    }

    override fun canCreate(dirs: Array<out PsiDirectory>): Boolean {
        return config?.showTemplatesContainingSeparatorInNewGroup != false
    }
}