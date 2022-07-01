package com.pingfangx.plugin.tree.view.helper

import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.AnActionButton
import com.intellij.ui.LayeredIcon
import com.intellij.ui.ToolbarDecorator
import com.pingfangx.plugin.templatex.TemplateXBundle
import com.pingfangx.plugin.tree.model.data.TreeElement
import com.pingfangx.plugin.tree.model.data.TreeGroup
import javax.swing.JComponent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

/**
 * 模板助手
 *
 * @author pingfangx
 * @date 2022/7/4
 */
abstract class AbstractTreeGroupToolbarHelper<Element : TreeElement, Group : TreeGroup<Element>> {
    abstract val project: Project
    abstract val treeHelper: TreeHelper
    abstract val groups: MutableList<Group>
    protected open val elementName = TemplateXBundle.message("common.action.tree.element")
    protected open val groupName = TemplateXBundle.message("common.action.tree.group")
    protected open fun newElement(name: String): Element? = null
    protected open fun copyElement(element: Element): Element? = null
    protected open fun newGroup(name: String): Group? = null

    protected open val showCopyAction = false

    /** 创建工具栏 */
    fun createToolbar(): JComponent {
        return createToolbarDecorator().createPanel()
    }

    protected open fun createToolbarDecorator() =
        ToolbarDecorator.createDecorator(treeHelper.tree).setToolbarPosition(ActionToolbarPosition.TOP)
            // 添加
            .setAddAction { addElementOrGroup(it) }
            .setAddIcon(LayeredIcon.ADD_WITH_DROPDOWN)
            // 移除
            .setRemoveAction { removeElementOrGroup() }
            .apply {
                if (showCopyAction) {
                    addExtraAction(object :
                        AnActionButton(ActionsBundle.message("action.EditorCopy.text"), AllIcons.Actions.Copy) {
                        override fun actionPerformed(e: AnActionEvent) {
                            val selectedElement = singleSelectedElement
                            if (selectedElement != null) {
                                copyElement(selectedElement)?.let { copyElement ->
                                    addElement(copyElement, getGroupByElement(selectedElement))
                                }
                            }
                        }

                        override fun updateButton(e: AnActionEvent) {
                            e.presentation.isEnabled = singleSelectedElement != null
                        }
                    })
                }
            }

    /** 添加组或元素 */
    private fun addElementOrGroup(button: AnActionButton) {
        val group = DefaultActionGroup().apply {
            add(object : DumbAwareAction(
                TemplateXBundle.message(
                    "common.action.title.add.format",
                    elementName
                )
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    addElement(singleSelectedGroup)
                }
            })
            add(object : DumbAwareAction(
                TemplateXBundle.message(
                    "common.action.title.add.format",
                    groupName
                )
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    addGroup()
                }
            })
            configAddGroupOrElementActionGroup(this)
        }
        val context = DataManager.getInstance().getDataContext(button.contextComponent)
        val popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(null, group, context, JBPopupFactory.ActionSelectionAid.ALPHA_NUMBERING, true, null)
        popup.show(button.preferredPopupPoint!!)
    }

    protected open fun configAddGroupOrElementActionGroup(actionGroup: DefaultActionGroup) {}

    protected open fun addElement(group: Group? = null) {
        Messages.showInputDialog(
            treeHelper.tree,
            TemplateXBundle.message(
                "common.dialog.message.enter.the.new.name.format",
                elementName.lowercase()
            ),
            TemplateXBundle.message(
                "common.dialog.title.create.new.format",
                elementName
            ),
            null
        )?.let { addElement(it, group) }
    }

    protected open fun addElement(name: String, group: Group? = null) =
        addElement(requireNotNull(newElement(name)), group)

    protected open fun addElement(element: Element, group: Group? = null) {
        val groupPromise = group ?: groups.firstOrNull() ?: return
        val index = groupPromise.elements.size
        groupPromise.elements.add(element)

        treeHelper.insertNewElement(groupPromise, element, index)
    }

    /** 添加组 */
    protected open fun addGroup() {
        Messages.showInputDialog(
            treeHelper.tree,
            TemplateXBundle.message(
                "common.dialog.message.enter.the.new.name.format",
                groupName.lowercase()
            ),
            TemplateXBundle.message(
                "common.dialog.title.create.new.format",
                groupName
            ),
            null
        )?.let { addGroup(it) }
    }

    protected open fun addGroup(name: String) =
        addGroup(requireNotNull(newGroup(name)))

    protected open fun addGroup(group: Group) {
        // 添加进数据
        val index = groups.size
        groups.add(group)
        // 更新 UI
        treeHelper.insertNewGroup(group, index)
    }

    /** 移除组或元素 */
    private fun removeElementOrGroup() {
        var toSelect: TreeNode? = null

        val paths: Array<TreePath> = treeHelper.tree.selectionPaths ?: return

        for (path in paths) {
            val node = path.lastPathComponent as DefaultMutableTreeNode
            when (val o = node.userObject) {
                is TreeGroup<*> -> {
                    @Suppress("UNCHECKED_CAST") // it should be
                    groups.remove(o as Group)
                    treeHelper.removeNodeFromParent(node)
                }
                is TreeElement -> {
                    getGroupByElement(o)?.let { templateGroup ->
                        templateGroup.elements.remove(o)
                        toSelect = (node.parent as DefaultMutableTreeNode).getChildAfter(node)
                        treeHelper.removeNodeFromParent(node)
                    }
                }
            }
        }

        if (toSelect is DefaultMutableTreeNode) {
            treeHelper.setSelectedNode(toSelect as DefaultMutableTreeNode?)
        }
    }

    private val singleSelectedIndex: Int
        get() {
            val rows = treeHelper.tree.selectionRows
            return if (rows != null && rows.size == 1) rows[0] else -1
        }
    private val hasSingleSelected: Boolean
        get() = singleSelectedIndex != -1

    private val singleSelectedGroup: Group?
        get() {
            val path = treeHelper.tree.getPathForRow(singleSelectedIndex)
            return when (val userObject = (path.lastPathComponent as DefaultMutableTreeNode).userObject) {
                // 使用选中的组
                is TreeGroup<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    userObject as Group
                }
                // 使用元素所处提组
                is TreeElement -> getGroupByElement(userObject)
                else -> null
            }
        }
    private val singleSelectedElement: Element?
        get() {
            val path = treeHelper.tree.getPathForRow(singleSelectedIndex)
            val userObject = (path.lastPathComponent as DefaultMutableTreeNode).userObject
            if (userObject is TreeElement) {
                @Suppress("UNCHECKED_CAST")
                return userObject as? Element
            } else {
                return null
            }
        }

    protected fun getGroupByElement(element: TreeElement): Group? {
        for (group in groups) {
            if (group.elements.any { it == element }) {
                return group
            }
        }
        return null
    }

}