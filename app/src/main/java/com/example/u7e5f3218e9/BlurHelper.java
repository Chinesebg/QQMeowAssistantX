package com.example.u7e5f3218e9;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

/**
 * 弹窗背后的宿主内容模糊（Android 12+ 原生 {@link RenderEffect}，参照 LibChecker）。
 * 供 MainActivity / AboutActivity 等复用，避免重复实现。
 */
public final class BlurHelper {

    private BlurHelper() {
    }

    /** 打开/关闭整屏内容模糊，半径读取 {@link UiConfig#dialogBlur}（0 或关闭时清除效果）。 */
    public static void applyHostBlur(Activity activity, boolean on) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }
        View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        int blur = UiConfig.dialogBlur(activity);
        if (!on || blur <= 0) {
            content.setRenderEffect(null);
        } else {
            float r = blur * activity.getResources().getDisplayMetrics().density + 0.5f;
            content.setRenderEffect(RenderEffect.createBlurEffect(r, r, Shader.TileMode.CLAMP));
        }
    }

    /** 显示弹窗前模糊宿主内容，关闭后自动恢复。 */
    public static void showWithBlur(Activity activity, Dialog dialog) {
        dialog.setOnDismissListener(d -> applyHostBlur(activity, false));
        applyHostBlur(activity, true);
        dialog.show();
    }
}
