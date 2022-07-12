package com.pingfangx.plugin.templatex.util

import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.ui.ConfigureTemplatesDialog
import com.intellij.openapi.project.Project
import com.pingfangx.plugin.templatex.android.AndroidFileTemplateUtils
import java.io.File
import java.util.*

/**
 * 模板工具
 *
 * @author pingfangx
 * @date 2022/7/5
 */
object TemplateXUtils {
    /**
     * 除子子模板，普通模板不可以指定 fileName，创建时将直接使用输入的 NAME + 扩展名 作为文件名
     * 但是我们需要创建到指定目录，因此需要 fileName
     * 但是插件提供的模板又支持子模板（因为相关信息属于模板设置模块的配置，存储于 file.template.settings.xml）
     *
     * 因此我们在 NAME 中使用 ${S} 来作为目录分隔符
     *
     * 在 createFile 时将其填充到 properties 中，然后解析出文件名
     *
     * 注意只有 TemplateX 插件创建时会解析
     * 如果是 New 菜单创建，则会直接使用输入的 NAME + 扩展名 作为文件名，不会进行解析
     */
    private const val ATTRIBUTE_FILE_SEPARATOR = "S"

    /** 变量符号 */
    private const val VARIABLE_CHAR = '$'

    /**
     * 模板名和文件名的分隔符
     * 模板名可以方便对模板进行分组
     *
     * 原本计划放到 TemplateXConstants 中
     * 是否应该放到 TemplateUtils 中，不应该，工具应该是独立于库的，但是可以把相关方法也提取为工具方法，就可以放到 TemplateUtils
     * 是否应该放到 TemplateCreator 伴生对象中，不应该，除了 Creator，其他业务也会使用
     */
    private const val TEMPLATE_NAME_AND_FILE_NAME_DELIMITER = "-"
    fun getFileTemplates(project: Project): Array<FileTemplate> {
        val templateManager = FileTemplateManager.getInstance(project)
        return templateManager.getTemplates(FileTemplateManager.DEFAULT_TEMPLATES_CATEGORY)
    }

    /**
     * 显示模版设置
     *
     * com.intellij.ide.actions.EditFileTemplatesAction.actionPerformed
     */
    fun configureTemplatesDialog(project: Project) =
        ConfigureTemplatesDialog(project)

    /** 是否包含变量 */
    fun containsVariable(templateName: String): Boolean =
        templateName.contains(VARIABLE_CHAR)

    /** 从模板获取文件名 */
    fun getFileNameFromTemplate(template: FileTemplate, properties: Properties): String {
        var fileName = template.fileName
        if (fileName.isEmpty()) {
            // 名件名为空，取模板名
            if (template.name.contains(VARIABLE_CHAR)) {
                // 如果模板名包含变量，说明需要解析模板名，取模板名
                fileName = template.name
            } else {
                // 模板名不包含变量，说明要取输入的文件名
                fileName = properties.getProperty(FileTemplate.ATTRIBUTE_NAME)
                if (AndroidFileTemplateUtils.isAndroidLayoutResourceFileTemplate(template)) {
                    fileName = AndroidFileTemplateUtils.getAndroidLayoutResourceFileName(fileName)
                }
                return fileName
            }
        }
        // 如果有分隔符，则取最后一段
        if (fileName.contains(TEMPLATE_NAME_AND_FILE_NAME_DELIMITER)) {
            fileName = fileName.split(TEMPLATE_NAME_AND_FILE_NAME_DELIMITER).last()
        }
        // 如果有变量，则进行解析
        if (fileName.contains(VARIABLE_CHAR)) {
            TemplateXNameCaseUtils.appendCaseName(fileName, properties)
            // 如果有变量，则进行填充
            fileName = FileTemplateUtil.mergeTemplate(properties, fileName, false)
        }
        return fileName
    }

    /** 填充 File.separator */
    fun fillSeparator(properties: Properties) {
        properties.setProperty(ATTRIBUTE_FILE_SEPARATOR, File.separator)
    }

    /**
     * 是否是布尔值变量
     */
    fun isBooleanVariable(variable: String): Boolean {
        return variable.startsWith("ADD_")
                || variable.startsWith("USE_")
                || variable.startsWith("IS_")
                || variable.startsWith("IF_")
    }
}