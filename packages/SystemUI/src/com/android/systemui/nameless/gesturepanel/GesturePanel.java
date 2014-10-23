package com.android.systemui.nameless.gesturepanel;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.gesture.EdgeGestureManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.gesture.EdgeGesturePosition;
import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_APP_SWITCH;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_HOME;

public class GesturePanel extends FrameLayout implements GestureOverlayView.OnGestureListener {
    public static final String TAG = "GesturePanel";

    private GestureOverlayView mGestureView;

    State mState;
    View mContent;
    GestureLibrary mStore;

    public static enum State {Expanded, Opening, Closing}

    public GesturePanel(Context context) {
        this(context, null);
    }

    public GesturePanel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GesturePanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final File mStoreFile = new File("/data/system", "gpv_gestures");
        mStore = GestureLibraries.fromFile(mStoreFile);
    }

    // on-screen buttons
    private final OnClickListener mCancelButtonListener = new OnClickListener() {
        @Override public void onClick(View v) {
            if (mState != State.Opening) {
                switchToState(State.Closing);
            }
        }
    };

    private final OnClickListener mAddGestureButtonListener = new OnClickListener() {
        @Override public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(ComponentName.unflattenFromString(
                    "com.android.settings/com.android.settings.nameless.gesturepanel.GestureBuilderActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            switchToState(State.Closing);
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContent = findViewById(R.id.content);
        mGestureView = (GestureOverlayView) findViewById(R.id.gesture_overlay);
        mGestureView.setGestureVisible(true);
        mGestureView.addOnGestureListener(this);
        findViewById(R.id.cancel_gesturing).setOnClickListener(mCancelButtonListener);
        TextView addButton = (TextView) findViewById(R.id.add_gesture);
        addButton.setTextSize(getResources().getDimension(R.dimen.gesture_panel_text_size));
        addButton.setOnClickListener(mAddGestureButtonListener);

        invalidate();

        if (mStore != null) {
            mStore.load();
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getAction() == ACTION_DOWN) {
            if ((event.getKeyCode() == KEYCODE_BACK) ||
                    (event.getKeyCode() == KEYCODE_HOME) ||
                    (event.getKeyCode() == KEYCODE_APP_SWITCH)) {
                switchToState(State.Closing);
            }
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }

    protected void switchToState(State state) {
        switch (state) {
            case Expanded:
                mGestureView.setVisibility(View.VISIBLE);
                break;
            case Opening:
                mContent.setVisibility(View.VISIBLE);
                mGestureView.setVisibility(View.VISIBLE);
                break;
            case Closing:
                mContent.setVisibility(View.GONE);
                break;
        }
        mState = state;
    }

    // Public methods for PSB & BSB
    public void switchToClosingState() {
        switchToState(State.Closing);
    }

    public void switchToOpenState() {
        switchToState(State.Opening);
    }

    public boolean isGesturePanelAttached() {
        return mState != State.Closing;
    }

    // Gesture handling
    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event) {
    }

    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
        Gesture gesture = overlay.getGesture();
        List<Prediction> predictions = mStore.recognize(gesture);
        for (Prediction prediction : predictions) {
            if (prediction.score >= 2.0) {
                String uri = prediction.name.substring(prediction.name.indexOf('|') + 1);
                try {
                    Intent intent = Intent.parseUri(uri, 0);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (URISyntaxException e) {
                    Log.e(TAG, "URISyntaxException: [" + uri + "]");
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "ActivityNotFound: [" + uri + "]");
                }
                switchToState(State.Closing);
                return;
            }
        }
    }

    @Override
    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
        if (mState != State.Expanded) {
            switchToState(State.Expanded);
        }
    }

}
