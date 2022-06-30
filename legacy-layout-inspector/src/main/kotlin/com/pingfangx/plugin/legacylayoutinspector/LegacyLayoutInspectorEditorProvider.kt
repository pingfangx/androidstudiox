package com.pingfangx.plugin.legacylayoutinspector

import com.android.tools.idea.editors.layoutInspector.LayoutInspectorEditor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * 使用 captureType 报错
 * Created extension classloader is not equal to plugin's one.
 *
 * 因些注册新的 FileEditorProvider
 *
 * @author pingfangx
 * @date 2022/6/30
 */
class LegacyLayoutInspectorEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.extension.orEmpty().equals(LegacyLayoutInspectorFileType.EXT_LAYOUT_INSPECTOR, true)
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return LayoutInspectorEditor(project, file)
    }

    override fun getEditorTypeId(): String {
        return "legacy-layout-inspector"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }
}