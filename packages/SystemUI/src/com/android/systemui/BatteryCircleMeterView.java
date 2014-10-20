/*
 * Copyright (C) 2012 Sven Dawitz for the CyanogenMod Project
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

package com.android.systemui;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.view.ViewGroup.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.android.internal.R;

import com.android.systemui.BatteryMeterView;
import com.android.systemui.statusbar.phone.BarBackgroundUpdater;

/***
 * Note about CircleBattery Implementation:
 *
 * Unfortunately, we cannot use BatteryController or DockBatteryController here,
 * since communication between controller and this view is not possible without
 * huge changes. As a result, this Class is doing everything by itself,
 * monitoring battery level and battery settings.
 */

public class BatteryCircleMeterView extends ImageView {
    final static String QuickSettings = "quicksettings";
    final static String StatusBar = "statusbar";
    private Handler mHandler;
    private Context mContext;
    private BatteryReceiver mBatteryReceiver = null;

    // state variables
    private boolean mAttached;      // whether or not attached to a window
    private boolean mActivated;     // whether or not activated due to system settings
    private boolean mCirclePercent; // whether or not to show percentage number
    private boolean mIsCharging;    // whether or not device is currently charging
    private int     mLevel;         // current battery level
    private int     mAnimOffset;    // current level of charging animation
    private boolean mIsAnimating;   // stores charge-animation status to reliably remove callbacks
    private int     mDockLevel;     // current dock battery level
    private boolean mDockIsCharging;// whether or not dock battery is currently charging
    private boolean mIsDocked = false;      // whether or not dock battery is connected

    private int     mCircleSize;    // draw size of circle. read rather complicated from
                                     // another status bar icon, so it fits the icon size
                                     // no matter the dps and resolution
    private RectF   mRectLeft;      // contains the precalculated rect used in drawArc(), derived from mCircleSize
    private RectF   mRectRight;     // contains the precalculated rect used in drawArc() for dock battery
    private Float   mTextLeftX;     // precalculated x position for drawText() to appear centered
    private Float   mTextY;         // precalculated y position for drawText() to appear vertical-centered
    private Float   mTextRightX;    // precalculated x position for dock battery drawText()

    // quiet a lot of paint variables. helps to move cpu-usage from actual drawing to initialization
    private Paint   mPaintFont;
    private Paint   mPaintGray;
    private Paint   mPaintSystem;
    private Paint   mPaintRed;
    private String  mCircleBatteryView;

    private int mCircleColor;
    private int mCircleTextColor;
    private int mCircleTextChargingColor;
    private int mCircleAnimSpeed = 4;

    private int mCurrentUserId = 0;

    private boolean mQS;

    private int mOverrideIconColor = 0;

    private final int mDSBDuration;

    // runnable to invalidate view via mHandler.postDelayed() call
    private final Runnable mInvalidate = new Runnable() {
        public void run() {
            if(mActivated && mAttached) {
                invalidate();
            }
        }
    };

    // keeps track of current battery level and charger-plugged-state
    class BatteryReceiver extends BroadcastReceiver {
        private boolean mIsRegistered = false;

        public BatteryReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mIsCharging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;

                if (mActivated && mAttached) {
                    LayoutParams l = getLayoutParams();
                    l.width = mCircleSize + (mIsDocked ? mCircleSize : 0);
                    setLayoutParams(l);

                    invalidate();
                }
            }
        }

        protected void registerSelf() {
            if (!mIsRegistered) {
                mIsRegistered = true;

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                mContext.registerReceiver(mBatteryReceiver, filter);
            }
        }

        private void unregisterSelf() {
            if (mIsRegistered) {
                mIsRegistered = false;
                mContext.unregisterReceiver(this);
            }
        }

        protected void updateRegistration() {
            if (mActivated && mAttached) {
                registerSelf();
            } else {
                unregisterSelf();
            }
        }
    }

    /***
     * Start of CircleBattery implementation
     */
    public BatteryCircleMeterView(Context context) {
        this(context, null);
    }

    public BatteryCircleMeterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryCircleMeterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray circleBatteryType = context.obtainStyledAttributes(attrs,
            com.android.systemui.R.styleable.BatteryIcon, 0, 0);

        mCircleBatteryView = circleBatteryType.getString(
                com.android.systemui.R.styleable.BatteryIcon_batteryView);

        mDSBDuration = context.getResources().getInteger(com.android.systemui.R.integer
                .dsb_transition_duration);

        if (mCircleBatteryView == null) {
            mCircleBatteryView = StatusBar;
        }

        mContext = context;
        mHandler = new Handler();
        mBatteryReceiver = new BatteryReceiver();
        updateSettings(false);
        BarBackgroundUpdater.addListener(new BarBackgroundUpdater.UpdateListener(this) {

            @Override
            public ObjectAnimator onUpdateStatusBarIconColor(final int previousIconColor,
                    final int iconColor) {
                mOverrideIconColor = iconColor;

                if (mQS || mOverrideIconColor == 0) {
                    mPaintSystem.setColor(mCircleColor);
                    mHandler.removeCallbacks(mInvalidate);
                    mHandler.postDelayed(mInvalidate, 50);
                } else {
                    if (mActivated && mAttached) {
                        final ObjectAnimator anim = ObjectAnimator.ofObject(mPaintSystem, "color",
                                new ArgbEvaluator(), mPaintSystem.getColor(), mOverrideIconColor);
                        anim.setDuration(mDSBDuration);
                        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(final ValueAnimator animator) {
                                invalidate();
                            }

                        });
                        return anim;
                    }
                }

                return null;
            }

        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            mBatteryReceiver.updateRegistration();
            updateSettings(false);
            setColors(mQS);
            mHandler.postDelayed(mInvalidate, 250);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            mAttached = false;
            mBatteryReceiver.updateRegistration();
            mRectLeft = null;   // makes sure, size based variables get
                                // recalculated on next attach
            mCircleSize = 0;    // makes sure, mCircleSize is reread from icons on
                                // next attach
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCircleSize == 0) {
            initSizeMeasureIconHeight();
        }

        setMeasuredDimension(mCircleSize
                + (mIsDocked ? mCircleSize : 0), mCircleSize);
    }

    private void drawCircle(Canvas canvas, int level, int animOffset, float textX, RectF drawRect) {
        Paint usePaint = mPaintSystem;

        // turn red at 14% - same level android battery warning appears
        if (level <= 14) {
            usePaint = mPaintRed;
        }
        usePaint.setAntiAlias(true);
        usePaint.setPathEffect(null);

        // pad circle percentage to 100% once it reaches 97%
        // for one, the circle looks odd with a too small gap,
        // for another, some phones never reach 100% due to hardware design
        int padLevel = level;
        if (padLevel >= 97) {
            padLevel = 100;
        }

        // draw thin gray ring first
        canvas.drawArc(drawRect, 270, 360, false, mPaintGray);
        // draw colored arc representing charge level
        canvas.drawArc(drawRect, 270 + animOffset, 3.6f * padLevel, false, usePaint);
        // if chosen by options, draw percentage text in the middle
        // always skip percentage when 100, so layout doesnt break
        if (level < 100 && mCirclePercent) {
            if (level <= 14) {
                mPaintFont.setColor(mPaintRed.getColor());
            } else if (mOverrideIconColor == 0 || mQS) {
                if (mIsCharging) {
                    mPaintFont.setColor(mCircleTextChargingColor);
                } else {
                    mPaintFont.setColor(mCircleTextColor);
                }
            } else {
                mPaintFont.setColor(mOverrideIconColor);
            }
            canvas.drawText(Integer.toString(level), textX, mTextY, mPaintFont);
        }

    }

    public void setColors(boolean qs) {
        mQS = qs;
        Resources res = getResources();
        int fgColor = res.getColor(qs ? com.android.systemui.R.color.qs_batterymeter_circle_fg :
                com.android.systemui.R.color.sb_batterymeter_circle_fg);
        int bgColor = res.getColor(qs ? com.android.systemui.R.color.qs_batterymeter_circle_bg :
                com.android.systemui.R.color.sb_batterymeter_circle_bg);
        int textColor = res.getColor(qs ? com.android.systemui.R.color.qs_batterymeter_circle_text :
                com.android.systemui.R.color.sb_batterymeter_circle_text);
        int chargingTextColor = res.getColor(qs ? com.android.systemui.R.color.qs_batterymeter_circle_text_charging :
                com.android.systemui.R.color.sb_batterymeter_circle_text_charging);
        mCircleTextColor = textColor;
        mCircleTextChargingColor = chargingTextColor;
        mCircleColor = fgColor;

        mPaintSystem.setColor(mOverrideIconColor == 0 || qs ? mCircleColor : mOverrideIconColor);
        // could not find the darker definition anywhere in resources
        // do not want to use static 0x404040 color value. would break theming.
        mPaintGray.setColor(bgColor);
        mPaintRed.setColor(res.getColor(R.color.holo_red_light));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRectLeft == null) {
            initSizeBasedStuff();
        }

        updateChargeAnim();

        if (mIsDocked) {
            drawCircle(canvas, mDockLevel, (mDockIsCharging ? mAnimOffset : 0),
                    mTextLeftX, mRectLeft);
            drawCircle(canvas, mLevel, (mIsCharging ? mAnimOffset : 0), mTextRightX, mRectRight);
        } else {
            drawCircle(canvas, mLevel, (mIsCharging ? mAnimOffset : 0), mTextLeftX, mRectLeft);
        }
    }

    public void updateSettings(final boolean qs) {
        Resources res = getResources();
        ContentResolver resolver = mContext.getContentResolver();

        updateUser(ActivityManager.getCurrentUser());
        /*
         * initialize vars and force redraw
         */
        initializeCircleVars();
        mRectLeft = null;
        mCircleSize = 0;

        int batteryStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                                Settings.System.STATUS_BAR_BATTERY_STYLE, 0, mCurrentUserId);

        mCirclePercent = batteryStyle == 3;
        mActivated = (batteryStyle == 2 || mCirclePercent);

        setVisibility(mActivated ? View.VISIBLE : View.GONE);

        if (mBatteryReceiver != null) {
            mBatteryReceiver.updateRegistration();
        }

        if (mActivated && mAttached) {
            invalidate();
        }
    }

    public void updateUser(int userId) {
        mCurrentUserId = userId;
    }

    /***
     * Initialize the Circle vars for start
     */
    private void initializeCircleVars() {
        // initialize and setup all paint variables
        // stroke width is later set in initSizeBasedStuff()

        Resources res = getResources();

        mPaintFont = new Paint();
        mPaintFont.setAntiAlias(true);
        mPaintFont.setDither(true);
        mPaintFont.setStyle(Paint.Style.STROKE);

        mPaintGray = new Paint(mPaintFont);
        mPaintSystem = new Paint(mPaintFont);
        mPaintRed = new Paint(mPaintFont);

        // font needs some extra settings
        mPaintFont.setTextAlign(Align.CENTER);
        mPaintFont.setFakeBoldText(true);
    }


    /***
     * updates the animation counter
     * cares for timed callbacks to continue animation cycles
     * uses mInvalidate for delayed invalidate() callbacks
     */
    private void updateChargeAnim() {
        if (!(mIsCharging || mDockIsCharging) || (mLevel >= 97 && mDockLevel >= 97)) {
            if (mIsAnimating) {
                mIsAnimating = false;
                mAnimOffset = 0;
                mHandler.removeCallbacks(mInvalidate);
            }
            return;
        }

        mIsAnimating = true;

        if (mAnimOffset > 360) {
            mAnimOffset = 0;
        } else {
            mAnimOffset += mCircleAnimSpeed;
        }

        mHandler.removeCallbacks(mInvalidate);
        mHandler.postDelayed(mInvalidate, 50);
    }

    /***
     * initializes all size dependent variables
     * sets stroke width and text size of all involved paints
     * YES! i think the method name is appropriate
     */
    private void initSizeBasedStuff() {
        if (mCircleSize == 0) {
            initSizeMeasureIconHeight();
        }

        mPaintFont.setTextSize(mCircleSize / 2f);

        float strokeWidth = mCircleSize / 7.5f;
        float strokeWidthBg = strokeWidth * getFloat(mQS ?
                com.android.systemui.R.dimen.qs_circle_battery_thickness_bg_percentage :
                com.android.systemui.R.dimen.sb_circle_battery_thickness_bg_percentage);
        float strokeWidthFg = strokeWidth * getFloat(mQS ?
                com.android.systemui.R.dimen.qs_circle_battery_thickness_fg_percentage :
                com.android.systemui.R.dimen.sb_circle_battery_thickness_fg_percentage);
        mPaintRed.setStrokeWidth(strokeWidthFg);
        mPaintSystem.setStrokeWidth(strokeWidthFg);
        mPaintGray.setStrokeWidth(strokeWidthBg);
        // calculate rectangle for drawArc calls
        mRectLeft = new RectF(strokeWidth / 2.0f, 0 + strokeWidth / 2.0f, mCircleSize
                - strokeWidth / 2.0f, mCircleSize - strokeWidth / 2.0f);
        mRectRight = new RectF(mRectLeft.left + mCircleSize, mRectLeft.top, mRectLeft.right + mCircleSize,
                mRectLeft.bottom);

        // calculate Y position for text
        Rect bounds = new Rect();
        mPaintFont.getTextBounds("99", 0, "99".length(), bounds);
        mTextLeftX = mCircleSize / 2.0f;
        mTextRightX = mTextLeftX + mCircleSize;

        mTextY = mCircleSize / 2.0f + (bounds.bottom - bounds.top) / 2.0f - strokeWidth / 2.0f;

        // balance out rounding issues. works out on all resolutions
        if (mCircleBatteryView.equals("quicksettings")) {
            mTextY = mTextY + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f,
                    mContext.getResources().getDisplayMetrics());
        } else if (mCircleBatteryView.equals("statusbar")) {
            mTextY = mTextY + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f,
                    mContext.getResources().getDisplayMetrics());
        }

        // force new measurement for wrap-content xml tag
        onMeasure(0, 0);
    }

    /***
     * we need to measure the size of the circle battery by checking another
     * resource. unfortunately, those resources have transparent/empty borders
     * so we have to count the used pixel manually and deduct the size from
     * it. quiet complicated, but the only way to fit properly into the
     * statusbar for all resolutions
     */
    private void initSizeMeasureIconHeight() {
        Bitmap measure = null;
        if (mCircleBatteryView.equals(QuickSettings)) {
            measure = BitmapFactory.decodeResource(getResources(),
                    com.android.systemui.R.drawable.ic_qs_wifi_full_4);
        } else if (mCircleBatteryView.equals(StatusBar)) {
            measure = BitmapFactory.decodeResource(getResources(),
                    com.android.systemui.R.drawable.stat_sys_wifi_signal_4_fully);
        }
        if (measure == null) {
            return;
        }

        mCircleSize = Math.round((float)(measure.getHeight() - 3) * getFloat(mQS ?
                com.android.systemui.R.dimen.qs_circle_battery_size_percentage :
                com.android.systemui.R.dimen.sb_circle_battery_size_percentage));
    }

    private float getFloat(int resId) {
        return Float.parseFloat(getResources().getString(resId));
    }

}
