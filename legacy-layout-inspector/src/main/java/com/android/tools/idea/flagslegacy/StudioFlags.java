/*
 * Copyright 2022 pingfangx <https://www.pingfangx.com>
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.idea.flagslegacy;

import com.android.annotations.NonNull;

/**
 * pingfangx changed: Add this file in the flagslegacy package, some flags have been deleted in the following commit.
 * <p>
 * Delete unused flags
 * b3e962a4 Joe Baker-Malone <jbakermalone@google.com> on 2022/2/1 at 1:52
 * committed on 2022/2/1 at 2:55
 *
 * @author pingfangx
 * @date 2022/9/22
 */
public class StudioFlags {
  private static final FlagGroup LAYOUT_INSPECTOR = new FlagGroup();
  public static final Flag<Boolean> LAYOUT_INSPECTOR_LOAD_OVERLAY_ENABLED = Flag.create(
      LAYOUT_INSPECTOR, "load.overlay", "Enable the Load Overlay feature",
      "If enabled, show actions to let user choose overlay image on preview.", true);
  public static final Flag<Boolean> LAYOUT_INSPECTOR_SUB_VIEW_ENABLED = Flag.create(
      LAYOUT_INSPECTOR, "sub.view", "Enable the sub view feature",
      "If enabled, changes the preview to focus on a component.", true);
  public static final Flag<Boolean> LAYOUT_INSPECTOR_V2_PROTOCOL_ENABLED = Flag.create(
      LAYOUT_INSPECTOR, "capture.v2", "Enable using V2 protocol to capture view data",
      "If enabled, uses V2 protocol to capture view information from device.", false);
  public static final Flag<Boolean> LAYOUT_INSPECTOR_EDITING_ENABLED = Flag.create(
      LAYOUT_INSPECTOR, "ui.editing", "Enable editing ViewNode properties in the properties table.",
      "If enabled, users can edit properties in the properties table.", false);
  public static final Flag<Boolean> DYNAMIC_LAYOUT_INSPECTOR_ENABLED = Flag.create(
      LAYOUT_INSPECTOR, "dynamic.layout.inspector", "Enable dynamic layout inspector",
      "Turns on the dynamic layout inspector.", true);
  public static final Flag<Boolean> DYNAMIC_LAYOUT_INSPECTOR_EDITING_ENABLED = Flag.create(
      LAYOUT_INSPECTOR, "dynamic.layout.editor", "Enable dynamic layout editor",
      "If enabled, users can edit layout properties with live updates on a device while the dynamic layout inspector is running.",
      false);
  public static final Flag<Boolean> DYNAMIC_LAYOUT_INSPECTOR_ENABLE_SNAPSHOTS = Flag.create(
      LAYOUT_INSPECTOR, "dynamic.layout.inspector.enable.snapshots", "Enable snapshots",
      "Enable saving and loading snapshots in the layout inspector.", true);

  public static class Flag<T> {
    private final T defaultValue;

    public Flag(T defaultValue) {
      this.defaultValue = defaultValue;
    }

    public T get() {
      return defaultValue;
    }

    public static Flag<Boolean> create(
        @NonNull FlagGroup group,
        @NonNull String name,
        @NonNull String displayName,
        @NonNull String description,
        boolean defaultValue) {
      return new Flag<>(defaultValue);
    }
  }

  static class FlagGroup {
  }
}
