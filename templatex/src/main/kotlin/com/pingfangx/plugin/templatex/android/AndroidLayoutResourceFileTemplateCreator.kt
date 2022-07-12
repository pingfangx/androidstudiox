package com.pingfangx.plugin.templatex.android

import com.android.tools.idea.model.AndroidModel
import com.android.tools.idea.npw.model.ProjectSyncInvoker
import com.android.tools.idea.npw.model.RenderTemplateModel
import com.android.tools.idea.npw.project.getModuleTemplates
import com.android.tools.idea.npw.project.getPackageForApplication
import com.android.tools.idea.npw.project.getPackageForPath
import com.android.tools.idea.npw.template.TemplateResolver
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.WizardUiContext
import com.google.wireless.android.sdk.stats.AndroidStudioEvent
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.pingfangx.plugin.templatex.creator.TemplateCreator
import com.pingfangx.plugin.templatex.util.TemplateXUtils
import org.jetbrains.android.facet.AndroidFacet
import java.util.*

/**
 * Android 布局资源文件
 *
 * @author pingfangx
 * @date 2022/7/6
 */
class AndroidLayoutResourceFileTemplateCreator(
    private val dataContext: DataContext,
) : TemplateCreator {
    private val commandName: String = ""
    override fun create(template: FileTemplate, properties: Properties, dir: PsiDirectory): PsiElement? {
        try {
            // com.android.tools.idea.actions.NewAndroidComponentAction.actionPerformed
            val module = dataContext.getData(LangDataKeys.MODULE) ?: return null
            val facet = AndroidFacet.getInstance(module) ?: return null
            if (AndroidModel.get(facet) == null) {
                return null
            }
            var targetDirectory = CommonDataKeys.VIRTUAL_FILE.getData(dataContext)
            // If the user selected a simulated folder entry (eg "Manifests"), there will be no target directory
            if (targetDirectory != null && !targetDirectory.isDirectory) {
                targetDirectory = targetDirectory.parent
                assert(targetDirectory != null)
            }

            val moduleTemplates = facet.getModuleTemplates(targetDirectory)
            assert(moduleTemplates.isNotEmpty())
            val initialPackageSuggestion = if (targetDirectory == null) {
                facet.getPackageForApplication()
            } else {
                facet.getPackageForPath(moduleTemplates, targetDirectory)
            }
            val templateModel = RenderTemplateModel.fromFacet(
                facet,
                initialPackageSuggestion,
                moduleTemplates[0],
                commandName,
                ProjectSyncInvoker.DefaultProjectSyncInvoker(),
                true,
                AndroidStudioEvent.TemplatesUsage.TemplateComponent.WizardUiContext.MENU_GALLERY
            )
            var newActivity = TemplateResolver.getAllTemplates()
                .filter { WizardUiContext.MenuEntry in it.uiContexts }
                .find { it.name == template.name }
            newActivity = newActivity ?: run {
                // 如果模板名不匹配，则获取为 Android 布局资源文件
                TemplateResolver.getAllTemplates()
                    .filter { WizardUiContext.MenuEntry in it.uiContexts }
                    .find { it.name == AndroidFileTemplateUtils.androidLayoutResourceFileTemplateName }
            }
            templateModel.newTemplate = newActivity!!

            val layoutName = TemplateXUtils.getFileNameFromTemplate(template, properties)
            val rootTag =
                properties.getProperty(
                    AndroidFileTemplateUtils.ATTRIBUTE_ROOT_TAG,
                    AndroidFileTemplateUtils.DEFAULT_ROOT_TAG
                )

            // 可以直接执行 templateModel.handleFinished()
            for (widget in templateModel.newTemplate.widgets) {
                val stringParameter = (widget as? TextFieldWidget)?.p ?: continue
                when {
                    stringParameter.name.contains("Layout File Name") -> stringParameter.value = layoutName
                    stringParameter.name.contains("Root Tag") -> stringParameter.value = rootTag
                }
            }
            templateModel.handleFinished()
            return null // 会自动打开，所以返回 null
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }
}