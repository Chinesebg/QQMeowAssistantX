package com.example.u7e5f3218e9;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;
import java.util.Comparator;

public class QQAccessibilityService extends AccessibilityService {
    private static final String ID_INPUT = "com.tencent.mobileqq:id/input";
    private static final String ID_SEND = "com.tencent.mobileqq:id/send_btn";
    private static final String PKG_QQ = "com.tencent.mobileqq";
    private static final String PKG_QQI = "com.tencent.mobileqqi";
    private static final String TAG = "QQCatSvc";
    private String userOriginal = "";
    private String lastSet = "";
    private boolean processing = false;
    private long lastWriteTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        String pkg = e.getPackageName() != null ? e.getPackageName().toString() : "";
        if (PKG_QQ.equals(pkg) || PKG_QQI.equals(pkg)) {
            int type = e.getEventType();
            if (type == 32) {
                this.processing = false;
                this.userOriginal = "";
                this.lastSet = "";
                this.lastWriteTime = 0L;
                return;
            }
            if (type == 1) {
                AccessibilityNodeInfo src = e.getSource();
                if (src != null) {
                    String id = src.getViewIdResourceName();
                    if (ID_SEND.equals(id)) {
                        Log.d(TAG, "点击发送，兜底处理");
                        doProcess(true);
                    }
                    src.recycle();
                    return;
                }
                return;
            }
            if (type == 16) {
                // 每次事件都重新读取配置，保证在主界面切换引擎 / 模式后即时生效。
                CatConfig cfg = CatConfig.load(this);
                String mode = cfg.processingMode != null ? cfg.processingMode : CatConfig.MODE_PUNCTUATION;
                if (CatConfig.MODE_REALTIME.equals(mode)) {
                    doProcess(false);
                    return;
                }
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root == null) {
                    return;
                }
                AccessibilityNodeInfo inp = findNodeById(root, ID_INPUT);
                if (inp == null) {
                    inp = findEditable(root);
                }
                root.recycle();
                if (inp == null) {
                    return;
                }
                CharSequence cs = inp.getText();
                inp.recycle();
                if (cs == null || cs.length() == 0) {
                    return;
                }
                String raw = cs.toString().trim();
                if (!raw.isEmpty() && isPunctuationEnding(raw)) {
                    Log.d(TAG, "标点触发: " + raw);
                    doProcess(false);
                }
            }
        }
    }

    private boolean isPunctuationEnding(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return isTriggerPunctuation(s.charAt(s.length() - 1));
    }

    /** 句末触发标点：全角 / 半角均支持（含逗号）。 */
    private boolean isTriggerPunctuation(char c) {
        switch (c) {
            case '。':  // 全角句号
            case '，':  // 全角逗号
            case '！':  // 全角感叹号
            case '？':  // 全角问号
            case '；':  // 全角分号
            case '：':  // 全角冒号
            case '、':  // 全角顿号
            case '…':  // 省略号
            case '.':   // 半角句号
            case ',':   // 半角逗号
            case '!':   // 半角感叹号
            case '?':   // 半角问号
            case ';':   // 半角分号
            case ':':   // 半角冒号
            case ' ':   // 空格
                return true;
            default:
                return false;
        }
    }

    private void doProcess(boolean isSendClick) {
        if (this.processing) {
            return;
        }
        this.processing = true;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            this.processing = false;
            return;
        }
        AccessibilityNodeInfo inp = findNodeById(root, ID_INPUT);
        if (inp == null) {
            inp = findEditable(root);
        }
        if (inp == null) {
            root.recycle();
            this.processing = false;
            return;
        }
        CharSequence cs = inp.getText();
        if (cs == null || cs.length() == 0) {
            inp.recycle();
            root.recycle();
            this.processing = false;
            this.userOriginal = "";
            this.lastSet = "";
            return;
        }
        String raw = cs.toString().trim();
        if (raw.isEmpty()) {
            inp.recycle();
            root.recycle();
            this.processing = false;
            this.userOriginal = "";
            this.lastSet = "";
            return;
        }
        // 每次处理都重新读取配置，保证引擎 / 模式切换即时生效（不依赖窗口切换事件刷新缓存）。
        CatConfig cfg = CatConfig.load(this);
        long now = System.currentTimeMillis();
        long j = this.lastWriteTime;
        if (j > 0 && now - j < 600 && raw.equals(this.lastSet)) {
            Log.d(TAG, "写入回显跳过");
            this.lastWriteTime = 0L;
            inp.recycle();
            root.recycle();
            this.processing = false;
            return;
        }
        boolean isRealtime = CatConfig.MODE_REALTIME.equals(cfg.processingMode);
        if (!isRealtime && this.lastSet.isEmpty()) {
            this.userOriginal = stripAll(raw);
            Log.d(TAG, "标点首次剥离: " + this.userOriginal);
        } else if (this.lastSet.isEmpty() || !raw.startsWith(this.lastSet)) {
            if (this.lastSet.isEmpty()) {
                this.userOriginal = stripAll(raw);
                Log.d(TAG, "首条剥离: " + this.userOriginal);
            } else {
                this.userOriginal = stripAll(raw);
                Log.d(TAG, "不匹配剥离: " + this.userOriginal);
            }
        } else {
            String added = raw.substring(this.lastSet.length());
            this.userOriginal += added;
            Log.d(TAG, "前缀增量: +" + added + "  userOriginal=" + this.userOriginal);
        }
        if (this.userOriginal.isEmpty()) {
            Log.d(TAG, "原文为空，跳过");
            inp.recycle();
            root.recycle();
            this.processing = false;
            return;
        }
        String engine = cfg.engineMode != null ? cfg.engineMode : CatConfig.MODE_ENGINE_RULE;
        if (CatConfig.MODE_ENGINE_AI.equals(engine) || CatConfig.MODE_ENGINE_HYBRID.equals(engine)) {
            // AI / 混合：后台线程处理，完成后写回
            final String original = this.userOriginal;
            final CatConfig fCfg = cfg;
            inp.recycle();
            root.recycle();
            launchEngineRewrite(original, fCfg);
            return;
        }
        boolean withEmoticon = !isRealtime || isSendClick;
        String target = RuleEngine.convert(this.userOriginal, cfg.intensity, cfg.attitude, cfg.rules, withEmoticon);
        if (!target.equals(raw)) {
            Log.d(TAG, "写入: raw=" + raw + "  userOriginal=" + this.userOriginal + "  target=" + target);
            boolean ok = setText(inp, target);
            if (ok) {
                this.lastSet = target;
                this.lastWriteTime = System.currentTimeMillis();
            }
            inp.recycle();
            root.recycle();
            this.processing = false;
            return;
        }
        this.lastSet = target;
        inp.recycle();
        root.recycle();
        this.processing = false;
    }

    private void launchEngineRewrite(String input, CatConfig cfg) {
        // 处理开始时弹 toast 通知（无障碍服务在后台，普通 Toast 常被系统/厂商 ROM 拦截，改用覆盖层）。
        if (UiConfig.aiToastEnabled(this)) {
            showOverlayToast(R.string.ai_processing);
        }
        new Thread(() -> {
            AiConfig aiCfg = AiConfig.load(QQAccessibilityService.this);
            String result = Engine.process(input, cfg, aiCfg);
            postToMain(() -> {
                try {
                    writeProcessedText(result);
                } finally {
                    processing = false;
                }
            });
        }).start();
    }

    /** 用 TYPE_ACCESSIBILITY_OVERLAY 画一个自定义 toast，避免后台服务里普通 Toast 不显示。 */
    private void showOverlayToast(final int textRes) {
        postToMain(() -> {
            try {
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                float density = getResources().getDisplayMetrics().density;
                TextView tv = new TextView(this);
                tv.setText(textRes);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(14);
                tv.setGravity(Gravity.CENTER);
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(0xE6000000);
                bg.setCornerRadius(24 * density);
                tv.setBackground(bg);
                tv.setPadding((int) (20 * density), (int) (12 * density),
                        (int) (20 * density), (int) (12 * density));

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT);
                lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                lp.y = (int) (80 * density);

                wm.addView(tv, lp);
                tv.postDelayed(() -> {
                    try {
                        wm.removeView(tv);
                    } catch (Exception ignored) {
                    }
                }, 2000);
                Log.d(TAG, "AI 处理开始，弹出覆盖层 toast");
            } catch (Exception e) {
                // 覆盖层失败时回退普通 Toast
                Toast.makeText(this, textRes, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "覆盖层失败，回退普通 Toast: " + e.getMessage());
            }
        });
    }

    private void writeProcessedText(String target) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }
        AccessibilityNodeInfo inp = findNodeById(root, ID_INPUT);
        if (inp == null) {
            inp = findEditable(root);
        }
        root.recycle();
        if (inp == null) {
            return;
        }
        CharSequence cs = inp.getText();
        String raw = cs != null ? cs.toString().trim() : "";
        if (!raw.equals(target)) {
            boolean ok = setText(inp, target);
            if (ok) {
                this.lastSet = target;
                this.lastWriteTime = System.currentTimeMillis();
            }
        }
        inp.recycle();
    }

    private void postToMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private String stripAll(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String result = text;
        String[] emotes = RuleEngine.ALL_EMOTICONS;
        Arrays.sort(emotes, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return b.length() - a.length();
            }
        });
        for (String em : emotes) {
            if (em == null || em.isEmpty()) {
                continue;
            }
            int idx;
            while ((idx = result.indexOf(em)) >= 0) {
                int st;
                if (idx <= 0 || result.charAt(idx - 1) != ' ') {
                    st = idx;
                } else {
                    st = idx - 1;
                }
                result = result.substring(0, st) + result.substring(idx + em.length());
            }
        }
        return result.replaceAll("\\s*[\\p{S}\\p{So}\\p{Sm}\\p{Sk}\\p{P}]{3,}\\s*", " ").trim();
    }

    private AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo n, String id) {
        if (n == null || id == null) {
            return null;
        }
        if (id.equals(n.getViewIdResourceName())) {
            return AccessibilityNodeInfo.obtain(n);
        }
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo c = n.getChild(i);
            if (c != null) {
                AccessibilityNodeInfo r = findNodeById(c, id);
                c.recycle();
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private AccessibilityNodeInfo findEditable(AccessibilityNodeInfo n) {
        if (n == null) {
            return null;
        }
        if (n.isEditable()) {
            return AccessibilityNodeInfo.obtain(n);
        }
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo c = n.getChild(i);
            if (c != null) {
                AccessibilityNodeInfo r = findEditable(c);
                c.recycle();
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private boolean setText(AccessibilityNodeInfo n, String t) {
        if (n == null) {
            return false;
        }
        try {
            Bundle b = new Bundle();
            b.putCharSequence("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", t);
            boolean ok = n.performAction(2097152, b);
            if (ok) {
                Bundle a = new Bundle();
                a.putInt("ACTION_ARGUMENT_SELECTION_START_INT", t.length());
                a.putInt("ACTION_ARGUMENT_SELECTION_END_INT", t.length());
                n.performAction(131072, a);
            }
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onInterrupt() {
        this.processing = false;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo i = new AccessibilityServiceInfo();
        i.eventTypes = 49;
        i.feedbackType = 16;
        i.flags = 81;
        i.notificationTimeout = 50L;
        i.packageNames = new String[]{PKG_QQ, PKG_QQI};
        setServiceInfo(i);
    }
}