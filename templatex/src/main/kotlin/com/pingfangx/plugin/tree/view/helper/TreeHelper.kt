package com.pingfangx.plugin.tree.view.helper

import com.intellij.ui.CheckboxTree
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.tree.TreeUtil
import com.pingfangx.plugin.tree.model.data.TreeElement
import com.pingfangx.plugin.tree.model.data.TreeGroup
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

/**
 * 管理树相关
 *
 * 主要管理创建 Node
 *
 * 参考 TemplateListPanel 中的相关操作
 *
 * @author pingfangx
 * @date 2022/7/4
 */
class TreeHelper(
    var tree: JTree,
    private val onTreeSelectionChanged: ((userObject: Any?) -> Unit)? = null,
) {

    private val treeRoot = CheckedTreeNode()

    /** 初始化树 */
    fun initTree(groups: MutableList<out TreeGroup<*>>) {
        treeRoot.removeAllChildren()
        for (group in groups) {
            val groupNode = CheckedTreeNode(group)
            groupNode.isChecked = false
            treeRoot.add(groupNode) // add group
            for (element in group.elements) {
                val elementNode = CheckedTreeNode(element)
                elementNode.isChecked = element.checked
                groupNode.add(elementNode) // add elements into group
            }
        }
        tree = object : CheckboxTree(object : CheckboxTreeCellRenderer() {
            override fun customizeRenderer(
                tree: JTree?,
                value: Any?,
                selected: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ) {
                val userObject = (value as? DefaultMutableTreeNode)?.userObject ?: return
                when (userObject) {
                    is TreeElement -> {
                        textRenderer.append(userObject.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    }
                    is TreeGroup<*> -> {
                        textRenderer.append(userObject.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
                    }
                }
            }
        }, treeRoot) {
            override fun onNodeStateChanged(node: CheckedTreeNode?) {
                super.onNodeStateChanged(node)
                node ?: return
                // 只需要判断 TreeElement，TreeGroup 会相应触发 TreeElement
                (node.userObject as? TreeElement)?.checked = node.isChecked
            }
        }
        tree.addTreeSelectionListener {
            onTreeSelectionChanged?.invoke((tree.lastSelectedPathComponent as CheckedTreeNode).userObject)
        }
        expandAll()
    }

    /** 插件新的组 */
    fun insertNewGroup(newGroup: TreeGroup<*>, index: Int) {
        val groupNode = CheckedTreeNode(newGroup)
        groupNode.isChecked = false // 添加组默认应该不选中
        treeRoot.add(groupNode)

        (tree.model as DefaultTreeModel).nodesWereInserted(treeRoot, intArrayOf(index))

        setSelectedNode(groupNode)
    }

    /** 插入新的元素 */
    fun insertNewElement(group: TreeGroup<*>, element: TreeElement, index: Int) {
        val node = CheckedTreeNode(element)
        node.isChecked = true // 添加元素默认应该选中
        var child: DefaultMutableTreeNode? = treeRoot.firstChild as? DefaultMutableTreeNode
        while (child != null) {
            if ((child.userObject as? TreeGroup<*>)?.name == group.name) {
                // add node
                child.add(node)
                // notify update
                (tree.model as DefaultTreeModel).nodesWereInserted(child, intArrayOf(index))
                // select
                setSelectedNode(node)
            }
            child = treeRoot.getChildAfter(child) as? DefaultMutableTreeNode
        }
    }

    fun setSelectedNode(node: DefaultMutableTreeNode?) {
        node ?: return
        TreeUtil.selectPath(tree, TreePath(node.path))
    }

    fun removeNodeFromParent(node: DefaultMutableTreeNode) {
        val parent = node.parent
        val idx = parent.getIndex(node)
        node.removeFromParent()
        (tree.model as DefaultTreeModel).nodesWereRemoved(parent, intArrayOf(idx), arrayOf<TreeNode>(node))
    }

    fun expandAll() {
        for (i in 0 until tree.rowCount) {
            tree.expandRow(i)
        }
    }
}