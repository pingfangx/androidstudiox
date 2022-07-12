package com.pingfangx.plugin.templatex.android

import com.android.tools.idea.wizard.template.Template
import com.intellij.ide.fileTemplates.impl.FileTemplateBase

/**
 * Android 中的 template
 *
 * @author pingfangx
 * @date 2022/7/11
 */
class AndroidFileTemplate(
    private val template: Template,
    private var name: String = template.name,
    private var extension: String = "",
) : FileTemplateBase() {
    override fun getName(): String = name
    override fun setName(name: String) {
        this.name = name
    }

    override fun isDefault(): Boolean = false

    override fun getDescription(): String = ""

    override fun getExtension(): String = extension

    override fun setExtension(extension: String) {
        this.extension = extension
    }
}