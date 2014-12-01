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

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

public class ActionConstants {

    public static enum ActionConstant {
        ACTION_NULL          { @Override public String value() { return "**null**";}},
        ACTION_APP           { @Override public String value() { return "**app**";}},
        ACTION_SCREENSHOT    { @Override public String value() { return "**screenshot**";}};
        public String value() { return this.value(); }
    }

    public static ActionConstant fromString(final String string) {
        if (TextUtils.isEmpty(string)) {
            return ActionConstant.ACTION_NULL;
        }

        final ActionConstant[] actions = ActionConstant.values();
        for (ActionConstant action : actions) {
            if (TextUtils.equals(string, action.value())) {
                return action;
            }
        }
        // not in ENUM must be custom
        return ActionConstant.ACTION_APP;
    }

    public static String[] Actions() {
        return fromActionArray(ActionConstant.values());
    }

    public static String[] fromActionArray(final ActionConstant[] allTargets) {
        final int actions = allTargets.length;
        String[] values = new String [actions];
        for (int i = 0; i < actions; i++) {
            values [i] = allTargets[i].value();
        }
        return values;
    }

    /*public static Drawable getSystemUIDrawable(Context mContext, String DrawableID) {
        Resources res = mContext.getResources();
        PackageManager pm = mContext.getPackageManager();
        int resId = 0;
        Drawable d = null;
        if (pm != null) {
            Resources mSystemUiResources = null;
            try {
                mSystemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
            }

            if (mSystemUiResources != null && DrawableID != null) {
                resId = mSystemUiResources.getIdentifier(DrawableID, null, null);
            }
            if (resId > 0) {
                try {
                    d = mSystemUiResources.getDrawable(resId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return d;
    }*/

    public static String getProperName(final Context context, String actionString) {
        // Will return a string for the associated action, but will need the caller's context to get resources.
        final Resources res = context.getResources();
        String value;
        final ActionConstant action = fromString(actionString);
        switch (action) {
            case ACTION_SCREENSHOT:
                value = res.getString(com.android.internal.R.string.action_screenshot);
                break;
            /*case ACTION_APP:
                value = res.getString(com.android.internal.R.string.action_app);
                break;*/
            case ACTION_NULL:
            default:
                value = res.getString(com.android.internal.R.string.action_null);
                break;

        }
        return value;
    }
    /*public static Drawable getActionIcon(Context context,String actionString) {
        // Will return a Drawable for the associated action, but will need the caller's context to get resources.
        Resources res = context.getResources();
        Drawable value = null;
        ActionConstant action = fromString(actionString);
        switch (action) {
            case ACTION_SCREENSHOT:
                value = getSystemUIDrawable(context, "com.android.systemui:drawable/ic_action_screenshot");
                break;
            case ACTION_APP: // APP doesn't really have an icon - it should look up
                //the package icon - we'll return the 'null' on just in case
            case ACTION_NULL:
            default:
                value = getSystemUIDrawable(context, "com.android.systemui:drawable/ic_action_null");
                break;

        }
        return value;
    }*/

}
