package com.android.systemui.nameless.quicktiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import com.android.systemui.quicksettings.ExtensionQuickTile;
import com.android.systemui.statusbar.phone.QuickSettingsController;

import org.namelessrom.quicktiles.api.ExtensionTileData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 7/27/14.
 */
public class ExtensionManager {

    private static ExtensionManager extensionManager;

    private Context mContext;

    private ExtensionManager(final Context context) {
        mContext = context;
    }

    public static ExtensionManager getInstance(final Context context) {
        if (extensionManager == null) {
            extensionManager = new ExtensionManager(context);
        }
        return extensionManager;
    }

    public List<ExtensionQuickTile> getExtensionTiles(final QuickSettingsController qsc) {
        final List<ExtensionListing> extensions = getAvailableExtensions();
        final List<ExtensionQuickTile> extensionTiles = new ArrayList<ExtensionQuickTile>(extensions.size());

        for (final ExtensionListing listing : extensions) {
            extensionTiles.add(new ExtensionQuickTile(mContext, qsc, listing));
        }

        return extensionTiles;
    }

    /**
     * Returns a listing of all available (installed) extensions.
     */
    public List<ExtensionListing> getAvailableExtensions() {
        final List<ExtensionListing> availableExtensions = new ArrayList<ExtensionListing>();
        final PackageManager pm = mContext.getPackageManager();
        final List<ResolveInfo> resolveInfos = pm.queryIntentServices(
                new Intent(ExtensionTileData.ACTION_EXTENSION), PackageManager.GET_META_DATA);
        ExtensionListing listing;
        Bundle metaData;
        for (final ResolveInfo resolveInfo : resolveInfos) {
            listing = new ExtensionListing();
            listing.componentName = new ComponentName(resolveInfo.serviceInfo.packageName,
                    resolveInfo.serviceInfo.name);
            listing.title = resolveInfo.loadLabel(pm).toString();
            metaData = resolveInfo.serviceInfo.metaData;
            if (metaData != null) {
                listing.protocolVersion = metaData.getInt("protocolVersion");
                listing.description = metaData.getString("description");
            }

            listing.icon = resolveInfo.loadIcon(pm);
            availableExtensions.add(listing);
        }

        return availableExtensions;
    }

}
