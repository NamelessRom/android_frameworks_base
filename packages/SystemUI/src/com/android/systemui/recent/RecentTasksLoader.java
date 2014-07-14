/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RecentTasksLoader implements View.OnTouchListener {
    static final String TAG = "RecentTasksLoader";
    static final boolean DEBUG = PhoneStatusBar.DEBUG || false;

    private static final int DISPLAY_TASKS = 20;
    private static final int MAX_TASKS = DISPLAY_TASKS + 1; // allow extra for non-apps

    private Context mContext;
    private RecentsPanelView mRecentsPanel;

    private Object mFirstTaskLock = new Object();
    private TaskDescription mFirstTask;
    private boolean mFirstTaskLoaded;

    private AsyncTask<Void, ArrayList<TaskDescription>, Void> mTaskLoader;
    private AsyncTask<Void, TaskDescription, Void> mThumbnailLoader;
    private Handler mHandler;

    private int mIconDpi;
    private ColorDrawableWithDimensions mDefaultThumbnailBackground;
    private ColorDrawableWithDimensions mDefaultIconBackground;
    private int mNumTasksInFirstScreenful = Integer.MAX_VALUE;

    private boolean mFirstScreenful;
    private ArrayList<TaskDescription> mLoadedTasks;

    private enum State { LOADING, LOADED, CANCELLED };
    private State mState = State.CANCELLED;

    private static final int EDGE_DETECTION_MAX_DIFF = 30;
    private static final int EDGE_DETECTION_SKIP_AMOUNT = 30; // dp
    private static final int EDGE_DETECTION_SCAN_AMOUNT = 60; // dp
    private static final int GRAYSCALE_THRESHOLD_DARK = 192;
    private boolean mUseCardStack;
    private int mDefaultAppBarColor;
    private int mEdgeDetectionScanPixels;
    private int mEdgeDetectionSkipPixels;
    private ArrayList<TaskDescription> mReusableTasks;


    private static RecentTasksLoader sInstance;
    public static RecentTasksLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RecentTasksLoader(context);
        }
        sInstance.mUseCardStack = Recents.mUseCardStack;
        return sInstance;
    }

    private RecentTasksLoader(Context context) {
        mContext = context;
        mHandler = new Handler();

        final Resources res = context.getResources();

        // get the icon size we want -- on tablets, we use bigger icons
        boolean isTablet = res.getBoolean(R.bool.config_recents_interface_for_tablets);
        if (isTablet) {
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            mIconDpi = activityManager.getLauncherLargeIconDensity();
        } else {
            mIconDpi = res.getDisplayMetrics().densityDpi;
        }

        // Render default icon (just a blank image)
        int defaultIconSize = res.getDimensionPixelSize(com.android.internal.R.dimen.app_icon_size);
        int iconSize = (int) (defaultIconSize * mIconDpi / res.getDisplayMetrics().densityDpi);
        mDefaultIconBackground = new ColorDrawableWithDimensions(0x00000000, iconSize, iconSize);

        // Render the default thumbnail background
        int thumbnailWidth =
                (int) res.getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_width);
        int thumbnailHeight =
                (int) res.getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_height);
        int color = res.getColor(R.drawable.status_bar_recents_app_thumbnail_background);

        mDefaultThumbnailBackground =
                new ColorDrawableWithDimensions(color, thumbnailWidth, thumbnailHeight);

        mDefaultAppBarColor = res.getColor(R.color.status_bar_recents_app_bar_color);
        mEdgeDetectionSkipPixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                EDGE_DETECTION_SKIP_AMOUNT, res.getDisplayMetrics());
        mEdgeDetectionScanPixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                EDGE_DETECTION_SCAN_AMOUNT, res.getDisplayMetrics());
    }

    public void setRecentsPanel(RecentsPanelView newRecentsPanel, RecentsPanelView caller) {
        // Only allow clearing mRecentsPanel if the caller is the current recentsPanel
        if (newRecentsPanel != null || mRecentsPanel == caller) {
            mRecentsPanel = newRecentsPanel;
            if (mRecentsPanel != null) {
                mNumTasksInFirstScreenful = mRecentsPanel.numItemsInOneScreenful();
            }
        }
    }

    public Drawable getDefaultThumbnail() {
        return mDefaultThumbnailBackground;
    }

    public Drawable getDefaultIcon() {
        return mDefaultIconBackground;
    }

    public ArrayList<TaskDescription> getLoadedTasks() {
        return mLoadedTasks;
    }

    public void remove(TaskDescription td) {
        mLoadedTasks.remove(td);
    }

    public boolean isFirstScreenful() {
        return mFirstScreenful;
    }

    private boolean isCurrentHomeActivity(ComponentName component, ActivityInfo homeInfo) {
        if (homeInfo == null) {
            final PackageManager pm = mContext.getPackageManager();
            homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .resolveActivityInfo(pm, 0);
        }
        return homeInfo != null
            && homeInfo.packageName.equals(component.getPackageName())
            && homeInfo.name.equals(component.getClassName());
    }

    // Create an TaskDescription, returning null if the title or icon is null
    TaskDescription createTaskDescription(int taskId, int persistentTaskId, Intent baseIntent,
            ComponentName origActivity, CharSequence description, TaskDescription reuseTask) {
        Intent intent = new Intent(baseIntent);
        if (origActivity != null) {
            intent.setComponent(origActivity);
        }
        final PackageManager pm = mContext.getPackageManager();
        intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            final ActivityInfo info = resolveInfo.activityInfo;
            final String title = info.loadLabel(pm).toString();

            if (title != null && title.length() > 0) {
                if (DEBUG) Log.v(TAG, "creating activity desc for id="
                        + persistentTaskId + ", label=" + title);

                TaskDescription item = new TaskDescription(taskId,
                        persistentTaskId, resolveInfo, baseIntent, info.packageName,
                        description);
                item.setLabel(title);

                if (mUseCardStack) {
                    if (reuseTask != null) {
                        //Log.v(TAG, "Reuse task description color: " + reuseTask.getABColor());
                        item.setABHeight(reuseTask.getABHeight());
                        item.setABColor(reuseTask.getABColor());
                        item.setABUseLight(reuseTask.getABUseLight());
                    } else {
                        item.setABHeight(0);
                        item.setABColor(mDefaultAppBarColor);
                        item.setABUseLight(false);
                    }
                }

                return item;
            } else {
                if (DEBUG) Log.v(TAG, "SKIPPING item " + persistentTaskId);
            }
        }
        return null;
    }

    boolean isEdgeAtPosition(int[] pixels, int position) {
        int r1, r2, r3, g1, g2, g3, b1, b2, b3, diff;
        int val = pixels[position-1];

        r1 = (val >> 16) & 0xff;
        g1 = (val >> 8) & 0xff;
        b1 =  val & 0xff;

        val = pixels[position];

        r2 = (val >> 16) & 0xff;
        g2 = (val >> 8) & 0xff;
        b2 =  val & 0xff;

        val = pixels[position+1];

        r3 = (val >> 16) & 0xff;
        g3 = (val >> 8) & 0xff;
        b3 =  val & 0xff;

        diff = Math.abs(r1 - r3);
        diff += Math.abs(g1 - g3);
        diff += Math.abs(b1 - b3);

        return diff > EDGE_DETECTION_MAX_DIFF;
    }

    int detectEdge(int[] pixels, int length) {
        // simple 1D Laplacian operator for edge detection on each channel
        if (length > mEdgeDetectionSkipPixels+1) {
            int r1, r2, r3, g1, g2, g3, b1, b2, b3, diff;
            int val = pixels[mEdgeDetectionSkipPixels-1];

            r1 = (val >> 16) & 0xff;
            g1 = (val >> 8) & 0xff;
            b1 = val & 0xff;

            val = pixels[mEdgeDetectionSkipPixels];

            r2 = (val >> 16) & 0xff;
            g2 = (val >> 8) & 0xff;
            b2 = val & 0xff;

            for (int i = mEdgeDetectionSkipPixels+1; i < length; ++i) {
                diff = 0;
                val = pixels[i];

                r3 = (val >> 16) & 0xff;
                g3 = (val >> 8) & 0xff;
                b3 = val & 0xff;

                // do the math for each channel and fuse in diff
                diff += Math.abs(r1 - r3);
                diff += Math.abs(g1 - g3);
                diff += Math.abs(b1 - b3);

                if (diff > EDGE_DETECTION_MAX_DIFF) {
                    return i-1;
                }

                // move on to next pixel
                r1 = r2;
                g1 = g2;
                b1 = b2;
                r2 = r3;
                g2 = g3;
                b2 = b3;
            }
        }

        return -1;
    }

    void loadThumbnailAndIcon(TaskDescription td) {
        final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        final PackageManager pm = mContext.getPackageManager();
        Bitmap thumbnail = am.getTaskTopThumbnail(td.persistentTaskId);

        if (thumbnail != null && (td.intent.getFlags() & Intent.FLAG_FLOATING_WINDOW) != 0) {
            // See Activity.java:5300 for the values
            int x = Math.round(thumbnail.getWidth() * 0.05f);
            int y = Math.round(thumbnail.getHeight() * 0.15f);
            int width = Math.round(thumbnail.getWidth() * 0.9f);
            Bitmap tmp = Bitmap.createBitmap(thumbnail, x, y, width, thumbnail.getHeight() - y);
            thumbnail.recycle();
            thumbnail = tmp;
        }

        Drawable icon = getFullResIcon(td.resolveInfo, pm);

        if (DEBUG) Log.v(TAG, "Loaded bitmap for task "
                + td + ": " + thumbnail);
        synchronized (td) {
            int abHeight = 0;
            int abColor = mDefaultAppBarColor;

            if (thumbnail != null) {
                td.setThumbnail(new BitmapDrawable(mContext.getResources(), thumbnail));

                if (mUseCardStack) {
                    int maxHeight = Math.min(thumbnail.getHeight(), mEdgeDetectionScanPixels);
                    if (mEdgeDetectionSkipPixels < maxHeight) {
                        // Crop bitmap to single rightmost column of pixels,
                        // starting at mEdgeDetectionSkipPixels till maxHeight
                        Bitmap thumbRight = Bitmap.createBitmap(thumbnail, thumbnail.getWidth()-1, 0, 1, maxHeight);
                        Bitmap thumbLeft = Bitmap.createBitmap(thumbnail, 0, 0, 1, maxHeight);

                        // Obtain pixels from bitmap
                        int[] pixelsRight = new int[maxHeight];
                        thumbRight.getPixels(pixelsRight, 0, 1, 0, 0, 1, maxHeight);
                        int[] pixelsLeft = new int[maxHeight];
                        thumbLeft.getPixels(pixelsLeft, 0, 1, 0, 0, 1, maxHeight);

                        // Detect edges on the right side
                        abHeight = detectEdge(pixelsRight, maxHeight);

                        // Check abHeight and if there is also an edge on the left
                        if (abHeight != -1 && isEdgeAtPosition(pixelsLeft, abHeight)) {
                            //Log.v(TAG, "Edge found at " + abHeight);
                            abColor = pixelsRight[abHeight-1];
                        } else {
                            //Log.v(TAG, "No edge found");
                            abHeight = 0;

                            int curColor = td.getABColor();
                            if (curColor != 0 && curColor != mDefaultAppBarColor) {
                                // Reuse existing color
                                abColor = curColor;
                            } else {
                                // Take top color if its the same left and right
                                if (pixelsRight[0] == pixelsLeft[0]) {
                                    abColor = pixelsRight[0];
                                }
                            }
                        }
                        thumbRight.recycle();
                        thumbLeft.recycle();
                    }
                }
            } else {
                td.setThumbnail(mDefaultThumbnailBackground);
            }
            if (icon != null) {
                td.setIcon(icon);
            }

            if (mUseCardStack) {
                td.setABHeight(abHeight);
                td.setABColor(abColor);
                td.setABUseLight(.2126f * ((abColor >> 16) & 0xff)
                               + .7152f * ((abColor >> 8) & 0xff)
                               + .0722f * (abColor & 0xff) < GRAYSCALE_THRESHOLD_DARK);
            }
            td.setLoaded(true);
        }
    }

    Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                com.android.internal.R.mipmap.sym_def_app_icon);
    }

    Drawable getFullResIcon(Resources resources, int iconId) {
        try {
            return resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            return getFullResDefaultActivityIcon();
        }
    }

    private Drawable getFullResIcon(ResolveInfo info, PackageManager packageManager) {
        Resources resources;
        try {
            resources = packageManager.getResourcesForApplication(
                    info.activityInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.activityInfo.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    Runnable mPreloadTasksRunnable = new Runnable() {
            public void run() {
                loadTasksInBackground();
            }
        };

    // additional optimization when we have software system buttons - start loading the recent
    // tasks on touch down
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            preloadRecentTasksList();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            cancelPreloadingRecentTasksList();
        } else if (action == MotionEvent.ACTION_UP) {
            // Remove the preloader if we haven't called it yet
            mHandler.removeCallbacks(mPreloadTasksRunnable);
            if (!v.isPressed()) {
                cancelLoadingThumbnailsAndIcons();
            }

        }
        return false;
    }

    public void preloadRecentTasksList() {
        mHandler.post(mPreloadTasksRunnable);
    }

    public void cancelPreloadingRecentTasksList() {
        cancelLoadingThumbnailsAndIcons();
        mHandler.removeCallbacks(mPreloadTasksRunnable);
    }

    public void cancelLoadingThumbnailsAndIcons(RecentsPanelView caller) {
        // Only oblige this request if it comes from the current RecentsPanel
        // (eg when you rotate, the old RecentsPanel request should be ignored)
        if (mRecentsPanel == caller) {
            cancelLoadingThumbnailsAndIcons();
        }
    }


    private void cancelLoadingThumbnailsAndIcons() {
        if (mRecentsPanel != null && mRecentsPanel.isShowing()) {
            return;
        }

        if (mTaskLoader != null) {
            mTaskLoader.cancel(true);
            mTaskLoader = null;
        }
        if (mThumbnailLoader != null) {
            mThumbnailLoader.cancel(true);
            mThumbnailLoader = null;
        }
        mLoadedTasks = null;
        if (mRecentsPanel != null) {
            mRecentsPanel.onTaskLoadingCancelled();
        }
        mFirstScreenful = false;
        mState = State.CANCELLED;
    }

    private void clearFirstTask() {
        synchronized (mFirstTaskLock) {
            mFirstTask = null;
            mFirstTaskLoaded = false;
        }
    }

    public void preloadFirstTask() {
        Thread bgLoad = new Thread() {
            public void run() {
                TaskDescription first = loadFirstTask();
                synchronized(mFirstTaskLock) {
                    if (mCancelPreloadingFirstTask) {
                        clearFirstTask();
                    } else {
                        mFirstTask = first;
                        mFirstTaskLoaded = true;
                    }
                    mPreloadingFirstTask = false;
                }
            }
        };
        synchronized(mFirstTaskLock) {
            if (!mPreloadingFirstTask) {
                clearFirstTask();
                mPreloadingFirstTask = true;
                bgLoad.start();
            }
        }
    }

    public void cancelPreloadingFirstTask() {
        synchronized(mFirstTaskLock) {
            if (mPreloadingFirstTask) {
                mCancelPreloadingFirstTask = true;
            } else {
                clearFirstTask();
            }
        }
    }

    boolean mPreloadingFirstTask;
    boolean mCancelPreloadingFirstTask;
    public TaskDescription getFirstTask() {
        while(true) {
            synchronized(mFirstTaskLock) {
                if (mFirstTaskLoaded) {
                    return mFirstTask;
                } else if (!mFirstTaskLoaded && !mPreloadingFirstTask) {
                    mFirstTask = loadFirstTask();
                    mFirstTaskLoaded = true;
                    return mFirstTask;
                }
            }
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
            }
        }
    }

    public TaskDescription loadFirstTask() {
        final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        final List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasksForUser(
                1, ActivityManager.RECENT_IGNORE_UNAVAILABLE, UserHandle.CURRENT.getIdentifier());
        TaskDescription item = null;
        if (recentTasks.size() > 0) {
            ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(0);

            Intent intent = new Intent(recentInfo.baseIntent);
            if (recentInfo.origActivity != null) {
                intent.setComponent(recentInfo.origActivity);
            }

            // Don't load the current home activity.
            if (isCurrentHomeActivity(intent.getComponent(), null)) {
                return null;
            }

            // Don't load ourselves
            if (intent.getComponent().getPackageName().equals(mContext.getPackageName())) {
                return null;
            }

            item = createTaskDescription(recentInfo.id,
                    recentInfo.persistentId, recentInfo.baseIntent,
                    recentInfo.origActivity, recentInfo.description, null);
            if (item != null) {
                loadThumbnailAndIcon(item);
            }
            return item;
        }
        return null;
    }

    public TaskDescription findFirstTask() {
        final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        final List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasksForUser(
                MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE, UserHandle.CURRENT.getIdentifier());
        TaskDescription item = null;
        int i = -1;
        for (ActivityManager.RecentTaskInfo recentInfo : recentTasks) {
            i++;
            Intent intent = new Intent(recentInfo.baseIntent);
            if (recentInfo.origActivity != null) {
                intent.setComponent(recentInfo.origActivity);
            }

            // Don't load the current home activity
            if (isCurrentHomeActivity(intent.getComponent(), null)) {
                continue;
            }

            // Don't load ourselves
            if (intent.getComponent().getPackageName().equals(mContext.getPackageName())) {
                continue;
            }

            item = createTaskDescription(recentInfo.id,
                    recentInfo.persistentId, recentInfo.baseIntent,
                    recentInfo.origActivity, recentInfo.description, null);
            if (item != null) {
                loadThumbnailAndIcon(item);
            }
            // don't load the first activity
            if (i == 0) {
                continue;
            }
            return item;
        }
        return null;
    }

    public void loadTasksInBackground() {
        if (mUseCardStack && mRecentsPanel != null) {
            mReusableTasks = mRecentsPanel.getReuseTaskDescriptions();
        }
        loadTasksInBackground(false);
    }
    public void loadTasksInBackground(final boolean zeroeth) {
        if (mState != State.CANCELLED) {
            return;
        }
        mState = State.LOADING;
        mFirstScreenful = true;

        final LinkedBlockingQueue<TaskDescription> tasksWaitingForThumbnails =
                new LinkedBlockingQueue<TaskDescription>();
        mTaskLoader = new AsyncTask<Void, ArrayList<TaskDescription>, Void>() {
            @Override
            protected void onProgressUpdate(ArrayList<TaskDescription>... values) {
                if (!isCancelled()) {
                    ArrayList<TaskDescription> newTasks = values[0];
                    // do a callback to RecentsPanelView to let it know we have more values
                    // how do we let it know we're all done? just always call back twice
                    if (mRecentsPanel != null) {
                        mRecentsPanel.onTasksLoaded(newTasks, mFirstScreenful);
                    }
                    if (mLoadedTasks == null) {
                        mLoadedTasks = new ArrayList<TaskDescription>();
                    }
                    mLoadedTasks.addAll(newTasks);
                    mFirstScreenful = false;
                }
            }
            @Override
            protected Void doInBackground(Void... params) {
                // We load in two stages: first, we update progress with just the first screenful
                // of items. Then, we update with the rest of the items
                final int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                final PackageManager pm = mContext.getPackageManager();
                final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);

                final List<ActivityManager.RecentTaskInfo> recentTasks =
                        am.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
                int numTasks = recentTasks.size();
                ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);

                boolean firstScreenful = true;
                ArrayList<TaskDescription> tasks = new ArrayList<TaskDescription>();

                // skip the first task - assume it's either the home screen or the current activity.
                final int first = 0;
                for (int i = first, index = 0; i < numTasks && (index < MAX_TASKS); ++i) {
                    if (isCancelled()) {
                        break;
                    }
                    final ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(i);

                    Intent intent = new Intent(recentInfo.baseIntent);
                    if (recentInfo.origActivity != null) {
                        intent.setComponent(recentInfo.origActivity);
                    }

                    // Don't load the current home activity.
                    if (isCurrentHomeActivity(intent.getComponent(), homeInfo)) {
                        continue;
                    }

                    // Don't load ourselves
                    if (intent.getComponent().getPackageName().equals(mContext.getPackageName())) {
                        continue;
                    }

                    TaskDescription item = createTaskDescription(recentInfo.id,
                            recentInfo.persistentId, recentInfo.baseIntent,
                            recentInfo.origActivity, recentInfo.description, reuseTask);

                    if (item != null) {
                        while (true) {
                            try {
                                tasksWaitingForThumbnails.put(item);
                                break;
                            } catch (InterruptedException e) {
                                if (isCancelled()) {
                                    return null;
                                }
                            }
                        }
                        tasks.add(item);
                        if (firstScreenful && tasks.size() == mNumTasksInFirstScreenful) {
                            publishProgress(tasks);
                            tasks = new ArrayList<TaskDescription>();
                            firstScreenful = false;
                            //break;
                        }
                        ++index;
                    }
                }

                if (mUseCardStack && mReusableTasks != null) {
                    // Clear reusable tasks, not absolutely necessary though
                    mReusableTasks = null;
                }

                if (!isCancelled()) {
                    publishProgress(tasks);
                    if (firstScreenful) {
                        // always should publish two updates
                        publishProgress(new ArrayList<TaskDescription>());
                    }
                }

                while (true) {
                    try {
                        tasksWaitingForThumbnails.put(new TaskDescription());
                        break;
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            return null;
                        }
                    }
                }

                Process.setThreadPriority(origPri);
                return null;
            }
        };
        mTaskLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        loadThumbnailsAndIconsInBackground(tasksWaitingForThumbnails);
    }

    private void loadThumbnailsAndIconsInBackground(
            final BlockingQueue<TaskDescription> tasksWaitingForThumbnails) {
        // continually read items from tasksWaitingForThumbnails and load
        // thumbnails and icons for them. finish thread when cancelled or there
        // is a null item in tasksWaitingForThumbnails
        mThumbnailLoader = new AsyncTask<Void, TaskDescription, Void>() {
            @Override
            protected void onProgressUpdate(TaskDescription... values) {
                if (!isCancelled()) {
                    TaskDescription td = values[0];
                    if (td.isNull()) { // end sentinel
                        mState = State.LOADED;
                    } else {
                        if (mRecentsPanel != null) {
                            mRecentsPanel.onTaskThumbnailLoaded(td);
                        }
                    }
                }
            }
            @Override
            protected Void doInBackground(Void... params) {
                final int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                while (true) {
                    if (isCancelled()) {
                        break;
                    }
                    TaskDescription td = null;
                    while (td == null) {
                        try {
                            td = tasksWaitingForThumbnails.take();
                        } catch (InterruptedException e) {
                            if (isCancelled()) {
                                return null;
                            }
                        }
                    }
                    if (td.isNull()) { // end sentinel
                        publishProgress(td);
                        break;
                    }
                    loadThumbnailAndIcon(td);

                    publishProgress(td);
                }

                Process.setThreadPriority(origPri);
                return null;
            }
        };
        mThumbnailLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
