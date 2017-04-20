/* ******************************************************************************
 * Copyright 2017 Tourmaline Labs, Inc. All rights reserved.
 * Confidential & Proprietary - Tourmaline Labs, Inc. ("TLI")
 *
 * The party receiving this software directly from TLI (the "Recipient")
 * may use this software as reasonably necessary solely for the purposes
 * set forth in the agreement between the Recipient and TLI (the
 * "Agreement"). The software may be used in source code form solely by
 * the Recipient's employees (if any) authorized by the Agreement. Unless
 * expressly authorized in the Agreement, the Recipient may not sublicense,
 * assign, transfer or otherwise provide the source code to any third
 * party. Tourmaline Labs, Inc. retains all ownership rights in and
 * to the software
 *
 * This notice supersedes any other TLI notices contained within the software
 * except copyright notices indicating different years of publication for
 * different portions of the software. This notice does not supersede the
 * application of any third party copyright notice to that third party's
 * code.
 ******************************************************************************/
package com.tourmaline.example.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static Preferences instance;

    private Preferences(final SharedPreferences preferences){ this.preferences = preferences; }

    public static synchronized Preferences getInstance(Context context) {
        if(instance == null){
            final SharedPreferences preferences = context.getSharedPreferences("tourmalinelabs", Context.MODE_PRIVATE);
            instance = new Preferences(preferences);
        }
        return instance;
    }

    private SharedPreferences preferences;

    //Reading
    public boolean containsKey(final String key) {
        return preferences.contains(key);
    }
    public String getString(final String key, final String defaultValue) {
        return preferences.getString(key, defaultValue);
    }
    public int getInt(final String key, final int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }
    public long getLong(final String key, final long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }
    public float getFloat(final String key, final float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }
    public boolean getBoolean(final String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    //Writing
    public void removeKey(final String key){
        preferences.edit().remove(key).apply();
    }
    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }
    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }
    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }
    public void putFloat(String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }
    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

}


