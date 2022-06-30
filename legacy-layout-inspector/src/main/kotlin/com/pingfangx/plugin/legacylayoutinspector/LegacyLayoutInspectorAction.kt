package com.pingfangx.plugin.legacylayoutinspector

import com.android.tools.idea.editors.layoutInspector.actions.AndroidRunLayoutInspectorAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * 旧版
 *
 * 如果直接新建一个 Action 指向 AndroidRunLayoutInspectorAction
 * 则会在 [update] 中被隐藏
 *
 * 所以需要新建一个类处理
 *
 * @author pingfangx
 * @date 2022/6/30
 */
class LegacyLayoutInspectorAction : AndroidRunLayoutInspectorAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        // 忽略父类设置，总是可见
        e.presentation.isVisible = true
    }
}