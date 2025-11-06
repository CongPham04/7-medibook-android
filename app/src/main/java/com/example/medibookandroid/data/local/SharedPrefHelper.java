package com.example.medibookandroid.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.lang.reflect.Type;

public class SharedPrefHelper {
    private static final String PREFS_NAME = "MedibookPrefs";
    private final SharedPreferences prefs;
    private final Gson gson;

    public SharedPrefHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public String getString(String key) {
        return prefs.getString(key, null);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public <T> T getObject(String key, Type type) {
        String json = getString(key);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, type);
    }

    public void putObject(String key, Object object) {
        String json = gson.toJson(object);
        putString(key, json);
    }

    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
