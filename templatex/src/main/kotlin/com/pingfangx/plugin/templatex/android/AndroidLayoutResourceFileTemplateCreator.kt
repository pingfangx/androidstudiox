package com.pingfangx.plugin.templatex.android

import com.android.tools.idea.model.AndroidModel
import com.android.tools.idea.npw.model.ProjectSyncInvoker
import com.android.tools.idea.npw.model.RenderTemplateModel
import com.android.tools.idea.npw.project.getModuleTemplates
import com.android.tools.idea.npw.project.getPackageForApplication
import com.android.tools.idea.npw.project.getPackageForPath
import com.android.tools.idea.npw.template.TemplateResolver
import com.android.tools.idea.templates.recipe.DefaultRecipeExecutor
import com.android.tools.idea.templates.recipe.RenderingContext
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.impl.other.files.layoutResourceFile.res.layoutXml
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
import java.io.File
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

    /**
     * com.android.tools.idea.npw.model.RenderTemplateModel$TemplateRenderer#renderTemplate
     *
     * 但是执行时报错 java.lang.NoSuchMethodError: com.android.tools.idea.npw.model.RenderTemplateModel.getModuleTemplateDataBuilder()Lcom/android/tools/idea/templates/ModuleTemplateDataBuilder;
     * 手动创建报错 java.lang.NoClassDefFoundError: com/android/tools/idea/templates/ModuleTemplateDataBuilder
     * 注意 com.android.tools.idea.npw.model.RenderTemplateModel.TemplateRenderer.renderTemplate 本身就写着 FIX ME
     */
    private fun RenderTemplateModel.renderTemplate(
        layoutName: String,
        rootTag: String,
    ): File? {
        val paths = template.get().paths
        paths.moduleRoot ?: return null
        val context = RenderingContext(
            project = project,
            module = module,
            commandName = commandName,
            templateData = this.moduleTemplateDataBuilder.build(),
            moduleRoot = paths.moduleRoot!!,
            dryRun = false,
            showErrors = true
        )
        val executor = DefaultRecipeExecutor(context)
        val resOut = context.moduleTemplateData?.resDir ?: return null
        context.moduleTemplateData?.let {
            executor.layoutResourceFileRecipe(it, layoutName, rootTag)
            return resOut.resolve("layout/${layoutName}.xml")
        } ?: return null
    }

    /**
     * wizard/template-impl/src/com/android/tools/idea/wizard/template/impl/other/files/layoutResourceFile/layoutResourceFileRecipe.kt
     * com.android.tools.idea.wizard.template.impl.other.files.layoutResourceFile.layoutResourceFileTemplate 赋值 recipe
     * 执行时调用 layoutResourceFileRecipe
     * Recipe 定义于 wizard/template-plugin/src/com/android/tools/idea/wizard/template/Template.kt
     * typealias Recipe = RecipeExecutor.(TemplateData) -> Unit
     */
    private fun RecipeExecutor.layoutResourceFileRecipe(
        moduleData: ModuleTemplateData,
        layoutName: String,
        rootTag: String,
    ) {
        val resOut = moduleData.resDir
        save(layoutXml(rootTag), moduleData.resDir.resolve("layout/${layoutName}.xml"))
        open(resOut.resolve("layout/${layoutName}.xml"))
    }
}