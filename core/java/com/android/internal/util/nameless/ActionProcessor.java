package com.android.internal.util.nameless;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;

import com.android.internal.statusbar.IStatusBarService;

import java.net.URISyntaxException;

import static com.android.internal.util.nameless.ActionConstants.ActionConstant;
import static com.android.internal.util.nameless.ActionConstants.fromString;

/**
 * Created by alex on 01.12.14.
 */
public class ActionProcessor {
    private static final String TAG = "ActionProcessor";

    private ActionProcessor() {
        // no operation
    }

    public static boolean launchAction(final Context context, final String actionString) {
        if (TextUtils.isEmpty(actionString)
                || TextUtils.equals(actionString, ActionConstant.ACTION_NULL.value())) {
            return false;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final ActionConstant action = fromString(actionString);
                switch (action) {
                    case ACTION_SCREENSHOT:
                        final IStatusBarService barService = IStatusBarService.Stub.asInterface(
                                ServiceManager.getService(Context.STATUS_BAR_SERVICE));
                        if (barService != null) {
                            try {
                                barService.toggleScreenshot();
                            } catch (RemoteException exc) {
                                Logger.e(TAG, "RemoteException!", exc);
                            }
                        }
                        break;
                    case ACTION_APP:
                        try {
                            final Intent intent = Intent.parseUri(actionString, 0);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        } catch (URISyntaxException e) {
                            Logger.e(TAG, "URISyntaxException: [%s]", actionString);
                        } catch (ActivityNotFoundException e) {
                            Logger.e(TAG, "ActivityNotFound: [%s]", actionString);
                        }
                        break;
                }
            }
        }).start();

        return true;
    }

}
