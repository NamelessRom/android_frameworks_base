package com.android.systemui.recent;

import com.android.systemui.R;
import com.android.systemui.SwipeHelper;
import com.android.systemui.recent.RecentsPanelView.TaskDescriptionAdapter;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

public class RecentsCardStackView extends CardStackView implements View.OnClickListener,
        View.OnLongClickListener, SwipeHelper.Callback, RecentsPanelView.RecentsScrollView {

    private static RecentsCardStackView mInstance;

    public static int getLastCardPos() {
        if (mInstance != null) {
            int count = mInstance.mItems.size();
            if (count > 1) {
                return mInstance.scrollPositionToViewPosition(
                        mInstance.mItems.get(count-1).getPosition());
            }
        }
        return 0;
    }

    private int mLastViewTouch;
    private boolean mIsSwiping;
    private boolean mClearAllAnimationDone;
    private Handler mHandler = new Handler();

    // SwipeHelper for handling swipe to dismiss.
    private SwipeHelper mSwipeHelper;

    // RecentScrollView implementation for interaction with parent
    // RecentsPanelView.
    private RecentsCallback mCallback;
    private TaskDescriptionAdapter mAdapter;

    // RecentsActivity for dismissing RecentsView
    private final RecentsActivity mRecentsActivity;

    public RecentsCardStackView(Context context, RecentsActivity recentsActivity, int orientation) {
        super(context, orientation);

        setOnClickListener(this);
        setOnLongClickListener(this);

        setBackgroundResource(R.drawable.status_bar_recents_background);

        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(
                mOrientation == PORTRAIT ? SwipeHelper.X : SwipeHelper.Y,
                this, densityScale, pagingTouchSlop);

        mClearAllAnimationDone = true;

        mRecentsActivity = recentsActivity;

        mInstance = this;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(mContext).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }

    @Override
    public void onClick(View view) {
        if (!wasScrolling() && !mIsSwiping) {
            int viewId = getChildIdAtViewPosition(mLastViewTouch, false);

            if (viewId >= 0 && mCallback != null) {
                View v = mItems.get(viewId).getContentView();
                if (v != null) {
                    mCallback.handleOnClick(v);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (!isScrolling() && !mIsSwiping) {
            int viewId = getChildIdAtViewPosition(mLastViewTouch, false);

            if (viewId >= 0 && mCallback != null) {
                View contentView = mItems.get(viewId).getContentView();
                if (contentView != null) {
                    RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) contentView.getTag();
                    if (holder != null) {
                        final View thumbnailView = holder.thumbnailView;
                        final View anchorView = holder.appColorBarView;
                        mCallback.handleLongPress(contentView, anchorView, thumbnailView);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onInterceptTouchEvent(ev) ||
                super.onInterceptTouchEvent(ev);
    }


    private void dismissChild(View v) {
        mSwipeHelper.dismissChild(v, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mLastViewTouch = (int)getPos(ev);
        mIsSwiping = mSwipeHelper.onTouchEvent(ev);
        return mIsSwiping ||
                super.onTouchEvent(ev);
    }

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        int id = getChildIdAtViewPosition(
                getPos(ev), false);

        CardStackViewItem view = null;
        if (id >= 0) {
            view = mItems.get(id);
        }
        return view;
    }

    @Override
    public View getChildContentView(View v) {
        return v;
    }

    @Override
    public boolean canChildBeDismissed(int gestureDirection, View v) {
        return true;
    }

    @Override
    public boolean isConstrainSwipeEnabled() {
        return true;
    }

    @Override
    public boolean isFadeoutEnabled(int gestureDirection) {
         return true;
     }

    @Override
    public void onBeginDrag(View v) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
        v.setActivated(true);
    }

    @Override
    public void onChildDismissed(int gestureDirection, View v) {
        // First: Handle swipe and let callback remove view from adapter
        if (mCallback != null) {
            // Use fake view with correct task description
            mCallback.handleSwipe(v);
        }

        // Second: Remove item from current view.
        // Remove animation will call the adapter for layouting cards that
        // become visible. Therefore, we need to handle swipe (removing from
        // adapter) before removing the item.
        removeItem(v);
        resetOverscrolling();
    }

    public void onDragCancelled(View v) {
        v.setActivated(false);
        resetOverscrolling();
    }

    @Override
    public int numItemsInOneScreenful() {
        return mItems.size();
    }

    @Override
    protected void updateAdapter() {
        int count = mAdapter.getCount();
        int size = mItems.size();
        int missing = count - size;
        for (int i = 0; i < missing; ++i) {
            // Create all missing items
            // (empty views, shouldn't be a big performance issue)
            CardStackViewItem item = new CardStackViewItem(mContext, mOrientation);
            addView(item, positionView(0));
            mItems.add(item);
        }

        if (missing > 0) {
            // Compute lengths and positions
            updateLengths();
            updatePositions();
        }

        // Update all visible items and their content views from adapter
        for (int i = 0; i < count; ++i) {
            CardStackViewItem item = mItems.get(i);

            if (isOccluded(i)) {
                if (item.getContentView() != null) {
                    // This should never happen!
                    // (content views are released by updateLayout())
                    Log.w(TAG, "Need to release occluded content view " + i);
                    item.resetContentView();
                }

                if (item.getTag() == null) {
                    // Update single item from adapter. Add tag info to card
                    // (necessary if clear all hits an occluded card), but
                    // don't update content view because card is occluded.
                    updateAdapter(i, item, true);
                }
            } else {
                // Update single item's content view from adapter
                updateAdapter(i, item, false);
            }
        }
    }

    @Override
    protected void updateAdapter(int i, CardStackViewItem item, boolean occluded) {
        // Let adapter create a view and add to item
        //Log.d(TAG, "Refresh view from adapter " + i);
        View contentView = item.getContentView();
        View child = mAdapter.getView(i, contentView, item);
        item.setTag(child.getTag()); // tag info needed to fake recents item
        if (!occluded && contentView == null) {
            //Log.d(TAG, "Add view from adapter " + i);
            // There is no existing content view that has been update, so
            // we need to add the newly created view to the item
            item.setContentView(child, getCardWidth(), getCardHeight());
        }
    }

    @Override
    public void setAdapter(TaskDescriptionAdapter adapter) {
        //Log.d(TAG, "Added adapter with size " + adapter.getCount());
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                updateAdapter();
                updateLayout();
            }

            public void onInvalidated() {
                updateAdapter();
                updateLayout();
            }
        });

        updateAdapter();
        updateLayout();
    }

    @Override
    public void onTouchOutside() {
        mRecentsActivity.dismissAndGoHome();
    }

    @Override
    public void setCallback(RecentsCallback callback) {
        mCallback = callback;
    }

    @Override
    public void setMinSwipeAlpha(float minAlpha) {
        mSwipeHelper.setMinAlpha(minAlpha);
    }

    @Override
    public View findViewForTask(int persistentTaskId) {
        for (CardStackViewItem item : mItems) {
            View view = item.getContentView();
            if (view != null) {
                RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) view.getTag();
                if (holder != null && holder.taskDescription.persistentTaskId == persistentTaskId) {
                    return view;
                }
            }
        }
        return null;
    }

    @Override
    public void drawFadedEdges(Canvas c, int left, int right, int top,
            int bottom) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOnScrollListener(Runnable listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void swipeAllViewsInLayout() {
        Thread clearAll = new Thread(new Runnable() {
            @Override
            public void run() {
                int count = mItems.size();
                // if we have more than one app, don't kill the current one
                if(count > 1) count--;
                View[] refView = new View[count];
                for (int i = 0; i < count; i++) {
                    refView[i] = mItems.get(i);
                }
                for (int i = 0; i < count; i++) {
                    final View child = refView[i];
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dismissChild(child);
                        }
                    });
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        // User will see the app fading instantly after the previous
                        // one. This will probably never happen
                    }
                }
                // we're done dismissing childs here, reset
                mClearAllAnimationDone = true;
            }
        });

        if (mClearAllAnimationDone) {
            mClearAllAnimationDone = false;
            clearAll.start();
        }
    }

    @Override
    public void removeViewInLayout(View view) {
        for (CardStackViewItem item : mItems) {
            if (item.getContentView() == view) {
                dismissChild(item);
            }
        }
    }
}
