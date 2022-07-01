package com.pingfangx.plugin.templatex.propertiesprovider

import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider
import com.intellij.psi.PsiDirectory
import com.pingfangx.plugin.templatex.util.TemplateXUtils
import java.util.*

/**
 * 提供属性
 *
 * @author pingfangx
 * @date 2022/7/11
 */
class TemplateXPropertiesProvider : DefaultTemplatePropertiesProvider {
    override fun fillProperties(directory: PsiDirectory, props: Properties) {
        TemplateXUtils.fillSeparator(props)
    }
}