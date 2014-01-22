/*
 *  Copyright (C) 2013 The OmniROM Project
 *  Copyright (C) 2014 ParanoidAndroid Project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.android.systemui.statusbar.phone;

import com.android.systemui.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

class QuickSettingsDualRssiTile extends QuickSettingsDualBasicTile {

    public QuickSettingsDualRssiTile(Context context) {
        this(context, null);
    }

    public QuickSettingsDualRssiTile(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                        context.getResources().getDimensionPixelSize(R.dimen.quick_settings_cell_height)
        ));

        mFront = new QuickSettingsBasicRssiTile(context);
        mFront.setupDualTile(this);
        mBack = new QuickSettingsBasicTile(context);
        mBack.setupDualTile(this);

        addView(mBack,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        addView(mFront,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public void setFrontImageOverlayDrawable(Drawable drawable) {
        ((QuickSettingsBasicRssiTile) mFront).setImageOverlayDrawable(drawable);
    }

    public void setFrontImageOverlayResource(int id) {
        ((QuickSettingsBasicRssiTile) mFront).setImageOverlayResource(id);
    }
}
