/*
 * <!--
 *    Copyright (C) 2014 The NamelessRom Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -->
 */

package com.android.internal.util.nameless;

import android.os.SystemProperties;
import android.util.Log;

/**
 * A Logging utility
 */
public class Logger {
    private static boolean DEBUG = SystemProperties.getBoolean("persist.nameless.debug", false);

    public static synchronized void setEnabled(final boolean enable) {
        DEBUG = enable;
    }

    public static synchronized boolean getEnabled() {
        return DEBUG;
    }

    public static void d(final Object object, final String msg) {
        if (DEBUG) { Log.d(getTag(object), getMessage(msg)); }
    }

    public static void d(final Object object, final String msg, final Object... objects) {
        if (DEBUG) Log.d(getTag(object), getMessage(msg, objects));
    }

    public static void d(final Object object, final String msg, final Exception exception) {
        if (DEBUG) Log.d(getTag(object), getMessage(msg), exception);
    }

    public static void e(final Object object, final String msg) {
        if (DEBUG) Log.e(getTag(object), getMessage(msg));
    }

    public static void e(final Object object, final String msg, final Object... objects) {
        if (DEBUG) Log.e(getTag(object), getMessage(msg, objects));
    }

    public static void e(final Object object, final String msg, final Exception exception) {
        if (DEBUG) Log.e(getTag(object), getMessage(msg), exception);
    }

    public static void i(final Object object, final String msg) {
        if (DEBUG) Log.i(getTag(object), getMessage(msg));
    }

    public static void i(final Object object, final String msg, final Object... objects) {
        if (DEBUG) Log.i(getTag(object), getMessage(msg, objects));
    }

    public static void i(final Object object, final String msg, final Exception exception) {
        if (DEBUG) Log.i(getTag(object), getMessage(msg), exception);
    }

    public static void v(final Object object, final String msg) {
        if (DEBUG) Log.v(getTag(object), getMessage(msg));
    }

    public static void v(final Object object, final String msg, final Object... objects) {
        if (DEBUG) Log.v(getTag(object), getMessage(msg, objects));
    }

    public static void v(final Object object, final String msg, final Exception exception) {
        if (DEBUG) Log.v(getTag(object), getMessage(msg), exception);
    }

    public static void w(final Object object, final String msg) {
        if (DEBUG) Log.w(getTag(object), getMessage(msg));
    }

    public static void w(final Object object, final String msg, final Object... objects) {
        if (DEBUG) Log.w(getTag(object), getMessage(msg, objects));
    }

    public static void w(final Object object, final String msg, final Exception exception) {
        if (DEBUG) Log.w(getTag(object), getMessage(msg), exception);
    }

    public static void wtf(final Object object, final String msg) {
        if (DEBUG) Log.wtf(getTag(object), getMessage(msg));
    }

    public static void wtf(final Object object, final String msg, final Object... objects) {
        if (DEBUG) Log.wtf(getTag(object), getMessage(msg, objects));
    }

    public static void wtf(final Object object, final String msg, final Exception exception) {
        if (DEBUG) Log.wtf(getTag(object), getMessage(msg), exception);
    }

    public static String getTag(final Object object) {
        if (object instanceof String) {
            return ((String) object);
        } else {
            return object.getClass().getSimpleName();
        }
    }

    public static String getMessage(final String msg) {
        return String.format("--> %s", msg);
    }

    public static String getMessage(final String msg, final Object... objects) {
        return String.format("--> %s", String.format(msg, objects));
    }

}
