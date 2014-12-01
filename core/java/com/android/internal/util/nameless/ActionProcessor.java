/*
 * Copyright (C) 2013 Android Open Kang Project
 * Modifications Copyright (C) 2014 The NamelessRom Project
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

public class ActionProcessor {
    private static final String TAG = "ActionProcessor";

    private static final String ACTION_SCREEN_RECORD =
            "org.namelessrom.screencast.ACTION_SCREEN_RECORD";

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
                    case ACTION_SCREENSHOT: {
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
                    }
                    case ACTION_SCREENRECORD: {
                        final Intent intent = new Intent(ACTION_SCREEN_RECORD);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException anfe) {
                            Logger.e(TAG, "Could not start screen recording", anfe);
                        }
                        break;
                    }
                    case ACTION_APP: {
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
            }
        }).start();

        return true;
    }

}
