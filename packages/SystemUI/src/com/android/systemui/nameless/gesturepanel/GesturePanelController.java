package com.android.systemui.nameless.gesturepanel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.service.gesture.EdgeGestureManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.internal.util.gesture.EdgeGesturePosition;
import com.android.internal.util.gesture.EdgeServiceConstants;
import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;

/**
 * Created by alex on 25.10.14.
 */
public class GesturePanelController extends EdgeGestureManager.EdgeGestureActivationListener implements View.OnTouchListener {
    private static final String TAG = "GesturePanelController";
    private static final boolean DEBUG = true;

    private static GesturePanelController sInstance;

    public static final int GESTURE_PANEL_DISABLED = 0;
    public static final int GESTURE_PANEL_LEFT = 1;
    public static final int GESTURE_PANEL_RIGHT = 2;
    public static final int GESTURE_PANEL_BOTTOM = 3;

    private int mState;
    private boolean mAttached;

    private BaseStatusBar mBar;
    private Context mContext;
    private Handler mHandler;

    private GesturePanel mPanel;

    private WindowManager mWindowManager;
    private EdgeGestureManager mGestureManager;

    private class SettingsObserver extends ContentObserver {

        public SettingsObserver(final Handler handler) { super(handler); }

        private void observe() {
            final ContentResolver cr = mContext.getContentResolver();
            //cr.registerContentObserver(
            //        Settings.System.getUriFor(Settings.System.GESTURES_DOUBLE_TAP),
            //        false, this, UserHandle.USER_ALL);

            updateSettings();
        }

        @Override public void onChange(boolean selfChange) { onChange(selfChange, null); }

        @Override public void onChange(boolean selfChange, Uri uri) { updateSettings(); }
    }

    private void updateSettings() {
        mState = GESTURE_PANEL_LEFT;
        //mState = Settings.System.getIntForUser(mContext.getContentResolver(),
        //        Settings.System.GESTURES_DOUBLE_TAP, 0, UserHandle.USER_CURRENT);
    }

    private GesturePanelController() { super(Looper.getMainLooper()); }

    public static GesturePanelController getInstance() {
        if (sInstance == null) {
            sInstance = new GesturePanelController();
        }
        return sInstance;
    }

    public void init(Context context, WindowManager windowManager, BaseStatusBar baseStatusBar) {
        mContext = context;
        mHandler = new Handler();

        new SettingsObserver(mHandler).observe();

        mWindowManager = windowManager;
        mBar = baseStatusBar;

        mGestureManager = EdgeGestureManager.getInstance();
        mGestureManager.setEdgeGestureActivationListener(this);

        attachGesturePanel();
    }

    private boolean shouldShowGesturePanel() {
        return mState != GESTURE_PANEL_DISABLED;
    }

    public boolean isPanelShowing() {
        return mPanel != null && mAttached;
    }

    public void attachGesturePanel() {
        if (shouldShowGesturePanel()) {
            // want some slice?
            switch (mState) {
                // this is just main gravity, the trigger is centered later
                case GESTURE_PANEL_LEFT:
                    addPanelInLocation(Gravity.LEFT);
                    break;
                case GESTURE_PANEL_RIGHT:
                    addPanelInLocation(Gravity.RIGHT);
                    break;
                case GESTURE_PANEL_BOTTOM:
                default:
                    addPanelInLocation(Gravity.BOTTOM);
                    break;
            }
        }
    }

    public void detachGesturePanel() {
        if (mAttached) {
            if (mPanel != null) mWindowManager.removeView(mPanel);
            mAttached = false;
        }
    }

    public void showGesturePanel() {
        if (!mAttached) {
            attachGesturePanel();
        }
        if (mPanel != null) {
            mPanel.switchToOpenState();
        }
    }

    public void hideGesturePanel() {
        if (!mAttached) {
            attachGesturePanel();
        }
        if (mPanel != null) {
            mPanel.switchToClosingState();
        }
    }

    public void resetGesturePanel(boolean enabled) {
        detachGesturePanel();
        if (enabled) attachGesturePanel();
    }

    private void addPanelInLocation(final int gravity) {
        if (mAttached) return;

        // gesture panel
        mPanel = (GesturePanel) View.inflate(mContext, R.layout.gesture_action_overlay, null);
        mPanel.setOnTouchListener(this);
        mPanel.switchToClosingState();

        setupEdgeGesture(gravity);

        // init panel
        //mPanel.init(mHandler, mBar, gravity);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
        lp.setTitle("GesturePanel");
        lp.windowAnimations = android.R.style.Animation;

        mWindowManager.addView(mPanel, lp);
        mAttached = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mPanel.onTouchEvent(event);
        return true;
    }

    private boolean activateFromListener(int touchX, int touchY, EdgeGesturePosition position) {
        if (mPanel == null) {
            log("mPanel == null, attaching");
            attachGesturePanel();
        }
        if (mPanel != null) {
            log("switchToOpenState");
            mPanel.switchToOpenState();
            return true;
        }
        return false;
    }

    private void setupEdgeGesture(int gravity) {
        log("setupEdgeGesture");
        int triggerSlot = convertToEdgeGesturePosition(gravity);

        int sensitivity = mContext.getResources()
                .getInteger(R.integer.gesture_panel_trigger_sensivity);
        if (sensitivity < EdgeServiceConstants.SENSITIVITY_LOWEST
                || sensitivity > EdgeServiceConstants.SENSITIVITY_HIGHEST) {
            sensitivity = EdgeServiceConstants.SENSITIVITY_DEFAULT;
        }

        mGestureManager.updateEdgeGestureActivationListener(this,
                sensitivity << EdgeServiceConstants.SENSITIVITY_SHIFT | triggerSlot);
    }

    private int convertToEdgeGesturePosition(int gravity) {
        switch (gravity) {
            case Gravity.LEFT:
                return EdgeGesturePosition.LEFT.FLAG;
            case Gravity.RIGHT:
                return EdgeGesturePosition.RIGHT.FLAG;
            case Gravity.BOTTOM:
            default: // fall back
                return EdgeGesturePosition.BOTTOM.FLAG;
        }
    }

    @Override
    public void onEdgeGestureActivation(int touchX, int touchY, EdgeGesturePosition position,
            int flags) {
        log("onEdgeGestureActivation");
        if (activateFromListener(touchX, touchY, position)) {
            // give the main thread some time to do the bookkeeping
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    restoreListenerState();
                }
            });
        } else {
            restoreListenerState();
        }
    }

    private void log(final String msg) {
        if (DEBUG) Log.d(TAG, msg);
    }
}
