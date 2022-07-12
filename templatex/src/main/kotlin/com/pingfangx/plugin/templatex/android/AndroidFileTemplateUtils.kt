package com.pingfangx.plugin.templatex.android

import com.android.tools.idea.wizard.template.impl.other.files.layoutResourceFile.layoutResourceFileTemplate
import com.intellij.ide.fileTemplates.FileTemplate
import com.pingfangx.plugin.templatex.util.TemplateXNameCaseUtils
import org.jetbrains.kotlin.util.prefixIfNot

/**
 * Android 模板工具
 *
 * @author pingfangx
 * @date 2022/7/11
 */
object AndroidFileTemplateUtils {
    const val ATTRIBUTE_ROOT_TAG = "ROOT_TAG"
    const val DEFAULT_ROOT_TAG = "LinearLayout"

    /**
     * 是否支持 Android
     *
     * 即使 IDE 安装了 Android 插件，也必须在 plugin.xml 中声明依赖 org.jetbrains.android 才能解析到类
     */
    val supportAndroid: Boolean by lazy {
        try {
            Class.forName("com.android.tools.idea.wizard.template.Template")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    val androidLayoutResourceFileTemplateName: String by lazy {
        layoutResourceFileTemplate.name
    }
    private const val ANDROID_LAYOUT_RESOURCE_FILE_NAME_PREFIX = "activity_"

    /** 是否是 Android 布局资源文件模板 */
    fun isAndroidLayoutResourceFileTemplate(template: FileTemplate) =
        supportAndroid && (template.isAndroidFileTemplate || template.isXmlFile)

    private val FileTemplate.isAndroidFileTemplate: Boolean
        get() = this is AndroidFileTemplate && name == androidLayoutResourceFileTemplateName

    private val FileTemplate.isXmlFile: Boolean
        get() = extension == "xml"

    /** 获取 Android 布局资源文件名 */
    fun getAndroidLayoutResourceFileName(name: String): String =
        TemplateXNameCaseUtils.toSnakeCase(name).prefixIfNot(ANDROID_LAYOUT_RESOURCE_FILE_NAME_PREFIX)

    /** 获取支持的 Android 模板 */
    fun getSupportedAndroidTemplates(): MutableList<FileTemplate> {
        return mutableListOf<FileTemplate>().apply {
            if (supportAndroid) {
                add(AndroidFileTemplate(layoutResourceFileTemplate))
            }
        }
    }

    /**
     * 默认的变量
     *
     * 也可以扩展 DefaultTemplatePropertiesProvider 来实现
     * 但是是否会影响其他创建模板时的默认属性填充
     */
    private val defaultAttributeMap: Map<String, String>
        get() = mapOf(
            ATTRIBUTE_ROOT_TAG to DEFAULT_ROOT_TAG
        )

    /** 填充变量 */
    fun fillVariables(
        template: FileTemplate, variables: MutableSet<String>, inputtedVariables: MutableMap<String, String>
    ) {
        if (!supportAndroid) {
            return
        }
        if (!isAndroidLayoutResourceFileTemplate(template)) {
            return
        }
        val defaultAttributeMap = defaultAttributeMap
        // 添加布局的相关 key
        variables.addAll(defaultAttributeMap.keys)
        // 填弃默认值
        for (variable in variables) {
            if (!inputtedVariables.contains(variable)) {
                // 如果没有输入，则看是否有默认值
                defaultAttributeMap[variable]?.let { default ->
                    inputtedVariables[variable] = default
                }
            }
        }
    }
}