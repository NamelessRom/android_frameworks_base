package com.android.systemui.quicksettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.View;

import com.android.systemui.nameless.quicktiles.ExtensionListing;
import com.android.systemui.statusbar.phone.QuickSettingsController;

import org.namelessrom.quicktiles.api.ExtensionTile;

/**
 * Created by alex on 7/27/14.
 */
public class ExtensionQuickTile extends QuickSettingsTile {

    private static final String TAG = "ExtensionTile";

    private ExtensionListing mExtensionListing;
    private ExtensionTile    mExtensionTile;

    private Resources mRemoteResources;

    private boolean isSupported;

    public ExtensionQuickTile(final Context context, final QuickSettingsController qsc,
            final ExtensionListing listing) {
        super(context, qsc);
        this.mExtensionListing = listing;

        mDrawable = -1;
        mDrawableFull = mExtensionListing.icon;

        isSupported = createConnection(mExtensionListing.componentName);

        mOnClick = new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (mExtensionTile != null) {
                    Intent intent;
                    try {
                        if (mExtensionTile.isActivated()) {
                            intent = mExtensionTile.getIntentActivated();
                        } else {
                            intent = mExtensionTile.getIntentDeactivated();
                        }
                    } catch (RemoteException exception) {
                        intent = null;
                    }
                    if (intent != null) {
                        mContext.sendBroadcast(intent);
                    }
                }
            }
        };

        try {
            mRemoteResources = mContext.getPackageManager()
                    .getResourcesForApplication(mExtensionListing.componentName.getPackageName());
        } catch (Exception exc) {
            mRemoteResources = null;
            isSupported = false;
        }
    }

    @Override void updateQuickSettings() {
        mTile.setVisibility(isSupported ? View.VISIBLE : View.INVISIBLE);
        super.updateQuickSettings();
    }

    @Override public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        if (mExtensionTile != null) {
            String tmp;
            try {
                tmp = mExtensionTile.isActivated()
                        ? mExtensionTile.getTitleActivated()
                        : mExtensionTile.getTitleDeactivated();
                mLabel = (tmp != null ? tmp : mLabel);
            } catch (RemoteException exception) { }

            try {
                tmp = mExtensionTile.isActivated()
                        ? mExtensionTile.getIconActivated()
                        : mExtensionTile.getIconDeactivated();
            } catch (RemoteException exception) {
                tmp = null;
            }
            if (tmp != null && !tmp.isEmpty()) {
                try {
                    mDrawableFull = mRemoteResources.getDrawable(
                            mRemoteResources.getIdentifier(tmp, "drawable",
                                    mExtensionListing.componentName.getPackageName()));
                } catch (Exception exc) {
                    Slog.e(TAG, "Error getting drawable: " + exc.getMessage());
                    mDrawableFull = mExtensionListing.icon;
                }
            }
        } else {
            isSupported = false;
        }
    }

    private boolean createConnection(final ComponentName cn) {
        try {
            if (!mContext.bindService(new Intent().setComponent(cn), serviceConnection,
                    Context.BIND_AUTO_CREATE)) {
                Slog.e(TAG, "Error binding to extension " + cn.flattenToShortString());
                return false;
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "Error binding to extension " + cn.flattenToShortString(), e);
            return false;
        }
        return true;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mExtensionTile = ExtensionTile.Stub.asInterface(iBinder);
            try {
                isSupported = mExtensionTile.isSupported();
            } catch (RemoteException exception) {
                isSupported = false;
            }
            updateResources();
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mExtensionTile = null;
            updateResources();
        }
    };

}
