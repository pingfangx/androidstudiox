package com.pingfangx.plugin.templatex.view.ui

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.impl.FileTemplateBase
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiDirectory
import com.intellij.ui.*
import com.intellij.ui.components.JBLabel
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBUI
import com.pingfangx.plugin.templatex.TemplateXBundle
import com.pingfangx.plugin.templatex.android.AndroidFileTemplateUtils
import com.pingfangx.plugin.templatex.model.data.TemplateXConfigData
import com.pingfangx.plugin.templatex.util.TemplateXUtils
import com.pingfangx.plugin.tree.view.helper.TreeHelper
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.event.DocumentEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

/**
 * 模板的管理
 *
 * @author pingfangx
 * @date 2022/7/5
 */
class TemplateXPanel(
    private val project: Project,
    /**
     * 原始配置
     *
     * 用于 [resetConfig] 时还原
     * [apply] 时设置
     * [isModified] 判断是否修改
     */
    private val originConfig: TemplateXConfigData,
    /**
     * 选中的目录
     * 如果是在设置中，则为 null
     */
    private val selectedDir: PsiDirectory? = null,
) : JPanel(BorderLayout()) {
    /** 所有的模版，在 [initComponents] 时更新 */
    private val allFileTemplates = mutableListOf<FileTemplate>()

    /**
     * 各变量都需要持有一份，并在控件变化时进行更新
     *
     * 如果不持有，以控件为主，则在重新布局时，会丢失已修改但未保存的配置
     */
    private val currentConfig = TemplateXConfigData()

    /** 置认的属性 */
    private val defaultProperties by lazy {
        FileTemplateManager.getInstance(project).defaultProperties.also {
            if (selectedDir != null) {
                FileTemplateUtil.fillDefaultProperties(it, selectedDir)
            }
        }
    }

    /** 是否处理创建模板的对话框模式 */
    private val createTemplatesDialogMode: Boolean
        get() = selectedDir != null

    /** 已输入的属性 */
    private val inputtedVariables = mutableMapOf<String, String>()

    /** 用于只更新某一部分 component */
    private lateinit var splitter: Splitter

    /** 操作 tree */
    private lateinit var treeHelper: TreeHelper

    /** 请求焦点 */
    var preferredFocusedComponent: JComponent? = null

    /** 所有选中的模板 */
    val selectionTemplates: List<FileTemplate>
        get() = allFileTemplates.filter { currentConfig.selectionTemplateNames.contains(it.name) }

    /** 提供的属性 */
    val properties: Properties
        get() = Properties(defaultProperties).apply {
            putAll(inputtedVariables)
        }

    /** 是否在创建后打开文件  */
    val openFilesAfterCreation: Boolean
        get() = currentConfig.openFilesAfterCreation

    /** 是否有修改 */
    val isModified: Boolean
        // 直接用 config 进行比较，但是为了避免 filter 的耗时，以及模板发生变化导致 name 不匹配模板的情况，不需要过滤
        get() = originConfig != currentConfig

    init {
        resetConfig()
        initComponents()
    }

    // region 布局相关

    private fun resetConfig() {
        currentConfig.update(originConfig)
    }

    private fun initComponents() {
        allFileTemplates.clear()
        allFileTemplates.addAll(TemplateXUtils.getFileTemplates(project))
        splitter = Splitter().apply {
            firstComponent = createChooseTemplatePanel()
            secondComponent = createVariablesPanel()
        }
        add(splitter, BorderLayout.CENTER)
    }

    private fun refreshComponents() {
        removeAll()
        initComponents()
        revalidate()
        repaint()
    }

    private fun createChooseTemplatePanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(JBLabel(TemplateXBundle.message("template.choose.template")), BorderLayout.PAGE_START)

            val treeRoot = CheckedTreeNode()
            // 过滤已忽略的
            val filterTemplates = allFileTemplates.filter { !currentConfig.ignoredTemplateNames.contains(it.name) }
            for (template in filterTemplates) {
                val treeNode = CheckedTreeNode(template)
                treeNode.isChecked = currentConfig.selectionTemplateNames.contains(template.name)
                treeRoot.add(treeNode)
            }
            val tree = object : CheckboxTree(object : CheckboxTreeCellRenderer() {
                override fun customizeRenderer(
                    tree: JTree?,
                    value: Any?,
                    selected: Boolean,
                    expanded: Boolean,
                    leaf: Boolean,
                    row: Int,
                    hasFocus: Boolean
                ) {
                    val template = (value as? DefaultMutableTreeNode)?.userObject as? FileTemplate ?: return
                    if (FileTemplateBase.isChild(template)) {
                        textRenderer.append(template.fileName.ifEmpty {
                            IdeBundle.message("label.empty.file.name")
                        })
                        textRenderer.border = JBUI.Borders.emptyLeft(JBUI.scale(20))
                    } else {
                        textRenderer.border = null
                        textRenderer.append(template.name)
                    }
                }
            }, treeRoot) {
                override fun onNodeStateChanged(node: CheckedTreeNode?) {
                    super.onNodeStateChanged(node)
                    val template = node?.userObject as? FileTemplate ?: return
                    if (node.isChecked) {
                        currentConfig.selectionTemplateNames.add(template.name)
                    } else {
                        currentConfig.selectionTemplateNames.remove(template.name)
                    }
                    refreshVariablesPanel()
                }
            }

            treeHelper = TreeHelper(tree)

            TreeSpeedSearch(tree)

            add(createToolbar(tree), BorderLayout.CENTER)
        }
    }

    private fun createToolbar(tree: JTree) = ToolbarDecorator.createDecorator(tree)
        .setToolbarPosition(ActionToolbarPosition.TOP)
        .setAddAction { addElement(it) }
        .setAddIcon(LayeredIcon.ADD_WITH_DROPDOWN)
        .setRemoveAction { removeElement(tree) }
        .addExtraAction(object : AnActionButton(
            TemplateXBundle.message("template.action.show.templates.settings"),
            @Suppress("DialogTitleCapitalization") // messageWithSentenceCase
            (TemplateXBundle.messageWithSentenceCase("template.action.show.templates.settings")),
            AllIcons.General.GearPlain
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                TemplateXUtils.configureTemplatesDialog(project).showAndGet()
                // 点 apply 也会触发变化，所以不判断 showAndGet()
                refreshComponents()
            }
        })
        .createPanel()

    private fun addElement(button: AnActionButton) {
        val group = DefaultActionGroup().apply {
            val ignoredTemplates = allFileTemplates.filter { currentConfig.ignoredTemplateNames.contains(it.name) }
            for (template in ignoredTemplates) {
                add(object : AnAction(template.name) {
                    override fun actionPerformed(e: AnActionEvent) {
                        // 因为不确定添加的位置，索性移除后重新刷新
                        currentConfig.ignoredTemplateNames.remove(template.name)
                        refreshComponents()
                    }
                })
            }
        }
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                null,
                group,
                button.dataContext,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true,
            )
        popup.show(button.preferredPopupPoint!!)
    }

    private fun removeElement(tree: JTree) {
        val paths: Array<TreePath> = tree.selectionPaths ?: return

        var toSelect: TreeNode? = null
        for (path in paths) {
            val node = path.lastPathComponent as DefaultMutableTreeNode
            val fileTemplate = node.userObject as FileTemplate
            currentConfig.selectionTemplateNames.remove(fileTemplate.name) // 移除则不再选中
            currentConfig.ignoredTemplateNames.add(fileTemplate.name)

            toSelect = (node.parent as DefaultMutableTreeNode).getChildAfter(node)
            treeHelper.removeNodeFromParent(node)
        }
        if (toSelect is DefaultMutableTreeNode) {
            treeHelper.setSelectedNode(toSelect as DefaultMutableTreeNode?)
        }
    }

    private fun createVariablesPanel() = panel {
        titledRow(TemplateXBundle.message("template.plugin.config")) {
            row {
                checkBox(
                    TemplateXBundle.message("template.open.files.after.creation"),
                    { currentConfig.openFilesAfterCreation },
                    {})
                    .component.addItemListener {
                        currentConfig.openFilesAfterCreation = it.stateChange == ItemEvent.SELECTED
                    }
            }
            row {
                checkBox(
                    TemplateXBundle.message("template.show.templates.containing.separator.in.new.group"),
                    { currentConfig.showTemplatesContainingSeparatorInNewGroup },
                    {}
                ).component.addItemListener {
                    currentConfig.showTemplatesContainingSeparatorInNewGroup = it.stateChange == ItemEvent.SELECTED
                }
            }
        }
        if (createTemplatesDialogMode) {
            titledRow(TemplateXBundle.message("template.specify.variables")) {
                val unsetVariables = mutableSetOf<String>()
                if (currentConfig.selectionTemplateNames.isEmpty()) {
                    row {
                        label(TemplateXBundle.message("template.please.select.templates.from.left"))
                    }
                } else {
                    // 首先添加 NAME，那其排在最前
                    unsetVariables.add(FileTemplate.ATTRIBUTE_NAME)
                    for (templateName in currentConfig.selectionTemplateNames) {
                        val template = allFileTemplates.find { it.name == templateName } ?: continue
                        unsetVariables.addAll(template.getUnsetAttributes(defaultProperties, project))
                    }
                    for (variable in unsetVariables) {
                        row(variable) {
                            val textField = textField({ inputtedVariables.getOrDefault(variable, "") }, {}).component
                            if (variable == FileTemplate.ATTRIBUTE_NAME) {
                                preferredFocusedComponent = textField
                            }
                            textField.document.addDocumentListener(object : DocumentAdapter() {
                                override fun textChanged(e: DocumentEvent) {
                                    inputtedVariables[variable] = textField.text
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    private fun refreshVariablesPanel() {
        splitter.secondComponent = createVariablesPanel()
        splitter.revalidate()
        splitter.repaint()
    }

    // endregion

    // region 公共方法相关

    /** 新输入的配置 */
    private fun filterCurrentConfig(): TemplateXConfigData {
        val predicate: (String) -> Boolean = { name ->
            // 找到了名称对应的模板，名称才有效
            allFileTemplates.find { it.name == name } != null
        }
        return TemplateXConfigData(
            selectionTemplateNames = currentConfig.selectionTemplateNames.filter(predicate).toMutableSet(),
            ignoredTemplateNames = currentConfig.ignoredTemplateNames.filter(predicate).toMutableSet(),
            openFilesAfterCreation = currentConfig.openFilesAfterCreation,
            showTemplatesContainingSeparatorInNewGroup = currentConfig.showTemplatesContainingSeparatorInNewGroup
        )
    }

    fun reset() {
        resetConfig()
        refreshComponents()
    }

    fun apply() {
        originConfig.update(filterCurrentConfig())
    }

    fun applyAndClose(): Boolean {
        apply()
        if (currentConfig.selectionTemplateNames.isEmpty()) {
            return true
        }

        val fileName = inputtedVariables.getOrDefault(FileTemplate.ATTRIBUTE_NAME, "")
        if (fileName.isEmpty()) {
            val inputFileName = Messages.showInputDialog(
                IdeBundle.message("error.please.enter.a.file.name"),
                CommonBundle.getErrorTitle(),
                null
            )
            return if (inputFileName.isNullOrEmpty()) {
                false
            } else {
                inputtedVariables[FileTemplate.ATTRIBUTE_NAME] = inputFileName
                true
            }
        }
        return true
    }
    // endregion
}