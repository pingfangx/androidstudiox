package com.pingfangx.plugin.legacylayoutinspector

import com.intellij.openapi.fileTypes.FileType
import icons.StudioIcons
import javax.swing.Icon

/**
 * 旧版的在 filetypes.xml 中配置了 removed_mapping 被移除了
 *
 * 是通过 name 匹配的，因此我们再添加一个其他 name 的 FileType 即可
 *
 * 由于 LayoutInspectorFileType 的构造函数是 private，所以我们直接重写一个
 *
 * @author pingfangx
 * @date 2022/6/30
 */
class LegacyLayoutInspectorFileType private constructor() : FileType {
    override fun getName(): String {
        return "Legacy Layout Inspector"
    }

    override fun getDescription(): String {
        return "Legacy Layout Inspector Snapshot"
    }

    override fun getDefaultExtension(): String {
        return EXT_LAYOUT_INSPECTOR
    }

    override fun getIcon(): Icon {
        return StudioIcons.Shell.Menu.LAYOUT_INSPECTOR
    }

    override fun isBinary(): Boolean {
        return true
    }

    override fun isReadOnly(): Boolean {
        return true
    }

    companion object {
        @JvmField
        val INSTANCE = LegacyLayoutInspectorFileType()
        const val EXT_LAYOUT_INSPECTOR = "li"
        const val DOT_EXT_LAYOUT_INSPECTOR = ".li"
    }
}