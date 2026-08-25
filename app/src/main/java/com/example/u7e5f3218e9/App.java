package com.example.u7e5f3218e9;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * 应用入口：在创建 Activity 之前应用深浅色模式（参照 LibChecker 的做法）。
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(ThemeManager.nightMode(this));
    }
}
