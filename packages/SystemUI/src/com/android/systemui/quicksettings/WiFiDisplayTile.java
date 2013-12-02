<<<<<<< HEAD
=======
/*
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2013 CyanogenMod Project
 * Copyright (C) 2013 The SlimRoms Project
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

>>>>>>> d85304f... Quicksettings customizations Slim style
package com.android.systemui.quicksettings;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.WifiDisplayStatus;
import android.view.LayoutInflater;
import android.view.View;
<<<<<<< HEAD
import android.view.View.OnClickListener;
=======
import android.view.View.OnLongClickListener;
>>>>>>> d85304f... Quicksettings customizations Slim style

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;

public class WiFiDisplayTile extends QuickSettingsTile{

    private boolean enabled = false;
    private boolean connected = false;

<<<<<<< HEAD
    public WiFiDisplayTile(Context context, 
            QuickSettingsController qsc) {
        super(context, qsc);

        mOnClick = new OnClickListener() {

            @Override
            public void onClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_WIFI_DISPLAY_SETTINGS);
            }
        };
=======
    public WiFiDisplayTile(Context context,
            QuickSettingsController qsc) {
        super(context, qsc);

        mOnLongClick = new OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                startSettingsActivity(android.provider.Settings.ACTION_WIFI_DISPLAY_SETTINGS);
                return true;
            }
        };

>>>>>>> d85304f... Quicksettings customizations Slim style
        qsc.registerAction(DisplayManager.ACTION_WIFI_DISPLAY_STATUS_CHANGED, this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
<<<<<<< HEAD
        WifiDisplayStatus status = (WifiDisplayStatus)intent.getParcelableExtra(DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
=======
        WifiDisplayStatus status = (WifiDisplayStatus) intent.getParcelableExtra(
                DisplayManager.EXTRA_WIFI_DISPLAY_STATUS);
>>>>>>> d85304f... Quicksettings customizations Slim style
        enabled = status.getFeatureState() == WifiDisplayStatus.FEATURE_STATE_ON;
        connected = status.getActiveDisplay() != null;
        updateResources();
    }

    @Override
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        if(enabled && connected) {
            mLabel = mContext.getString(R.string.quick_settings_wifi_display_label);
<<<<<<< HEAD
            mDrawable = R.drawable.ic_qs_cast_connected;
        }else{
            mLabel = mContext.getString(R.string.quick_settings_wifi_display_no_connection_label);
            mDrawable = R.drawable.ic_qs_cast_available;
=======
            mDrawable = R.drawable.ic_qs_remote_display_connected;
        }else{
            mLabel = mContext.getString(R.string.quick_settings_wifi_display_no_connection_label);
            mDrawable = R.drawable.ic_qs_remote_display;
>>>>>>> d85304f... Quicksettings customizations Slim style
        }
    }

    @Override
    void updateQuickSettings() {
        mTile.setVisibility(enabled ? View.VISIBLE : View.GONE);
        super.updateQuickSettings();
    }
}
