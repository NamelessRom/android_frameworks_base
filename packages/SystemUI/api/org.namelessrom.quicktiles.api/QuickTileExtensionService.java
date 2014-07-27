package org.namelessrom.quicktiles.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.namelessrom.quicktiles.api.ExtensionTileData;

/**
 * Created by alex on 7/27/14.
 */
public abstract class QuickTileExtensionService extends Service {

    private static final String ACTION_START = "action_start";
    private static final String ACTION_STOP  = "action_stop";

    public static void start(final Context context) {
        final Intent i = new Intent(context, QuickTileExtensionService.class);
        i.setAction(ACTION_START);
        context.startService(i);
    }

    protected QuickTileExtensionService() { super(); }

    protected ExtensionTileData getExtensionTileData() {
        return null;
    }

    protected boolean isActivated() {
        return false;
    }

    @Override public IBinder onBind(final Intent intent) { return mBinder; }

    private final ExtensionTile.Stub mBinder = new ExtensionTile.Stub() {
        @Override public boolean isActivated() {
            return QuickTileExtensionService.this.isActivated();
        }

        @Override public boolean isSupported() {
            if (getExtensionTileData() == null) return false;
            return getExtensionTileData().isSupported();
        }

        @Override public String getIconActivated() {
            if (getExtensionTileData() == null) return null;
            return getExtensionTileData().getIconActivated();
        }

        @Override public String getIconDeactivated() {
            if (getExtensionTileData() == null) return null;
            return getExtensionTileData().getIconDeactivated();
        }

        @Override public String getTitleActivated() {
            if (getExtensionTileData() == null) return null;
            return getExtensionTileData().getTitleActivated();
        }

        @Override public String getTitleDeactivated() {
            if (getExtensionTileData() == null) return null;
            return getExtensionTileData().getTitleDeactivated();
        }

        @Override public Intent getIntentActivated() {
            if (getExtensionTileData() == null) return null;
            return getExtensionTileData().getIntentActivated();
        }

        @Override public Intent getIntentDeactivated() {
            if (getExtensionTileData() == null) return null;
            return getExtensionTileData().getIntentDeactivated();
        }
    };

}
