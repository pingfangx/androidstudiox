package com.pingfangx.plugin.templatex.model.data

import java.io.Serializable

/**
 * 配置数据
 *
 * @author pingfangx
 * @date 2022/7/4
 */
data class TemplateXConfigData(
    /** 选中的模板名 */
    var selectionTemplateNames: MutableSet<String> = mutableSetOf(),
    /** 忽略的模板名 */
    var ignoredTemplateNames: MutableSet<String> = mutableSetOf(),
    /** 是否在创建后打开文件 */
    var openFilesAfterCreation: Boolean = true,
    /** 是否在新建菜单显示包含分隔符的模板 */
    var showTemplatesContainingSeparatorInNewGroup: Boolean = true,
) : Serializable {
    fun update(source: TemplateXConfigData) {
        selectionTemplateNames.clear()
        selectionTemplateNames.addAll(source.selectionTemplateNames)
        ignoredTemplateNames.clear()
        ignoredTemplateNames.addAll(source.ignoredTemplateNames)
        openFilesAfterCreation = source.openFilesAfterCreation
        showTemplatesContainingSeparatorInNewGroup = source.showTemplatesContainingSeparatorInNewGroup
    }
}