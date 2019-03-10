package com.home.retrofitrxjavademo;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "com.delaware.app.Interrupciones";
    SharedPreferences.Editor editor;
    SharedPreferences pref;

    public PreferenceManager(Context context) {
        if (pref == null) {
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }

    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key) {
        return pref.getString(key, null);
    }

    public void deleteKey(String key) {
        editor.remove(key);
        editor.apply();
    }
}
