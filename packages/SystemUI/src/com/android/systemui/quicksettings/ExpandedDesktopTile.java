<<<<<<< HEAD
=======
/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
<<<<<<< HEAD
import android.os.Handler;
=======
>>>>>>> d85304f... Quicksettings customizations Slim style
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class ExpandedDesktopTile extends QuickSettingsTile {
<<<<<<< HEAD
    private boolean mEnabled = false;

    public ExpandedDesktopTile(Context context,
            QuickSettingsController qsc, Handler handler) {
=======

    private boolean mEnabled = false;

    public ExpandedDesktopTile(Context context, final QuickSettingsController qsc) {
>>>>>>> d85304f... Quicksettings customizations Slim style
        super(context, qsc);

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change the system setting
<<<<<<< HEAD
                Settings.System.putIntForUser(mContext.getContentResolver(),
                        Settings.System.EXPANDED_DESKTOP_STATE, mEnabled ? 0 : 1,
                        UserHandle.USER_CURRENT);
            }
        };

        mOnLongClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$SystemSettingsActivity");
                startSettingsActivity(intent);
                return true;
            }
        };

        Uri stateUri = Settings.System.getUriFor(Settings.System.EXPANDED_DESKTOP_STATE);
        qsc.registerObservedContent(stateUri, this);
    }

=======
                Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.EXPANDED_DESKTOP_STATE,
                    !mEnabled ? 1 : 0);
            }
        };

        qsc.registerObservedContent(Settings.System.getUriFor(
                    Settings.System.EXPANDED_DESKTOP_STATE), this);
    }

    @Override
>>>>>>> d85304f... Quicksettings customizations Slim style
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

<<<<<<< HEAD
    private synchronized void updateTile() {
        mEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.EXPANDED_DESKTOP_STATE, 0, UserHandle.USER_CURRENT) == 1;
=======
    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        updateResources();
    }

    private synchronized void updateTile() {
        mEnabled = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.EXPANDED_DESKTOP_STATE, 0,
                UserHandle.USER_CURRENT) == 1;
>>>>>>> d85304f... Quicksettings customizations Slim style
        if (mEnabled) {
            mDrawable = R.drawable.ic_qs_expanded_desktop_on;
            mLabel = mContext.getString(R.string.quick_settings_expanded_desktop);
        } else {
            mDrawable = R.drawable.ic_qs_expanded_desktop_off;
            mLabel = mContext.getString(R.string.quick_settings_expanded_desktop_off);
        }
    }

<<<<<<< HEAD
    @Override
    public void onChangeUri(ContentResolver resolver, Uri uri) {
        updateResources();
    }
=======
>>>>>>> d85304f... Quicksettings customizations Slim style
}
