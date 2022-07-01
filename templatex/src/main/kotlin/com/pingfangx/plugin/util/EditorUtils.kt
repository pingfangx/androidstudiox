package com.pingfangx.plugin.util

import com.intellij.ide.impl.ProjectViewSelectInPaneTarget
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

/**
 * 编辑器相关工具
 *
 * 参考 com.android.tools.idea.templates.TemplateUtils
 * 但是不想依赖 android
 *
 * @author pingfangx
 * @date 2022/7/7
 */
object EditorUtils {
    fun openEditors(project: Project, files: List<VirtualFile>, select: Boolean = false) {
        var last: VirtualFile? = null
        for (file in files) {
            last = file
            openEditor(project, file)
        }
        if (select) {
            last?.let { selectEditor(project, it) }
        }
    }

    fun openEditor(project: Project, vFile: VirtualFile) {
        val descriptor = OpenFileDescriptor(project, vFile)
        FileEditorManager.getInstance(project).openEditor(descriptor, true)
    }

    fun selectEditor(project: Project, file: VirtualFile) {
        ApplicationManager.getApplication().assertReadAccessAllowed()

        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
        val currentPane = ProjectView.getInstance(project).currentProjectViewPane ?: return

        ProjectViewSelectInPaneTarget(project, currentPane, true).select(psiFile, false)
    }
}