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

import java.util.ArrayList;
import java.util.List;

public class ActionConstants {

    public static enum ActionConstant {
        ACTION_NULL          { @Override public String value() { return "**null**";}},
        ACTION_APP           { @Override public String value() { return "**app**";}},
        ACTION_SCREENSHOT    { @Override public String value() { return "**screenshot**";}},
        ACTION_SCREENRECORD  { @Override public String value() { return "**screenrecord**";}};
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

    public static List<ActionConstant> Actions() {
        final ArrayList<ActionConstant> actionConstants = new ArrayList<>();
        actionConstants.add(ActionConstant.ACTION_NULL);
        actionConstants.add(ActionConstant.ACTION_SCREENSHOT);
        actionConstants.add(ActionConstant.ACTION_SCREENRECORD);

        return actionConstants;
    }

    public static String[] fromActionArray(final ActionConstant[] allTargets) {
        final int actions = allTargets.length;
        String[] values = new String[actions];
        for (int i = 0; i < actions; i++) {
            values[i] = allTargets[i].value();
        }
        return values;
    }

    public static String getProperName(final Context context, String actionString) {
        final Resources res = context.getResources();
        final ActionConstant action = fromString(actionString);
        switch (action) {
            case ACTION_SCREENSHOT:
                return res.getString(com.android.internal.R.string.action_screenshot);
            case ACTION_SCREENRECORD:
                return res.getString(com.android.internal.R.string.action_screenrecord);
            case ACTION_NULL:
                return res.getString(com.android.internal.R.string.action_null);
            default:
            case ACTION_APP:
                return res.getString(com.android.internal.R.string.action_app);
        }
    }

}
