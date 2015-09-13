/*
 * <!--
 *    Copyright (C) 2015 The NamelessRom Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -->
 */

package org.nameless;

import android.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy;

public class GestureHelper implements WindowManagerPolicy.PointerEventListener, GestureDetector.OnGestureListener {
    public static final int INVALID = Integer.MIN_VALUE;

    protected static final int DEFAULT_SWIPE_DISTANCE = 350;
    protected static final int DEFAULT_SWIPE_TIMEOUT_MS = 500;

    protected static final int MAX_POINTERS = 32;
    protected static final int MIN_POINTERS = 1;

    public static final int SWIPE_FROM_LEFT = 0;
    public static final int SWIPE_FROM_TOP = 1;
    public static final int SWIPE_FROM_RIGHT = 2;
    public static final int SWIPE_FROM_BOTTOM = 3;

    public int mSwipeDistance;
    public int mSwipeTimeout;

    public interface Listener {
        void onSwipe(final int direction, final int pointerCount);
    }

    private Listener mListener;

    private int mPointerCount;
    private final float[] mPointerX = new float[MAX_POINTERS];
    private final float[] mPointerY = new float[MAX_POINTERS];
    private final long[] mPointerTime = new long[MAX_POINTERS];

    private boolean mGestureHandled;

    public GestureHelper(@NonNull final Listener listener) {
        mListener = listener;
        mSwipeDistance = DEFAULT_SWIPE_DISTANCE;
        mSwipeTimeout = DEFAULT_SWIPE_TIMEOUT_MS;
    }

    @Override
    public void onPointerEvent(MotionEvent motionEvent) {
        onDown(motionEvent);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mGestureHandled = false;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mPointerCount = motionEvent.getPointerCount();
                if ((mPointerCount >= MIN_POINTERS) && (mPointerCount <= MAX_POINTERS)) {
                    getPointerDown(motionEvent);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mGestureHandled) {
                    break;
                }
                final int swipeDirection = getSwipeDirection(motionEvent);
                if (swipeDirection != INVALID) {
                    mListener.onSwipe(swipeDirection, mPointerCount);
                    mGestureHandled = true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                mGestureHandled = true;
            }
        }
        return false;
    }

    private void getPointerDown(MotionEvent motionEvent) {
        final int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
        final int pointerIndex = motionEvent.findPointerIndex(pointerId);
        if (pointerIndex != MotionEvent.INVALID_POINTER_ID) {
            mPointerX[pointerIndex] = motionEvent.getX(pointerIndex);
            mPointerY[pointerIndex] = motionEvent.getY(pointerIndex);
            mPointerTime[pointerIndex] = motionEvent.getEventTime();
        }
    }

    private int getSwipeDirection(MotionEvent motionEvent) {
        final int pointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
        final int pointerIndex = motionEvent.findPointerIndex(pointerId);
        final float x = motionEvent.getX(pointerIndex);
        final float y = motionEvent.getY(pointerIndex);

        int direction = INVALID;
        for (int index = 0; index < mPointerCount; index++) {
            final long elapsed = motionEvent.getEventTime() - mPointerTime[pointerIndex];
            if (elapsed > mSwipeTimeout) {
                break;
            }

            int tmpDirection = INVALID;

            // detect up and down
            final float fromY = mPointerY[pointerIndex];
            if (y > (fromY + mSwipeDistance)) {
                tmpDirection = SWIPE_FROM_TOP;
            } else if (y < (fromY - mSwipeDistance)) {
                tmpDirection = SWIPE_FROM_BOTTOM;
            } else {
                // detect left and right
                final float fromX = mPointerX[pointerIndex];
                if (x > (fromX + mSwipeDistance / 2)) {
                    tmpDirection = SWIPE_FROM_LEFT;
                }
                if (x < (fromX - mSwipeDistance / 2)) {
                    tmpDirection = SWIPE_FROM_RIGHT;
                }
            }

            if (direction != INVALID && direction != tmpDirection) {
                return INVALID;
            }
            direction = tmpDirection;
        }
        return direction;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

}
