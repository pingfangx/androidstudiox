package com.pingfangx.plugin.tree.model.data

/**
 * 用于树的数据
 *
 * @author pingfangx
 * @date 2022/7/5
 */

interface TreeGroup<E : TreeElement> {
    val name: String
    val elements: MutableList<E>
}

interface TreeElement {
    val name: String
    var checked: Boolean
}