package com.android.systemui.quicksettings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;

import com.android.internal.util.nameless.FlashlightConstants;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsController;

public class TorchTile extends QuickSettingsTile {
    private boolean mActive = false;

    public TorchTile(Context context, 
            QuickSettingsController qsc, Handler handler) {
        super(context, qsc);

        mOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FlashlightConstants.ACTION_TOGGLE_STATE);
                mContext.sendBroadcast(i);
            }
        };

        mOnLongClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(FlashlightConstants.INTENT_LAUNCH_APP);
                return true;
            }
        };

        qsc.registerAction(FlashlightConstants.ACTION_STATE_CHANGED, this);
    }

    @Override
    void onPostCreate() {
        updateTile();
        super.onPostCreate();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    private synchronized void updateTile() {
        if (mActive) {
            mDrawable = R.drawable.ic_qs_torch_on;
            mLabel = mContext.getString(R.string.quick_settings_torch);
        } else {
            mDrawable = R.drawable.ic_qs_torch_off;
            mLabel = mContext.getString(R.string.quick_settings_torch_off);
        }
    }

}
