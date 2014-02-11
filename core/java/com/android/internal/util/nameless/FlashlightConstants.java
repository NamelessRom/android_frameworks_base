/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Copyright (C) 2013 ParanoidAndroid Project
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

import android.content.Intent;

public class FlashlightConstants {
    /**
     * Package name of the flashlight app
     */
    public static final String APP_PACKAGE_NAME = "com.nameless.flashlight";

    /**
     * Intent broadcast action for toggling the torch state
     */
    public static final String ACTION_TOGGLE_STATE = APP_PACKAGE_NAME + ".TOGGLE_FLASHLIGHT";

    /**
     * Intent broadcast action for turning the torch off
     */
    public static final String ACTION_OFF = APP_PACKAGE_NAME + ".FLASHLIGHT_OFF";

    /**
     * Intent broadcast action for turning the torch on
     */
    public static final String ACTION_ON = APP_PACKAGE_NAME + ".FLASHLIGHT_ON";
    /**
     * Intent for launching the torch application
     */
    public static Intent INTENT_LAUNCH_APP = new Intent(Intent.ACTION_MAIN)
            .setClassName(APP_PACKAGE_NAME, APP_PACKAGE_NAME + ".MainActivity");
}
