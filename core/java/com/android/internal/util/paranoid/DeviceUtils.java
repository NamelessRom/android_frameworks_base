/*
* Copyright (C) 2013 SlimRoms Project
* Copyright (C) 2013 ParanoidAndroid Project
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.internal.util.paranoid;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.DisplayInfo;
import android.view.WindowManager;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;

import java.lang.RuntimeException;
import java.util.ArrayList;
import java.util.List;

public class DeviceUtils {

    private static final String SETTINGS_METADATA_NAME = "com.android.settings";

    // Device types
    private static final int DEVICE_PHONE  = 0;
    private static final int DEVICE_HYBRID = 1;
    private static final int DEVICE_TABLET = 2;

    public static boolean deviceSupportsUsbTether(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getTetherableUsbRegexs().length != 0);
    }

    public static boolean deviceSupportsMobileData(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean deviceSupportsBluetooth() {
        return (BluetoothAdapter.getDefaultAdapter() != null);
    }

    public static boolean deviceSupportsNfc(Context context) {
        return NfcAdapter.getDefaultAdapter(context) != null;
    }

    public static boolean deviceSupportsLte(Context context) {
        final TelephonyManager tm =
            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return (tm.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE)
                || (tm.getLteOnGsmMode() != 0);
    }

    public static boolean deviceSupportsGps(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    public static boolean deviceSupportsCameraFlash() {
        Camera camera;

        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            // camera not available
            return false;
        }

        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getFlashMode() == null) {
            camera.release();
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null
                || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1
                && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            camera.release();
            return false;
        }

        camera.release();
        return true;
    }

    private static int getScreenType(Context con) {
        WindowManager wm = (WindowManager)con.getSystemService(Context.WINDOW_SERVICE);
        DisplayInfo outDisplayInfo = new DisplayInfo();
        wm.getDefaultDisplay().getDisplayInfo(outDisplayInfo);
        int shortSize = Math.min(outDisplayInfo.logicalHeight, outDisplayInfo.logicalWidth);
        int shortSizeDp =
            shortSize * DisplayMetrics.DENSITY_DEFAULT / outDisplayInfo.logicalDensityDpi;
        if (shortSizeDp < 600) {
            return DEVICE_PHONE;
        } else if (shortSizeDp < 720) {
            return DEVICE_HYBRID;
        } else {
            return DEVICE_TABLET;
        }
    }

    public static boolean isPhone(Context con) {
        return getScreenType(con) == DEVICE_PHONE;
    }

    public static boolean isHybrid(Context con) {
        return getScreenType(con) == DEVICE_HYBRID;
    }

    public static boolean isTablet(Context con) {
        return getScreenType(con) == DEVICE_TABLET;
    }

}
