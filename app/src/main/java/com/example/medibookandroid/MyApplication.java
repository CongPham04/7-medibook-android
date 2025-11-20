package com.example.medibookandroid;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class MyApplication extends Application {
    private static final String PREFS_NAME = "AppSettings";
    private static final String PREF_LANGUAGE_KEY = "AppLanguage";

    @Override
    public void onCreate() {
        super.onCreate();
        loadAndSetLocale();
    }

    private void loadAndSetLocale() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Lấy ngôn ngữ đã lưu, mặc định là 'en' nếu chưa có
        String langCode = prefs.getString(PREF_LANGUAGE_KEY, "en");

        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }
}

