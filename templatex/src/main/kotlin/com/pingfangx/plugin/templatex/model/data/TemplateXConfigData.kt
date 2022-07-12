package com.pingfangx.plugin.templatex.model.data

import java.io.Serializable

/**
 * 配置数据
 *
 * 当添加字段时
 * 1. 添加字段
 * 2. 更新 [update] 方法
 * 3. 更新创建方法
 *
 * @author pingfangx
 * @date 2022/7/4
 */
data class TemplateXConfigData(
    /** 选中的模板名 */
    var selectionTemplateNames: MutableSet<String> = mutableSetOf(),
    /** 忽略的模板名 */
    var ignoredTemplateNames: MutableSet<String> = mutableSetOf(),
    /** 输入的变量 */
    var inputtedVariables: MutableMap<String, String> = mutableMapOf(),
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
        inputtedVariables.clear()
        inputtedVariables.putAll(source.inputtedVariables)
        openFilesAfterCreation = source.openFilesAfterCreation
        showTemplatesContainingSeparatorInNewGroup = source.showTemplatesContainingSeparatorInNewGroup
    }
}