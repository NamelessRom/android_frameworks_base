/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.systemui.recent;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SettingConfirmationHelper;
import android.view.Display;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SystemUI;

public class Recents extends SystemUI implements RecentsComponent {
    private static final String TAG = "Recents";
    private static final boolean DEBUG = false;

    private boolean mInitCardStack = false;
    static boolean mUseCardStack = false;

    @Override
    public void start() {
        putComponent(RecentsComponent.class, this);
    }

    @Override
    public void toggleRecents(Display display, int layoutDirection,
            View statusBarView, int expandedDesktopStyle) {
        if (DEBUG) Log.d(TAG, "toggle recents panel");
        try {
            int cardStackStatus = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_RECENTS_CARD_STACK, 0, UserHandle.USER_CURRENT);

            boolean useCardStack = false;
            if (cardStackStatus == 1) {
                useCardStack = true;
            }

            boolean toggleCardStack = false;
            if (!mInitCardStack) {
                // First run, accept value from settings
                mInitCardStack = true;
                mUseCardStack = useCardStack;
            } else if (useCardStack != mUseCardStack) {
                // No first run and setting has been flipped, toggle view
                toggleCardStack = true;
                mUseCardStack = useCardStack;
            }

            TaskDescription firstTask = RecentTasksLoader.getInstance(mContext).getFirstTask();

            Intent intent = new Intent(RecentsActivity.TOGGLE_RECENTS_INTENT);
            intent.setClassName("com.android.systemui",
                    "com.android.systemui.recent.RecentsActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            if (firstTask == null) {
                if (RecentsActivity.forceOpaqueBackground(mContext)) {
                    ActivityOptions opts = ActivityOptions.makeCustomAnimation(mContext,
                            R.anim.recents_launch_from_launcher_enter,
                            R.anim.recents_launch_from_launcher_exit);
                    mContext.startActivityAsUser(intent, opts.toBundle(), new UserHandle(
                            UserHandle.USER_CURRENT));
                } else {
                    // The correct window animation will be applied via the activity's style
                    mContext.startActivityAsUser(intent, new UserHandle(
                            UserHandle.USER_CURRENT));
                }
            } else {
                Bitmap first = null;
                if (firstTask.getThumbnail() instanceof BitmapDrawable) {
                    first = ((BitmapDrawable) firstTask.getThumbnail()).getBitmap();
                } else {
                    first = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    Drawable d = RecentTasksLoader.getInstance(mContext).getDefaultThumbnail();
                    d.draw(new Canvas(first));
                }
                final Resources res = mContext.getResources();

                DisplayMetrics dm = new DisplayMetrics();
                if (immersiveModeStyle == IMMERSIVE_MODE_FULL ||
                    immersiveModeStyle == IMMERSIVE_MODE_HIDE_ONLY_NAVBAR ||
                    immersiveModeStyle == IMMERSIVE_MODE_APP) {
                    display.getRealMetrics(dm);
                } else {
                    display.getMetrics(dm);
                }

                final Configuration config = res.getConfiguration();

                float thumbWidth;
                float thumbHeight;

                int cardPadding = res.getDimensionPixelSize(
                        com.android.internal.R.dimen.status_bar_recents_card_margin);
                Rect backgroundPadding = new Rect();
                if (mUseCardStack) {
                    float aspectRatio =
                        (float)first.getWidth() / (float)first.getHeight();

                    // The card contains a background 9 patch drawable for
                    // rendering a drop shadow. This introduces additional
                    // padding, which needs to be removed as well.
                    NinePatchDrawable npd = (NinePatchDrawable)res.getDrawable(R.drawable.status_bar_recent_card_shadow);

                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        // Full width, but padding reduces width of content a little bit
                        thumbWidth = dm.widthPixels - (cardPadding * 2);
                        if (npd != null && npd.getPadding(backgroundPadding)) {
                            thumbWidth -= backgroundPadding.left + backgroundPadding.right;
                        }
                        thumbHeight = thumbWidth / aspectRatio;
                    } else {
                        // Full height, but padding reduces height of content a little bit
                        thumbHeight = dm.heightPixels - (cardPadding * 2);
                        if (npd != null && npd.getPadding(backgroundPadding)) {
                            thumbHeight -= backgroundPadding.top + backgroundPadding.bottom;
                        }
                        thumbWidth = thumbHeight * aspectRatio;
                    }

                } else {
                    thumbWidth = res
                            .getDimensionPixelSize(R.dimen.status_bar_recents_thumbnail_width);
                    thumbHeight = res
                            .getDimensionPixelSize(R.dimen.status_bar_recents_thumbnail_height);
                }

                if (first == null) {
                    throw new RuntimeException("Recents thumbnail is null");
                }
                if (first.getWidth() != thumbWidth || first.getHeight() != thumbHeight) {
                    first = Bitmap.createScaledBitmap(first, (int) thumbWidth, (int) thumbHeight,
                            true);
                    if (first == null) {
                        throw new RuntimeException("Recents thumbnail is null");
                    }
                    if (mUseCardStack) {
                        // Compute reduced card size
                        if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            thumbHeight = dm.widthPixels - (backgroundPadding.top + backgroundPadding.bottom);
                        } else {
                            thumbWidth = dm.heightPixels - (backgroundPadding.left + backgroundPadding.right);
                        }

                        // Ensure thumbnail size is smaller or equal than bitmap
                        if (first.getWidth() < thumbWidth) {
                            thumbWidth = first.getWidth();
                        }
                        if (first.getHeight() < thumbHeight) {
                            thumbHeight = first.getHeight();
                        }

                        // Crop thumbnail to reduced card size
                        if (first.getWidth() != thumbWidth || first.getHeight() != thumbHeight) {
                            first = Bitmap.createBitmap(first, 0, 0, (int)thumbWidth, (int)thumbHeight);
                            if (first == null) {
                                throw new RuntimeException("Recents thumbnail is null");
                            }
                        }
                    }
                }

                // calculate it here, but consider moving it elsewhere
                // first, determine which orientation you're in.
                int x, y;

                if (mUseCardStack) {
                    int cardPos = RecentsCardStackView.getLastCardPos();
                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        x = cardPadding + backgroundPadding.left;
                        y = cardPos + cardPadding + backgroundPadding.top;
                    } else {
                        x = cardPos + cardPadding + backgroundPadding.left;
                        y = cardPadding + backgroundPadding.top;
                    }
                } else {
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    float appLabelLeftMargin = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_app_label_left_margin);
                    float appLabelWidth = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_app_label_width);
                    float thumbLeftMargin = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_thumbnail_left_margin);
                    float thumbBgPadding = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_thumbnail_bg_padding);

                    float width = appLabelLeftMargin +
                            +appLabelWidth
                            + thumbLeftMargin
                            + thumbWidth
                            + 2 * thumbBgPadding;

                    x = (int) ((dm.widthPixels - width) / 2f + appLabelLeftMargin + appLabelWidth
                            + thumbBgPadding + thumbLeftMargin);
                    y = (int) (dm.heightPixels
                            - res.getDimensionPixelSize(R.dimen.status_bar_recents_thumbnail_height)
                            - thumbBgPadding);
                    if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                        x = dm.widthPixels - x - res.getDimensionPixelSize(
                                R.dimen.status_bar_recents_thumbnail_width);
                    }
                } else { // if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    float thumbTopMargin = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_thumbnail_top_margin);
                    float thumbBgPadding = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_thumbnail_bg_padding);
                    float textPadding = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_text_description_padding);
                    float labelTextSize = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_app_label_text_size);
                    Paint p = new Paint();
                    p.setTextSize(labelTextSize);
                    float labelTextHeight = p.getFontMetricsInt().bottom
                            - p.getFontMetricsInt().top;
                    float descriptionTextSize = res.getDimensionPixelSize(
                            R.dimen.status_bar_recents_app_description_text_size);
                    p.setTextSize(descriptionTextSize);
                    float descriptionTextHeight = p.getFontMetricsInt().bottom
                            - p.getFontMetricsInt().top;

                    float statusBarHeight = res.getDimensionPixelSize(
                            com.android.internal.R.dimen.status_bar_height);
                    float recentsItemTopPadding = statusBarHeight;
                    if (expandedDesktopStyle != 0) {
                        statusBarHeight = 0;
                    }

                    float height = thumbTopMargin
                            + thumbHeight
                            + 2 * thumbBgPadding + textPadding + labelTextHeight
                            + recentsItemTopPadding + textPadding + descriptionTextHeight;
                    float recentsItemRightPadding = res
                            .getDimensionPixelSize(R.dimen.status_bar_recents_item_padding);
                    float recentsScrollViewRightPadding = res
                            .getDimensionPixelSize(R.dimen.status_bar_recents_right_glow_margin);
                    x = (int) (dm.widthPixels - res
                            .getDimensionPixelSize(R.dimen.status_bar_recents_thumbnail_width)
                            - thumbBgPadding - recentsItemRightPadding
                            - recentsScrollViewRightPadding);
                    y = (int) ((dm.heightPixels - statusBarHeight - height) / 2f + thumbTopMargin
                            + recentsItemTopPadding + thumbBgPadding + statusBarHeight);
                }
                }

                ActivityOptions opts = ActivityOptions.makeThumbnailScaleDownAnimation(
                        statusBarView,
                        first, x, y,
                        new ActivityOptions.OnAnimationStartedListener() {
                            public void onAnimationStarted() {
                                Intent intent =
                                        new Intent(RecentsActivity.WINDOW_ANIMATION_START_INTENT);
                                intent.setPackage("com.android.systemui");
                                mContext.sendBroadcastAsUser(intent,
                                        new UserHandle(UserHandle.USER_CURRENT));
                            }
                        });
                intent.putExtra(RecentsActivity.WAITING_FOR_WINDOW_ANIMATION_PARAM, true);
                mContext.startActivityAsUser(intent, opts.toBundle(), new UserHandle(
                        UserHandle.USER_CURRENT));
            }

            if (toggleCardStack) {
                restartRecentsActivity();
            }

            if (cardStackStatus == 0 || cardStackStatus == 3) {
                SettingConfirmationHelper.showConfirmationDialogForSetting(
                    mContext,
                    mContext.getString(R.string.status_bar_recents_card_stack_title),
                    mContext.getString(R.string.status_bar_recents_card_stack_message),
                    mContext.getResources().getDrawable(R.drawable.status_bar_recents_card_stack),
                    Settings.System.STATUS_BAR_RECENTS_CARD_STACK,
                    new SettingConfirmationHelper.OnSelectListener() {
                        @Override
                        public void onSelect(boolean enabled) {
                            if (mUseCardStack != enabled) {
                                mUseCardStack = enabled;
                                restartRecentsActivity();
                            }
                        }
                    });
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to launch RecentAppsIntent", e);
        }
    }

    private void restartRecentsActivity() {
        try {
            Intent intent = new Intent(RecentsActivity.TOGGLE_RECENTS_INTENT);
            intent.setClassName("com.android.systemui",
                    "com.android.systemui.recent.RecentsActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            mContext.startActivityAsUser(intent, new UserHandle(
                    UserHandle.USER_CURRENT));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to launch RecentAppsIntent", e);
        }
    }

    @Override
    public void preloadRecentTasksList() {
        if (DEBUG) Log.d(TAG, "preloading recents");
        Intent intent = new Intent(RecentsActivity.PRELOAD_INTENT);
        intent.setClassName("com.android.systemui",
                "com.android.systemui.recent.RecentsPreloadReceiver");
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));

        RecentTasksLoader.getInstance(mContext).preloadFirstTask();
    }

    @Override
    public void cancelPreloadingRecentTasksList() {
        if (DEBUG) Log.d(TAG, "cancel preloading recents");
        Intent intent = new Intent(RecentsActivity.CANCEL_PRELOAD_INTENT);
        intent.setClassName("com.android.systemui",
                "com.android.systemui.recent.RecentsPreloadReceiver");
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));

        RecentTasksLoader.getInstance(mContext).cancelPreloadingFirstTask();
    }

    @Override
    public void closeRecents() {
        if (DEBUG) Log.d(TAG, "closing recents panel");
        Intent intent = new Intent(RecentsActivity.CLOSE_RECENTS_INTENT);
        intent.setPackage("com.android.systemui");
        mContext.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
    }
}
