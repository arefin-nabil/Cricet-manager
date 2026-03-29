package com.test.crickethub;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class CricketHubApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Global Theme Persistence:
        // Reads 'dark_mode' from SharedPreferences at start and applies it immediately.
        SharedPreferences prefs = getSharedPreferences("CricketHub_Settings", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", true); // Default to Dark Mode

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
