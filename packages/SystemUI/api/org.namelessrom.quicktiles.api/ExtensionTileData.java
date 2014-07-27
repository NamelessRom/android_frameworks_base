package org.namelessrom.quicktiles.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * A parcelable, serializable class representing data related to an {@link org.namelessrom.quicktiles.api.QuickTileExtensionService}
 */
public class ExtensionTileData implements Parcelable {

    public static final String ACTION_EXTENSION = "org.namelessrom.quicktiles.api.ACTION_EXTENSION";

    /**
     * The internal interface may change so we need to define a version
     */
    public static final int PARCELABLE_VERSION = 1;

    /**
     * The number of fields of this parcelable
     */
    public static final int PARCELABLE_SIZE = 6;

    /**
     * The function of the tile may not be supported by some devices, allow to handle this case
     */
    private static final String KEY_SUPPORTED = "supported";

    /**
     * The icons for activated / deactivated state
     * TODO: allow to define more icons for our swiping feature
     */
    private static final String KEY_ICON_ACTIVATED   = "icon_activated";
    private static final String KEY_ICON_DEACTIVATED = "icon_deactivated";

    /**
     * The titles for activated / deactivated state
     * TODO: allow to define more titles for our swiping feature
     */
    private static final String KEY_TITLE_ACTIVATED   = "title_activated";
    private static final String KEY_TITLE_DEACTIVATED = "title_deactivated";

    /**
     * The intents for activated / deactivated state
     * TODO: allow to define more intents for our swiping feature
     */
    private static final String KEY_INTENT_ACTIVATED   = "intent_activated";
    private static final String KEY_INTENT_DEACTIVATED = "intent_deactivated";

    private boolean isSupported = false;

    private String mIconActivated   = null;
    private String mIconDeactivated = null;

    private String mTitleActivated   = null;
    private String mTitleDeactivated = null;

    private Intent mIntentActivated   = null;
    private Intent mIntentDeactivated = null;

    public ExtensionTileData() { }

    //==============================================================================================
    // Getter && Setter
    //==============================================================================================

    public boolean isSupported() { return isSupported; }

    public ExtensionTileData setSupported(final boolean isSupported) {
        this.isSupported = isSupported;
        return this;
    }

    public String getIconActivated() { return mIconActivated; }

    public ExtensionTileData setIconActivated(final String mIconActivated) {
        this.mIconActivated = mIconActivated;
        return this;
    }

    public String getIconDeactivated() { return mIconDeactivated; }

    public ExtensionTileData setIconDeactivated(final String mIconDeactivated) {
        this.mIconDeactivated = mIconDeactivated;
        return this;
    }

    public String getTitleActivated() { return mTitleActivated; }

    public ExtensionTileData setTitleActivated(final String mTitleActivated) {
        this.mTitleActivated = mTitleActivated;
        return this;
    }

    public String getTitleDeactivated() { return mTitleDeactivated; }

    public ExtensionTileData setTitleDeactivated(final String mTitleDeactivated) {
        this.mTitleDeactivated = mTitleDeactivated;
        return this;
    }

    public Intent getIntentActivated() { return mIntentActivated; }

    public ExtensionTileData setIntentActivated(final Intent mIntentActivated) {
        this.mIntentActivated = mIntentActivated;
        return this;
    }

    public Intent getIntentDeactivated() { return mIntentDeactivated; }

    public ExtensionTileData setIntentDeactivated(final Intent mIntentDeactivated) {
        this.mIntentDeactivated = mIntentDeactivated;
        return this;
    }

    //==============================================================================================
    // Parcelable
    //==============================================================================================

    /**
     * Serializes the contents of this object to JSON.
     */
    public JSONObject serialize() throws JSONException {
        final JSONObject data = new JSONObject();
        data.put(KEY_SUPPORTED, isSupported);
        data.put(KEY_ICON_ACTIVATED, mIconActivated);
        data.put(KEY_ICON_DEACTIVATED, mIconDeactivated);
        data.put(KEY_TITLE_ACTIVATED, mTitleActivated);
        data.put(KEY_TITLE_DEACTIVATED, mTitleDeactivated);
        data.put(KEY_INTENT_ACTIVATED,
                (mIntentActivated == null) ? null : mIntentActivated.toUri(0));
        data.put(KEY_INTENT_DEACTIVATED,
                (mIntentDeactivated == null) ? null : mIntentDeactivated.toUri(0));
        return data;
    }

    /**
     * Deserializes the given JSON representation of extension data, populating this object.
     */
    public void deserialize(final JSONObject data) throws JSONException {
        this.isSupported = data.optBoolean(KEY_SUPPORTED);
        this.mIconActivated = data.optString(KEY_ICON_ACTIVATED);
        this.mIconDeactivated = data.optString(KEY_ICON_DEACTIVATED);
        this.mTitleActivated = data.optString(KEY_TITLE_ACTIVATED);
        this.mTitleDeactivated = data.optString(KEY_TITLE_DEACTIVATED);
        try {
            this.mIntentActivated = Intent.parseUri(data.optString(KEY_INTENT_ACTIVATED), 0);
        } catch (Exception ignored) {}
        try {
            this.mIntentDeactivated = Intent.parseUri(data.optString(KEY_INTENT_DEACTIVATED), 0);
        } catch (Exception ignored) {}
    }

    /**
     * Serializes the contents of this object to a {@link android.os.Bundle}.
     */
    public Bundle toBundle() {
        final Bundle data = new Bundle();
        data.putBoolean(KEY_SUPPORTED, isSupported);
        data.putString(KEY_ICON_ACTIVATED, mIconActivated);
        data.putString(KEY_ICON_DEACTIVATED, mIconDeactivated);
        data.putString(KEY_TITLE_ACTIVATED, mTitleActivated);
        data.putString(KEY_TITLE_DEACTIVATED, mTitleDeactivated);
        data.putString(KEY_INTENT_ACTIVATED,
                (mIntentActivated == null) ? null : mIntentActivated.toUri(0));
        data.putString(KEY_INTENT_DEACTIVATED,
                (mIntentDeactivated == null) ? null : mIntentDeactivated.toUri(0));
        return data;
    }

    /**
     * Deserializes the given {@link android.os.Bundle} representation of extension data, populating this
     * object.
     */
    public void fromBundle(final Bundle bundle) {
        this.isSupported = bundle.getBoolean(KEY_SUPPORTED);
        this.mIconActivated = bundle.getString(KEY_ICON_ACTIVATED);
        this.mIconDeactivated = bundle.getString(KEY_ICON_DEACTIVATED);
        this.mTitleActivated = bundle.getString(KEY_TITLE_ACTIVATED);
        this.mTitleDeactivated = bundle.getString(KEY_TITLE_DEACTIVATED);
        try {
            this.mIntentActivated = Intent.parseUri(bundle.getString(KEY_INTENT_ACTIVATED), 0);
        } catch (Exception ignored) { }
        try {
            this.mIntentDeactivated = Intent.parseUri(bundle.getString(KEY_INTENT_DEACTIVATED), 0);
        } catch (Exception ignored) { }
    }

    @Override public int describeContents() { return 0; }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override public boolean equals(final Object o) {
        if (o == null) return false;

        try {
            final ExtensionTileData other = (ExtensionTileData) o;
            return other.isSupported == isSupported
                    && other.mIconActivated == mIconActivated
                    && other.mIconDeactivated == mIconDeactivated
                    && TextUtils.equals(other.mTitleActivated, mTitleActivated)
                    && TextUtils.equals(other.mTitleDeactivated, mTitleDeactivated)
                    && intentEquals(other.mIntentActivated, mIntentActivated)
                    && intentEquals(other.mIntentDeactivated, mIntentDeactivated);

        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * Internal method for checking if two intents are equal
     */
    private static boolean intentEquals(final Intent x, final Intent y) {
        if (x == null || y == null) {
            return x == y;
        } else {
            return x.equals(y);
        }
    }

    /**
     * Returns true if the two provided data objects are equal (or both null).
     */
    public static boolean equals(final ExtensionTileData x, final ExtensionTileData y) {
        if (x == null || y == null) {
            return x == y;
        } else {
            return x.equals(y);
        }
    }

    @Override public void writeToParcel(final Parcel parcel, final int flags) {
        /**
         * NOTE: When adding fields in the process of updating this API, make sure to bump
         * {@link #PARCELABLE_VERSION} and modify {@link #PARCELABLE_SIZE}.
         */
        parcel.writeInt(PARCELABLE_VERSION);
        parcel.writeInt(PARCELABLE_SIZE);
        // Version 1 below
        parcel.writeInt(isSupported ? 1 : 0);
        parcel.writeString(mIconActivated);
        parcel.writeString(mIconDeactivated);
        parcel.writeString(TextUtils.isEmpty(mTitleActivated) ? "" : mTitleActivated);
        parcel.writeString(TextUtils.isEmpty(mTitleDeactivated) ? "" : mTitleDeactivated);
        parcel.writeString((mIntentActivated == null) ? "" : mIntentActivated.toUri(0));
        parcel.writeString((mIntentDeactivated == null) ? "" : mIntentDeactivated.toUri(0));
        // Version 2 below
    }

    /**
     * @see android.os.Parcelable
     */
    public static final Creator<ExtensionTileData> CREATOR = new Creator<ExtensionTileData>() {
        public ExtensionTileData createFromParcel(final Parcel in) {
            return new ExtensionTileData(in);
        }

        public ExtensionTileData[] newArray(final int size) {
            return new ExtensionTileData[size];
        }
    };

    private ExtensionTileData(final Parcel in) {
        final int parcelableVersion = in.readInt();
        final int parcelableSize = in.readInt();
        // Version 1 below
        if (parcelableVersion >= 1) {
            this.isSupported = (in.readInt() != 0);
            this.mIconActivated = in.readString();
            this.mIconDeactivated = in.readString();
            this.mTitleActivated = in.readString();
            if (TextUtils.isEmpty(this.mTitleActivated)) this.mTitleActivated = null;
            if (TextUtils.isEmpty(this.mTitleDeactivated)) this.mTitleDeactivated = null;
            try {
                this.mIntentActivated = Intent.parseUri(in.readString(), 0);
            } catch (URISyntaxException ignored) { }
            try {
                this.mIntentDeactivated = Intent.parseUri(in.readString(), 0);
            } catch (URISyntaxException ignored) { }
        }
        // Version 2 below

        // Skip any fields we don't know about. For example, if our current version's
        // PARCELABLE_SIZE is 6 and the input parcelableSize is 12, skip the 6 fields we
        // haven't read yet (from above) since we don't know about them.
        in.setDataPosition(in.dataPosition() + (PARCELABLE_SIZE - parcelableSize));
    }

}
