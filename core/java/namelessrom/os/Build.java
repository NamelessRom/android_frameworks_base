/*
 * Copyright (C) 2015 The CyanogenMod Project
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

package namelessrom.os;

import android.os.SystemProperties;

/**
 * Information about the current NamelessRom build, extracted from system properties.
 */
public class Build {
    /** Value used for when a build property is unknown. */
    public static final String UNKNOWN = "unknown";

    /** The timestamp of the current build in the format of YYYY/MM/DD */
    public static final int DATE = SystemProperties.getInt("ro.nameless.date", -1);

    /** The codename of the device (for example bacon, find7, i9300, ...) */
    public static final String DEVICE = SystemProperties.get("ro.nameless.device", UNKNOWN);

    /** @hide */
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.nameless.debug", false);

    /** @hide */
    public static final String OTA_URL = SystemProperties.get("ro.nameless.ota.download",
            "https://sourceforge.net/projects/namelessrom/files/n-3.0/%s/");
}
