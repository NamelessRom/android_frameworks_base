/*
 *  Copyright (C) 2013 The OmniROM Project
 *  This code has been modified. Portions copyright (C) 2013, ParanoidAndroid Project.
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

class QuickSettingsDualTile extends QuickSettingsTileView {

    private final QuickSettingsBasicTile mFront;
    private final QuickSettingsBasicTile mBack;
    private final QuickSettingsTileFlipAnimator mFlipAnimator;

    public QuickSettingsDualTile(Context context) {
        this(context, null);
    }

    public QuickSettingsDualTile(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                        context.getResources().getDimensionPixelSize(R.dimen.quick_settings_cell_height)
        ));

        mFront = new QuickSettingsBasicTile(context);
        mBack = new QuickSettingsBasicTile(context);
        mFlipAnimator = new QuickSettingsTileFlipAnimator(mFront, mBack);

        setClickable(true);
        setSelected(true);
        setFocusable(true);

        mBack.setVisibility(View.GONE);

        mFront.setTemporary(true);
        mBack.setTemporary(true);

        addView(mFront,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        addView(mBack,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (!isEditModeEnabled()) {
            return mFlipAnimator.onTouch(this, e);
        }
        return super.onTouchEvent(e);
    }

    public void setFrontImageDrawable(Drawable drawable) {
        mFront.setImageDrawable(drawable);
    }
    
    public void setBackImageDrawable(Drawable drawable) {
        mBack.setImageDrawable(drawable);
    }

    public void setFrontImageResource(int id) {
        mFront.setImageResource(id);
    }

    public void setBackImageResource(int id) {
        mBack.setImageResource(id);
    }

    public void setFrontText(CharSequence text) {
        mFront.setText(text);
    }

    public void setBackText(CharSequence text) {
        mBack.setText(text);
    }

    public void setFrontTextResource(int id) {
        mFront.setTextResource(id);
    }

    public void setBackTextResource(int id) {
        mBack.setTextResource(id);
    }

    public void setFrontContentDescription(CharSequence text) {
        mFront.setContentDescription(text);
    }

    public void setBackContentDescription(CharSequence text) {
        mBack.setContentDescription(text);
    }

    public void setFrontPressed(boolean press) {
        mFront.setPressed(press);
    }

    public void setFrontOnClickListener(View.OnClickListener listener) {
        mFront.setOnClickListener(listener);
    }

    public void setBackOnClickListener(View.OnClickListener listener) {
        mBack.setOnClickListener(listener);
    }

    public void setFrontOnLongClickListener(View.OnLongClickListener listener) {
        mFront.setOnLongClickListener(listener);
    }

    public void setBackOnLongClickListener(View.OnLongClickListener listener) {
        mBack.setOnLongClickListener(listener);
    }

    public QuickSettingsTileView getFront() {
        return mFront;
    }

    public QuickSettingsTileView getBack() {
        return mBack;

    }

}
