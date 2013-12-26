/*
 * Copyright (C) 2012 The Android Open Source Project
 * This code has been modified. Portions copyright (C) 2013, OmniRom Project.
 * This code has been modified. Portions copyright (C) 2013, ParanoidAndroid Project.
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

import android.animation.LayoutTransition;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
<<<<<<< HEAD
import android.content.res.TypedArray;
=======
import android.provider.Settings;
import android.text.TextUtils;
>>>>>>> c59d05b... Base: QuickToggles (AOSPA) modification
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.systemui.R;

import java.util.ArrayList;

/**
 *
 */
public class QuickSettingsContainerView extends FrameLayout {

    // The number of columns in the QuickSettings grid
    private int mNumColumns;
    private int mNumFinalColumns;
    private int mNumFinalCol;
    private boolean updateColumns = false;

    // The gap between tiles in the QuickSettings grid
    private float mCellGap;

<<<<<<< HEAD
    private boolean mSingleRow;

    public QuickSettingsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QuickSettingsContainer, 0, 0);
        mSingleRow = a.getBoolean(R.styleable.QuickSettingsContainer_singleRow, false);
        a.recycle();
=======
    private Context mContext;
    private Resources mResources;

    // Default layout transition
    private LayoutTransition mLayoutTransition;

    // Edit mode status
    private boolean mEditModeEnabled;

    // Edit mode changed listener
    private EditModeChangedListener mEditModeChangedListener;

    public interface EditModeChangedListener {
        public abstract void onEditModeChanged(boolean enabled);
    }

    public QuickSettingsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mResources = getContext().getResources();

>>>>>>> c59d05b... Base: QuickToggles (AOSPA) modification
        updateResources();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLayoutTransition = getLayoutTransition();
        mLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
    }

    void updateResources() {
        mCellGap = mResources.getDimension(R.dimen.quick_settings_cell_gap);
        mNumColumns = mResources.getInteger(R.integer.quick_settings_num_columns);
        mNumFinalColumns = mResources.getInteger(R.integer.quick_settings_numfinal_columns);
        mNumFinalCol = shouldUpdateColumns() ? mNumFinalColumns : mNumColumns;
        requestLayout();
    }

    public void updateSpan() {
        Resources r = getContext().getResources();
        for(int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if(v instanceof QuickSettingsTileView) {
                QuickSettingsTileView qs = (QuickSettingsTileView) v;
                if (i < 3) { // Modify span of the first three childs
                    int span = r.getInteger(R.integer.quick_settings_user_time_settings_tile_span);
                    qs.setColumnSpan(span);
                } else {
                    qs.setColumnSpan(1); // One column item
                }
                qs.setTextSizes(getTileTextSize());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Calculate the cell width dynamically
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int availableWidth = (int) (width - getPaddingLeft() - getPaddingRight() -
<<<<<<< HEAD
                (mNumColumns - 1) * mCellGap);
        float cellWidth = (float) Math.ceil(((float) availableWidth) / mNumColumns);
        int cellHeight = 0;
        float cellGap = mCellGap;

        if (mSingleRow) {
            cellWidth = MeasureSpec.getSize(heightMeasureSpec);
            cellHeight = (int) cellWidth;
            cellGap /= 2;
        } else {
            cellHeight = (int) getResources().getDimension(R.dimen.quick_settings_cell_height);
        }
=======
                (mNumFinalCol - 1) * mCellGap);
        float cellWidth = (float) Math.ceil(((float) availableWidth) / mNumFinalCol);
>>>>>>> c59d05b... Base: QuickToggles (AOSPA) modification

        // Update each of the children's widths accordingly to the cell width
        final int N = getChildCount();
        int totalWidth = 0;
        int cursor = 0;
        for (int i = 0; i < N; ++i) {
            // Update the child's width
            QuickSettingsTileView v = (QuickSettingsTileView) getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                int colSpan = v.getColumnSpan();
                lp.width = (int) ((colSpan * cellWidth) + (colSpan - 1) * cellGap);
                lp.height = cellHeight;

                // Measure the child
                int newWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                int newHeightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                v.measure(newWidthSpec, newHeightSpec);
                cursor += colSpan;
                totalWidth += v.getMeasuredWidth() + cellGap;
            }
        }

        // Set the measured dimensions.  We always fill the tray width, but wrap to the height of
        // all the tiles.
<<<<<<< HEAD
        int numRows = (int) Math.ceil((float) cursor / mNumColumns);
        int newHeight = (int) ((numRows * cellHeight) + ((numRows - 1) * cellGap)) +
=======
        int numRows = (int) Math.ceil((float) cursor / mNumFinalCol);
        int newHeight = (int) ((numRows * cellHeight) + ((numRows - 1) * mCellGap)) +
>>>>>>> c59d05b... Base: QuickToggles (AOSPA) modification
                getPaddingTop() + getPaddingBottom();
        if (mSingleRow) {
            int totalHeight = cellHeight + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(totalWidth, totalHeight);
        } else {
            setMeasuredDimension(width, newHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int N = getChildCount();
        final boolean isLayoutRtl = isLayoutRtl();
        final int width = getWidth();

        int x = getPaddingStart();
        int y = getPaddingTop();
        int cursor = 0;

        float cellGap = mCellGap;

        if (mSingleRow) {
            cellGap /= 2;
        }

        for (int i = 0; i < N; ++i) {
            QuickSettingsTileView child = (QuickSettingsTileView) getChildAt(i);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (child.getVisibility() != GONE) {
                final int col = cursor % mNumFinalCol;
                final int colSpan = child.getColumnSpan();

                final int childWidth = lp.width;
                final int childHeight = lp.height;

                int row = (int) (cursor / mNumFinalCol);

                // Push the item to the next row if it can't fit on this one
<<<<<<< HEAD
                if ((col + colSpan) > mNumColumns && !mSingleRow) {
=======
                if ((col + colSpan) > mNumFinalCol) {
>>>>>>> c59d05b... Base: QuickToggles (AOSPA) modification
                    x = getPaddingStart();
                    y += childHeight + cellGap;
                    row++;
                }

                final int childLeft = (isLayoutRtl) ? width - x - childWidth : x;
                final int childRight = childLeft + childWidth;

                final int childTop = y;
                final int childBottom = childTop + childHeight;

                // Layout the container
                child.layout(childLeft, childTop, childRight, childBottom);

                // Offset the position by the cell gap or reset the position and cursor when we
                // reach the end of the row
                cursor += child.getColumnSpan();
<<<<<<< HEAD
                if (cursor < (((row + 1) * mNumColumns)) || mSingleRow) {
                    x += childWidth + cellGap;
                } else if (!mSingleRow) {
=======
                if (cursor < (((row + 1) * mNumFinalCol))) {
                    x += childWidth + mCellGap;
                } else {
>>>>>>> c59d05b... Base: QuickToggles (AOSPA) modification
                    x = getPaddingStart();
                    y += childHeight + cellGap;
                }
            }
        }
    }
<<<<<<< HEAD
=======

    public void setOnEditModeChangedListener(EditModeChangedListener listener) {
        mEditModeChangedListener = listener;
    }

    public void enableLayoutTransitions() {
        setLayoutTransition(mLayoutTransition);
    }

    public boolean isEditModeEnabled() {
        return mEditModeEnabled;
    }

    private boolean shouldUpdateColumns() {
        return updateColumns && !isLandscape();
    }

    private boolean isLandscape() {
        final boolean isLandscape =
            Resources.getSystem().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE;
        return isLandscape;
    }

    private int getTileTextSize() {
        // get tile text size based on column count
        switch (mNumFinalCol) {
            case 4:
                return mResources.getDimensionPixelSize(R.dimen.qs_4_column_text_size);
            case 3:
            default:
                return mResources.getDimensionPixelSize(R.dimen.qs_3_column_text_size);
        }
    }

    public void setEditModeEnabled(boolean enabled) {
        mEditModeEnabled = enabled;
        mEditModeChangedListener.onEditModeChanged(enabled);
        ArrayList<String> tiles = new ArrayList<String>();
        for(int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if(v instanceof QuickSettingsTileView) {
                QuickSettingsTileView qs = (QuickSettingsTileView) v;
                qs.setEditMode(enabled);

                // Add to provider string
                if(!enabled && qs.getVisibility() == View.VISIBLE
                        && !qs.isTemporary()) {
                    tiles.add(qs.getTileId().toString());
                }
            }
        }

        if(!enabled) { // Store modifications
            ContentResolver resolver = getContext().getContentResolver();
            if(!tiles.isEmpty()) {
                Settings.System.putString(resolver,
                        Settings.System.QUICK_SETTINGS_TILES,
                                TextUtils.join(QuickSettings.DELIMITER, tiles));
            } else { // No tiles
                Settings.System.putString(resolver,
                        Settings.System.QUICK_SETTINGS_TILES, QuickSettings.NO_TILES);
            }
            if (tiles.size() > 12) {
                updateColumns = true;
            } else if (tiles.size() < 12) {
                updateColumns = false;
            }
            updateSpan();
        }
    }
>>>>>>> c59d05b... Base: QuickToggles (AOSPA) modification
}
