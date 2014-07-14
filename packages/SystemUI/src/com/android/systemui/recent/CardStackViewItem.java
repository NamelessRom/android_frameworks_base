package com.android.systemui.recent;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class CardStackViewItem extends LinearLayout {

    private Context mContext;
    private int mOrientation;
    private int mPosition = 0;
    private float mTilt = 0.0f;
    private View mContentView;
    private int mStartPadding = 0;
    private int mEndPadding = 0;

    private Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();
    private Paint mPaint = new Paint();

    public CardStackViewItem(Context context) {
        super(context);
    }

    public CardStackViewItem(Context context, int orientation) {
        super(context);
        mContext = context;
        mOrientation = orientation;
        mCamera.save();
        mPaint.setColor(Color.WHITE);
        this.setStaticTransformationsEnabled(true);
    }

    public void resetContentView() {
        if (mContentView != null) {
            removeView(mContentView);
            mContentView = null;
        }
    }

    public void setContentView(View view, int width, int height) {
        mContentView = view;
        addView(view, new LinearLayout.LayoutParams(width, height));

        if (mOrientation == CardStackView.PORTRAIT) {
            mStartPadding = view.getPaddingTop();
            mEndPadding = view.getPaddingBottom();
        } else {
            mStartPadding = view.getPaddingLeft();
            mEndPadding = view.getPaddingRight();
        }
    }

    public View getContentView() {
        return mContentView;
    }

    public int getStartPadding() {
        return mStartPadding;
    }

    public int getEndPadding() {
        return mEndPadding;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int pos) {
        mPosition = pos;
    }

    public void setTilt(float tilt) {
        mTilt = tilt;
    }

    @Override
    protected boolean getChildStaticTransformation(View child, Transformation t) {
        int centerX = getWidth()/2;
        int centerY = getHeight()/2;

        mCamera.restore();
        mCamera.save();

        // Rotate the camera.
        if (mOrientation == CardStackView.PORTRAIT) {
            mCamera.rotateX(mTilt);
        } else {
            mCamera.rotateY(-mTilt);
        }

        mCamera.getMatrix(mMatrix);

        mMatrix.preTranslate(-centerX, -centerY);
        mMatrix.postTranslate(centerX, centerY);

        t.getMatrix().set(mMatrix);

        return true;
    }

    @Override
    public void invalidate() {
        if (mContentView != null) {
            super.invalidate();
            mContentView.invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }
}
