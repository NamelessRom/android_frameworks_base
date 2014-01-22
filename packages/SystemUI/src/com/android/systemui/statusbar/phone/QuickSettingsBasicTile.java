/*
 * Copyright (C) 2013 The Android Open Source Project
 * This code has been modified. Portions copyright (C) 2014 ParanoidAndroid Project.
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

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.R;

class QuickSettingsBasicTile extends QuickSettingsTileView {
    public TextView mTextView;
    public ImageView mImageView;
    public ImageView mSwitchView;

    public QuickSettingsBasicTile(Context context) {
        this(context, null);
    }

    public QuickSettingsBasicTile(Context context, AttributeSet attrs) {
        this(context, attrs, R.layout.quick_settings_tile_basic);
    }

    public QuickSettingsBasicTile(Context context, AttributeSet attrs, int layoutId) {
        super(context, attrs);

        setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            context.getResources().getDimensionPixelSize(R.dimen.quick_settings_cell_height)
        ));
        setBackgroundResource(R.drawable.qs_tile_background);
        addView(LayoutInflater.from(context).inflate(layoutId, null),
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
        mTextView = (TextView) findViewById(R.id.text);
        mImageView = (ImageView) findViewById(R.id.image);
    }

    @Override
    void setContent(int layoutId, LayoutInflater inflater) {
        throw new RuntimeException("why?");
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public TextView getTextView() {
        return mTextView;
    }

    public void setImageDrawable(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }

    public void setImageResource(int resId) {
        mImageView.setImageResource(resId);
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    public void setTextResource(int resId) {
        mTextView.setText(resId);
    }

    @Override
    public void setEditMode(boolean enabled) {
        // No hover on edit mode
        setBackgroundResource(enabled ? R.drawable.qs_tile_background_no_hover :
                R.drawable.qs_tile_background);
        super.setEditMode(enabled);
    }

    public void setupDualTile(final QuickSettingsDualBasicTile dualTile) {
        if(dualTile != null) {
            // Set up switch
            mSwitchView = (ImageView) findViewById(R.id.switch_button_image);
            mSwitchView.setVisibility(View.VISIBLE);
            mSwitchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dualTile.swapTiles();
                }
            });
        }
    }

    public void setSwitchViewVisibility(int vis) {
        mSwitchView.setVisibility(vis);
    }
}
