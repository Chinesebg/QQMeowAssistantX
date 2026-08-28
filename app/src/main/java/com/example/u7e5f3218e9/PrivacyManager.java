package com.example.u7e5f3218e9;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

/**
 * 使用声明 / 免责声明 的接受状态管理。
 * 以 versionCode 记录「已同意的版本」，首次启动或升级后（versionCode 变大）再次弹出声明。
 */
public final class PrivacyManager {

    private static final String PREFS = "privacy";
    private static final String KEY_ACCEPTED_VERSION_CODE = "accepted_version_code";

    private PrivacyManager() {
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** 是否需要弹出使用声明：从未同意过，或当前版本高于已同意版本。 */
    public static boolean shouldShowDisclaimer(Context ctx) {
        return prefs(ctx).getInt(KEY_ACCEPTED_VERSION_CODE, -1) < currentVersionCode(ctx);
    }

    /** 标记当前版本已同意声明。 */
    public static void markAccepted(Context ctx) {
        prefs(ctx).edit().putInt(KEY_ACCEPTED_VERSION_CODE, currentVersionCode(ctx)).apply();
    }

    private static int currentVersionCode(Context ctx) {
        try {
            PackageInfo info = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}
