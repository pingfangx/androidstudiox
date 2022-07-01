package com.pingfangx.plugin.templatex.model.data

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 持久化数据
 *
 * @author pingfangx
 * @date 2022/7/4
 */
@State(
    name = "templatex",
    storages = [Storage(value = "templatex.xml")],
)
class TemplateXStateService : PersistentStateComponent<TemplateXStateService> {
    var config = TemplateXConfigData()

    override fun getState(): TemplateXStateService {
        return this
    }

    override fun loadState(state: TemplateXStateService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project) = project.getService(TemplateXStateService::class.java)
    }
}