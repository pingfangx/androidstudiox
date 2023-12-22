/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.editors.layoutInspector.actions

import com.android.tools.analytics.UsageTracker
import com.android.tools.idea.editors.layoutInspector.ui.ViewNodeActiveDisplay
import com.google.wireless.android.sdk.stats.AndroidStudioEvent
import com.google.wireless.android.sdk.stats.LayoutInspectorEvent
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Image
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.JComponent

private fun getLayoutInspectorIcon(iconName: String): Icon? {
  return try {
    // Hedgehog
    val clazz = Class.forName("icons.StudioIcons\$LayoutInspector")
    val field = clazz.getField(iconName)
    field.get(null) as? Icon
  } catch (ignored: Throwable) {
    // Iguana
    try {
      val clazz = Class.forName("icons.StudioIcons\$LayoutInspector\$Toolbar")
      val field = clazz.getField(iconName)
      field.get(null) as? Icon
    } catch (ignored: Throwable) {
      null
    }
  }
}
/**
 * Lets the user choose an image to overlay on top of the captured view to compare the app's visual against design mocks.
 */
class LoadOverlayAction(private val myPreview: ViewNodeActiveDisplay) :
    AnAction(ACTION_ID, "Overlay Image", getLayoutInspectorIcon("LOAD_OVERLAY")), CustomComponentAction {
  companion object {
    @JvmField
    val ACTION_ID = "Load Overlay"

    @JvmField
    val LOG = Logger.getInstance(LoadOverlayAction::class.java)
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    return ActionButtonWithText(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
  }

  override fun update(e: AnActionEvent) {
    super.update(e)
    if (e == null) return
    if (myPreview.hasOverlay()) {
      e.presentation.icon = getLayoutInspectorIcon("CLEAR_OVERLAY")
      e.presentation.text = "Clear Overlay"
    }
    else {
      e.presentation.icon = getLayoutInspectorIcon("LOAD_OVERLAY")
      e.presentation.text = ACTION_ID
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    if (myPreview.hasOverlay()) {
      myPreview.setOverLay(null, null)
    }
    else {
      UsageTracker.log(AndroidStudioEvent.newBuilder().setKind(AndroidStudioEvent.EventKind.LAYOUT_INSPECTOR_EVENT)
               .setLayoutInspectorEvent(LayoutInspectorEvent.newBuilder()
                                          .setType(LayoutInspectorEvent.LayoutInspectorEventType.OVERLAY_IMAGE)
               ))

      loadOverlay(e)
    }
  }

  private fun loadOverlay(e: AnActionEvent) {
    // choose image
    val descriptor = FileTypeDescriptor("Choose Overlay", "svg", "png", "jpg")
    val fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, null, null)
    val toSelect = LocalFileSystem.getInstance().refreshAndFindFileByPath(e.project?.basePath ?: "/")
    val files = fileChooserDialog.choose(null, toSelect!!)
    if (files.isEmpty()) {
      return
    }
    assert(files.size == 1)

    myPreview.setOverLay(loadImageFile(files[0]), files[0].name)
  }

  private fun loadImageFile(file: VirtualFile): Image? {
    return try {
      ImageIO.read(file.inputStream)
    }
    catch (e: IOException) {
      Messages.showErrorDialog("Failed to read image from \"" + file.name + "\" Error: " + e.message, "Error")
      LOG.warn(e)
      return null
    }
  }
}
