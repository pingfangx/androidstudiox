package com.pingfangx.plugin.templatex.util

import com.android.tools.idea.wizard.template.impl.other.files.layoutResourceFile.layoutResourceFileTemplate
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate
import com.intellij.openapi.project.guessProjectDir
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.pingfangx.plugin.templatex.android.AndroidFileTemplate
import org.junit.Test
import java.io.File
import java.util.*

/**
 * 工具
 *
 * @author pingfangx
 * @date 2022/7/11
 */
class TemplateXUtilsTest : BasePlatformTestCase() {
    private lateinit var properties: Properties

    override fun setUp() {
        super.setUp()
        properties = Properties()
        properties.setProperty(FileTemplate.ATTRIBUTE_NAME, "Name")
        FileTemplateUtil.fillDefaultProperties(properties, psiManager.findDirectory(project.guessProjectDir()!!)!!)
    }

    @Test
    fun test_getFileNameFromTemplate_nameAndFileName() {

        val template: FileTemplate = CustomFileTemplate("TemplateName", "")
        // 如果没有 fileName 且不包含变量，直接返回输入的文件名
        assertEquals("Name", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.name = "\${NAME}Activity"
        // 如果没有 fileName，包含变量，则解析 name
        assertEquals("NameActivity", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.name = "view\$S\${NAME}Activity"
        // 解析分隔符
        assertEquals("view${File.separator}NameActivity", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.name = "TemplateName"
        template.fileName = "FileName"
        // 如果有 fileName 则优先取 fileName
        assertEquals("FileName", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.name = "\${NAME}Activity"
        template.fileName = "FileName"
        // 如果有 fileName 则优先取 fileName
        assertEquals("FileName", TemplateXUtils.getFileNameFromTemplate(template, properties))


        template.fileName = "view\$S\${NAME}Activity"
        // fileName 也是正常解析的
        assertEquals("view${File.separator}NameActivity", TemplateXUtils.getFileNameFromTemplate(template, properties))
    }

    @Test
    fun test_getFileNameFromTemplate_separator() {
        val template: FileTemplate = CustomFileTemplate("TemplateName", "")
        template.fileName = "Name-FileName"
        assertEquals("FileName", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.fileName = "Name-view\$S\${NAME}Activity"
        assertEquals("view${File.separator}NameActivity", TemplateXUtils.getFileNameFromTemplate(template, properties))
    }

    @Test
    fun test_getFileNameFromTemplate_nameCase() {
        val template: FileTemplate = CustomFileTemplate("TemplateName", "")
        template.fileName = "\${NAME}"
        assertEquals("Name", TemplateXUtils.getFileNameFromTemplate(template, properties))

        properties.setProperty(FileTemplate.ATTRIBUTE_NAME, "ClassName")

        template.fileName = "\${NAME}"
        assertEquals("ClassName", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.fileName = "\${Name}"
        assertEquals("ClassName", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.fileName = "\${UPPERCASENAME}"
        assertEquals("CLASSNAME", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.fileName = "\${lowercasename}"
        assertEquals("classname", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.fileName = "\${snake_case_name}"
        assertEquals("class_name", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.fileName = "\${CONSTANT_CASE_NAME}"
        assertEquals("CLASS_NAME", TemplateXUtils.getFileNameFromTemplate(template, properties))

        template.fileName = "\${camelCaseName}"
        assertEquals("className", TemplateXUtils.getFileNameFromTemplate(template, properties))
    }

    @Test
    fun test_getFileNameFromTemplate_android() {
        val template: FileTemplate = AndroidFileTemplate(layoutResourceFileTemplate)
        assertEquals("activity_name", TemplateXUtils.getFileNameFromTemplate(template, properties))

        properties.setProperty(FileTemplate.ATTRIBUTE_NAME, "ClassName")
        assertEquals("activity_class_name", TemplateXUtils.getFileNameFromTemplate(template, properties))

        properties.setProperty(FileTemplate.ATTRIBUTE_NAME, "activity_test")
        assertEquals("activity_test", TemplateXUtils.getFileNameFromTemplate(template, properties))
    }
}