package com.pingfangx.plugin.util

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project

/**
 * 项目项关
 *
 * @author pingfangx
 * @date 2022/7/12
 */
object ProjectUtils {
    /** 当前项目，可能为 null */
    val currentProject: Project?
        get() {
            val dataContext = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(2000)
            return dataContext?.getData(CommonDataKeys.PROJECT)
        }
}