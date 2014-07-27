package org.namelessrom.quicktiles.api;

import android.content.Intent;

interface ExtensionTile {
    boolean isActivated();
    boolean isSupported();
    String getIconActivated();
    String getIconDeactivated();
    String getTitleActivated();
    String getTitleDeactivated();
    Intent getIntentActivated();
    Intent getIntentDeactivated();
}
