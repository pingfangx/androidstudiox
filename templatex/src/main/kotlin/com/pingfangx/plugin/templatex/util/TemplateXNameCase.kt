package com.pingfangx.plugin.templatex.util

import com.intellij.ide.fileTemplates.FileTemplate
import java.util.*

/**
 * 命名风格
 *
 * @author pingfangx
 * @date 2022/7/11
 */
enum class TemplateXNameCase(
    val title: String,
    val transform: (String) -> String,
) {
    NORMAL_CASE("Name", { it }),
    UPPER_CASE("UPPERCASENAME", { it.uppercase() }),
    LOWER_CASE("lowercasename", { it.lowercase() }),
    SNAKE_CASE("snake_case_name", { TemplateXNameCaseUtils.toSnakeCase(it) }),
    CONSTANT_CASE("CONSTANT_CASE_NAME", { TemplateXNameCaseUtils.toConstantCase(it) }),
    CAMEL_CASE("camelCaseName", { TemplateXNameCaseUtils.toCamelCase(it) }),
    ;
}

object TemplateXNameCaseUtils {
    fun toSnakeCase(string: String): String =
        toSnakeOrConstantCase(string) { it.lowercaseChar() }

    fun toConstantCase(string: String): String =
        toSnakeOrConstantCase(string) { it.uppercaseChar() }

    fun toSnakeOrConstantCase(string: String, transform: (Char) -> Char): String {
        val builder = StringBuilder()
        if (string.isNotEmpty()) {
            builder.append(transform(string[0]))
            for (i in 1 until string.length) {
                val c = string[i]
                if (Character.isUpperCase(c)) {
                    builder.append("_")
                }
                builder.append(transform(c))
            }
        }
        return builder.toString()
    }

    fun toCamelCase(string: String): String =
        string.replaceFirstChar { it.lowercaseChar() }

    /** 添加不同风格的名字 */
    fun appendCaseName(fileName: String, properties: Properties) {
        val name = properties.getProperty(FileTemplate.ATTRIBUTE_NAME)
        for (case in TemplateXNameCase.values()) {
            if (fileName.contains(case.title)) {
                // 如果包含某个模式，则将转换后的结果放入变量中
                properties.setProperty(case.title, case.transform(name))
            }
        }
    }
}