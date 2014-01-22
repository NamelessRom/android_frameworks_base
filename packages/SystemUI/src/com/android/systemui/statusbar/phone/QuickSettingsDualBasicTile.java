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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.OvershootInterpolator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class QuickSettingsDualBasicTile extends QuickSettingsTileView {

    private static final int TRANSLATION_Y = 300;

    public QuickSettingsBasicTile mFront;
    public QuickSettingsBasicTile mBack;

    private Context mContext;
    private boolean mAnimationLock;

    public QuickSettingsDualBasicTile(Context context) {
        this(context, null);
    }

    public QuickSettingsDualBasicTile(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
    }

    public void setDefaultContent() {
        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                        mContext.getResources().getDimensionPixelSize(R.dimen.quick_settings_cell_height)
        ));

        mFront = new QuickSettingsBasicTile(mContext);
        mFront.setupDualTile(this);
        mBack = new QuickSettingsBasicTile(mContext);
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

    public QuickSettingsTileView getFront() {
        return mFront;
    }

    public QuickSettingsTileView getBack() {
        return mBack;
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

    public void swapTiles() {
        swapTiles(false);
    }

    public void swapTiles(final boolean bounce) {
        if(mAnimationLock) return;
        if(mFront.getVisibility() == View.VISIBLE) {
            mFront.animate().translationYBy(TRANSLATION_Y).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mBack.setVisibility(View.VISIBLE);
                            mAnimationLock = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mBack.bringToFront();
                            mFront.setVisibility(View.GONE);
                            mFront.setTranslationY(0);
                            mAnimationLock = false;
                        }
                    });
        } else {
            mBack.animate().translationYBy(-TRANSLATION_Y).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mFront.setVisibility(View.VISIBLE);
                            mAnimationLock = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFront.bringToFront();
                            if(bounce) {
                                mFront.animate().rotationX(20).setListener(
                                    new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            mFront.animate().rotationX(0).setListener(null);
                                        }
                                    });
                            }
                            mBack.setVisibility(View.GONE);
                            mBack.setTranslationY(0);
                            mAnimationLock = false;
                        }
                    });
        }
    }


    @Override
    public void setEditMode(boolean enabled) {
        super.setEditMode(enabled);
        int visibility = enabled ? View.INVISIBLE : View.VISIBLE;
        if (mFront.getVisibility() == View.VISIBLE) {
            mFront.setSwitchViewVisibility(visibility);
        } else {
            mBack.setSwitchViewVisibility(visibility);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // If edit mode is enabled, don't allow childrens to receive touch events
        if(isEditModeEnabled()) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }
}
