/*
 * <!--
 *    Copyright (C) 2014 The NamelessROM Project
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */

package com.android.internal.util.nameless;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;

public class NamelessActions {
    private static final String TAG = "NamelessActions";

    public static void processAction(final Context c, final ActionConstants.NamelessAction action) {
        switch (action) {
            case ACTION_ONTHEGO_TOGGLE:
                actionOnTheGoToggle(c);
                break;
            case ACTION_SCREEN_RECORD:
                actionScreenRecord(c);
                break;
            case ACTION_TOGGLE_GESTURE_PANEL:
                final Intent i = new Intent(Intent.ACTION_TOGGLE_GESTURE_PANEL);
                c.sendBroadcastAsUser(i, UserHandle.CURRENT);
                break;
            default:
                Log.w(TAG, "--> action not found");
                break;
        }
    }

    private static void actionOnTheGoToggle(final Context context) {
        final ComponentName cn = new ComponentName("com.android.systemui",
                "com.android.systemui.nameless.onthego.OnTheGoService");
        final Intent startIntent = new Intent();
        startIntent.setComponent(cn);
        startIntent.setAction("start");
        context.startService(startIntent);
    }

    private static void actionScreenRecord(final Context context) {
        final ComponentName cn = new ComponentName("org.namelessrom.screencast",
                "org.namelessrom.screencast.MainActivity");
        final Intent startIntent = new Intent();
        startIntent.setComponent(cn);
        context.startActivityAsUser(startIntent, UserHandle.CURRENT_OR_SELF);
    }

    public static void turnScreenOff(final Context context) {
        final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.goToSleep(SystemClock.uptimeMillis());
    }

}
