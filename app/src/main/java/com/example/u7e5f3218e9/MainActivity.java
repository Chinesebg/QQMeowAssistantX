package com.example.u7e5f3218e9;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CatConfig config;
    private MaterialSwitch rbPunctuation;
    private MaterialSwitch rbRealtime;
    private MaterialSwitch cbAppend;
    private MaterialSwitch cbEmoticon;
    private TextInputEditText etAppendText;
    private TextInputEditText etCustomEmoticons;
    private TextInputEditText etRules;
    private TextView statusText;
    private MaterialButton toggleButton;
    private View homeView;
    private View settingsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.themeRes(this));
        super.onCreate(savedInstanceState);
        try {
            this.config = CatConfig.load(this);
        } catch (Exception e) {
            this.config = new CatConfig();
        }
        setupEdgeToEdge();
        setContentView(buildRoot());
    }

    // ---------------------------------------------------------------- 布局

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        FrameLayout content = new FrameLayout(this);
        this.homeView = buildHomeView();
        this.settingsView = buildSettingsView();
        content.addView(this.homeView, new FrameLayout.LayoutParams(-1, -1));
        content.addView(this.settingsView, new FrameLayout.LayoutParams(-1, -1));
        this.settingsView.setVisibility(View.GONE);

        BottomNavigationView nav = new BottomNavigationView(this);
        nav.inflateMenu(R.menu.bottom_nav_menu);
        nav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    homeView.setVisibility(View.VISIBLE);
                    settingsView.setVisibility(View.GONE);
                    return true;
                } else if (id == R.id.navigation_settings) {
                    homeView.setVisibility(View.GONE);
                    settingsView.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });
        nav.setSelectedItemId(R.id.navigation_home);

        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        root.addView(nav, new LinearLayout.LayoutParams(-1, -2));

        // 沉浸式：内容避开状态栏；底部手势条由 BottomNavigationView 自动避让
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });
        return root;
    }

    private View buildHomeView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(24), dp(16), dp(24));

        TextView title = new TextView(this);
        title.setText(R.string.home_title);
        TextViewCompat.setTextAppearance(title, com.google.android.material.R.style.TextAppearance_Material3_HeadlineMedium);
        title.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText(R.string.home_subtitle);
        TextViewCompat.setTextAppearance(subtitle, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        subtitle.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams subtitleLp = new LinearLayout.LayoutParams(-1, -2);
        subtitleLp.setMargins(0, dp(4), 0, dp(16));
        root.addView(subtitle, subtitleLp);

        // 服务状态卡片
        MaterialCardView statusCard = new MaterialCardView(this);
        statusCard.setCardElevation(0);
        statusCard.setStrokeWidth(1);
        statusCard.setStrokeColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        statusCard.setRadius(dp(16));

        FrameLayout statusFrame = new FrameLayout(this);

        // Android 12 原生背景模糊（RenderEffect，参照 LibChecker）
        View backdrop = new View(this);
        GradientDrawable backdropBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{colorAttr(com.google.android.material.R.attr.colorPrimaryContainer),
                        colorAttr(com.google.android.material.R.attr.colorSecondaryContainer)});
        backdropBg.setCornerRadius(dp(16));
        backdrop.setBackground(backdropBg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            backdrop.setRenderEffect(RenderEffect.createBlurEffect(dp(20), dp(20), Shader.TileMode.CLAMP));
        }
        statusFrame.addView(backdrop, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(dp(16), dp(16), dp(16), dp(16));

        this.statusText = new TextView(this);
        this.statusText.setGravity(Gravity.CENTER);
        this.statusText.setTextSize(16);
        this.statusText.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        cardContent.addView(this.statusText, new LinearLayout.LayoutParams(-1, -2));

        this.toggleButton = new MaterialButton(this);
        this.toggleButton.setTextColor(ColorStateList.valueOf(
                colorAttr(com.google.android.material.R.attr.colorOnPrimary)));
        this.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openAccessibilitySettings();
            }
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(-1, -2);
        btnLp.setMargins(0, dp(12), 0, 0);
        cardContent.addView(this.toggleButton, btnLp);

        statusFrame.addView(cardContent, new FrameLayout.LayoutParams(-1, -2));
        statusCard.addView(statusFrame, new LinearLayout.LayoutParams(-1, -2));
        root.addView(statusCard, new LinearLayout.LayoutParams(-1, -2));

        // 处理模式
        addSectionTitle(root, R.string.section_mode);
        this.rbPunctuation = new MaterialSwitch(this);
        this.rbPunctuation.setText(R.string.mode_punctuation);
        this.rbPunctuation.setChecked(CatConfig.MODE_PUNCTUATION.equals(this.config.processingMode));
        this.rbPunctuation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rbRealtime.setChecked(false);
            }
            saveSwitchSettings();
        });
        root.addView(this.rbPunctuation, new LinearLayout.LayoutParams(-1, -2));

        this.rbRealtime = new MaterialSwitch(this);
        this.rbRealtime.setText(R.string.mode_realtime);
        this.rbRealtime.setChecked(CatConfig.MODE_REALTIME.equals(this.config.processingMode));
        this.rbRealtime.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rbPunctuation.setChecked(false);
            }
            saveSwitchSettings();
        });
        root.addView(this.rbRealtime, new LinearLayout.LayoutParams(-1, -2));

        addHint(root, R.string.mode_hint);

        // 功能开关
        addSectionTitle(root, R.string.section_switch);
        this.cbAppend = new MaterialSwitch(this);
        this.cbAppend.setText(R.string.switch_append_title);
        this.cbAppend.setChecked(this.config.enableAppend);
        this.cbAppend.setOnCheckedChangeListener((buttonView, isChecked) -> saveSwitchSettings());
        root.addView(this.cbAppend, new LinearLayout.LayoutParams(-1, -2));
        addSwitchDesc(root, R.string.switch_append_desc);

        this.etAppendText = new TextInputEditText(this);
        this.etAppendText.setInputType(InputType.TYPE_CLASS_TEXT);
        this.etAppendText.setText(this.config.appendText != null ? this.config.appendText : "喵");
        this.etAppendText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSwitchSettings();
            }
        });
        root.addView(wrapTextInput(this.etAppendText, getString(R.string.hint_append_text)),
                new LinearLayout.LayoutParams(-1, -2));

        this.cbEmoticon = new MaterialSwitch(this);
        this.cbEmoticon.setText(R.string.switch_emoticon_title);
        this.cbEmoticon.setChecked(this.config.enableRandomEmoticon);
        this.cbEmoticon.setOnCheckedChangeListener((buttonView, isChecked) -> saveSwitchSettings());
        root.addView(this.cbEmoticon, new LinearLayout.LayoutParams(-1, -2));
        addSwitchDesc(root, R.string.switch_emoticon_desc);

        // 文本替换规则
        addSectionTitle(root, R.string.section_rules);
        addHint(root, R.string.rule_hint);
        TextView presetBtn = new TextView(this);
        presetBtn.setText(R.string.preset_rules);
        TextViewCompat.setTextAppearance(presetBtn,
                com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        presetBtn.setTextColor(colorAttr(com.google.android.material.R.attr.colorPrimary));
        presetBtn.setPadding(dp(16), dp(12), dp(16), dp(12));
        presetBtn.setClickable(true);
        presetBtn.setFocusable(true);
        TypedValue presetBg = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, presetBg, true);
        presetBtn.setBackgroundResource(presetBg.resourceId);
        presetBtn.setOnClickListener(v -> applyRulePreset());
        LinearLayout.LayoutParams presetLp = new LinearLayout.LayoutParams(-1, -2);
        presetLp.setMargins(0, 0, 0, dp(8));
        root.addView(presetBtn, presetLp);
        this.etRules = new TextInputEditText(this);
        this.etRules.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        this.etRules.setMinLines(6);
        this.etRules.setGravity(Gravity.TOP | Gravity.START);
        this.etRules.setText(CatConfig.rulesToString(this.config.rules));
        root.addView(wrapTextInput(this.etRules, null), new LinearLayout.LayoutParams(-1, -2));

        // 自定义颜文字
        addSectionTitle(root, R.string.section_emoticons);
        addHint(root, R.string.emoticon_hint);
        this.etCustomEmoticons = new TextInputEditText(this);
        this.etCustomEmoticons.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        this.etCustomEmoticons.setMinLines(4);
        this.etCustomEmoticons.setGravity(Gravity.TOP | Gravity.START);
        this.etCustomEmoticons.setText(joinLines(this.config.customEmoticons));
        root.addView(wrapTextInput(this.etCustomEmoticons, getString(R.string.hint_custom_emoticons)),
                new LinearLayout.LayoutParams(-1, -2));

        // 保存 / 测试
        MaterialButton saveBtn = new MaterialButton(this);
        saveBtn.setText(R.string.btn_save);
        saveBtn.setOnClickListener(v -> saveConfig());
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(-1, -2);
        saveLp.setMargins(0, dp(16), 0, 0);
        root.addView(saveBtn, saveLp);

        MaterialButton testBtn = new MaterialButton(this, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        testBtn.setText(R.string.btn_test);
        testBtn.setOnClickListener(v -> showTestDialog());
        LinearLayout.LayoutParams testLp = new LinearLayout.LayoutParams(-1, -2);
        testLp.setMargins(0, dp(12), 0, 0);
        root.addView(testBtn, testLp);

        addFooterHint(root, R.string.home_footer_hint);

        scroll.addView(root, new LinearLayout.LayoutParams(-1, -2));
        return scroll;
    }

    private View buildSettingsView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(24), dp(16), dp(24));

        MaterialCardView card = new MaterialCardView(this);
        card.setCardElevation(0);
        card.setStrokeWidth(1);
        card.setStrokeColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);

        cardContent.addView(makeSettingsRow(R.drawable.ic_palette, getString(R.string.settings_theme_color), false,
                v -> showThemeDialog()));
        cardContent.addView(listDivider());
        cardContent.addView(makeSettingsRow(R.drawable.ic_dark_mode, getString(R.string.settings_dark_mode), false,
                v -> showDarkModeDialog()));
        cardContent.addView(listDivider());
        cardContent.addView(makeSettingsRow(R.drawable.ic_info, getString(R.string.settings_about), true,
                v -> startActivity(new Intent(MainActivity.this, AboutActivity.class))));
        cardContent.addView(listDivider());
        cardContent.addView(makeSettingsRow(R.drawable.ic_upgrade, getString(R.string.settings_get_updates), true,
                v -> showGetUpdatesSheet()));
        cardContent.addView(listDivider());
        cardContent.addView(makeSettingsRow(R.drawable.ic_language, getString(R.string.settings_language), false,
                v -> Toast.makeText(MainActivity.this, R.string.toast_language_wip, Toast.LENGTH_SHORT).show()));

        card.addView(cardContent, new LinearLayout.LayoutParams(-1, -2));
        root.addView(card, new LinearLayout.LayoutParams(-1, -2));
        scroll.addView(root, new LinearLayout.LayoutParams(-1, -2));
        return scroll;
    }

    // ---------------------------------------------------------------- 小组件

    private void addSectionTitle(LinearLayout parent, int textRes) {
        TextView tv = new TextView(this);
        tv.setText(textRes);
        TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_TitleLarge);
        tv.setTextColor(colorAttr(com.google.android.material.R.attr.colorPrimary));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(20), 0, dp(8));
        parent.addView(tv, lp);
    }

    private void addHint(LinearLayout parent, int textRes) {
        TextView tv = new TextView(this);
        tv.setText(textRes);
        TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
        tv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(12));
        parent.addView(tv, lp);
    }

    private void addSwitchDesc(LinearLayout parent, int textRes) {
        TextView tv = new TextView(this);
        tv.setText(textRes);
        TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
        tv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMarginStart(dp(52));
        lp.setMargins(dp(52), 0, 0, dp(4));
        parent.addView(tv, lp);
    }

    private void addFooterHint(LinearLayout parent, int textRes) {
        TextView tv = new TextView(this);
        tv.setText(textRes);
        TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
        tv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(28), 0, 0);
        parent.addView(tv, lp);
    }

    private TextInputLayout wrapTextInput(TextInputEditText edit, String hint) {
        TextInputLayout til = new TextInputLayout(this);
        if (hint != null) {
            til.setHint(hint);
        }
        til.addView(edit, new LinearLayout.LayoutParams(-1, -2));
        return til;
    }

    private View makeSettingsRow(int iconRes, String title, boolean chevron, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue bg = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, bg, true);
        row.setBackgroundResource(bg.resourceId);
        row.setOnClickListener(onClick);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        row.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));

        TextView titleTv = new TextView(this);
        titleTv.setText(title);
        TextViewCompat.setTextAppearance(titleTv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        titleTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        tLp.setMarginStart(dp(16));
        row.addView(titleTv, tLp);

        if (chevron) {
            TextView chev = new TextView(this);
            chev.setText("›");
            chev.setTextSize(24);
            chev.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
            chev.setGravity(Gravity.CENTER);
            row.addView(chev, new LinearLayout.LayoutParams(-2, -2));
        }
        return row;
    }

    private View listDivider() {
        View v = new View(this);
        v.setBackgroundColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 1);
        lp.setMarginStart(dp(56));
        v.setLayoutParams(lp);
        return v;
    }

    // ---------------------------------------------------------------- 主题与沉浸式

    private void setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        boolean dark = isDarkMode();
        controller.setAppearanceLightStatusBars(!dark);
        controller.setAppearanceLightNavigationBars(!dark);
    }

    private boolean isDarkMode() {
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    private void showThemeDialog() {
        int checked = ThemeManager.colorIndexOf(this);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_theme_color)
                .setSingleChoiceItems(ThemeManager.COLOR_NAMES, checked, (d, which) -> {
                    String color = ThemeManager.COLOR_VALUES[which];
                    ThemeManager.saveColor(this, color);
                    if (ThemeManager.THEME_PINK.equals(color)) {
                        Toast.makeText(this, R.string.toast_pink_theme, Toast.LENGTH_SHORT).show();
                    }
                    d.dismiss();
                    recreate();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    private void showDarkModeDialog() {
        int checked = ThemeManager.nightIndexOf(this);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_dark_mode)
                .setSingleChoiceItems(ThemeManager.NIGHT_NAMES, checked, (d, which) -> {
                    ThemeManager.saveNight(this, ThemeManager.NIGHT_VALUES[which]);
                    AppCompatDelegate.setDefaultNightMode(ThemeManager.nightMode(this));
                    d.dismiss();
                    recreate();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
    }

    // ---------------------------------------------------------------- 获取更新弹窗

    private void showGetUpdatesSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(12), 0, dp(24));

        // 拖拽把手
        View handle = new View(this);
        GradientDrawable handleBg = new GradientDrawable();
        handleBg.setColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        handleBg.setCornerRadius(dp(2));
        handle.setBackground(handleBg);
        LinearLayout.LayoutParams handleLp = new LinearLayout.LayoutParams(dp(32), dp(4));
        handleLp.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(handle, handleLp);

        // GitHub 行
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(24), dp(20), dp(24), dp(20));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue bg = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, bg, true);
        row.setBackgroundResource(bg.resourceId);
        row.setOnClickListener(v -> {
            dialog.dismiss();
            openUrl("https://github.com/Chinesebg/QQMeowAssistantX");
        });

        ImageView ghIcon = new ImageView(this);
        ghIcon.setImageResource(R.drawable.ic_github);
        ghIcon.setColorFilter(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        row.addView(ghIcon, new LinearLayout.LayoutParams(dp(28), dp(28)));

        TextView label = new TextView(this);
        label.setText(R.string.updates_github_label);
        TextViewCompat.setTextAppearance(label, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        label.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        labelLp.setMarginStart(dp(16));
        row.addView(label, labelLp);

        content.addView(row, new LinearLayout.LayoutParams(-1, -2));
        dialog.setContentView(content);
        dialog.show();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_cannot_open_settings, Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------------------------------------------- 原功能逻辑（保持不变）

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private void updateServiceStatus() {
        if (this.statusText == null || this.toggleButton == null) {
            return;
        }
        boolean enabled = isAccessibilityServiceEnabled();
        if (enabled) {
            this.statusText.setText(R.string.service_status_enabled);
            this.statusText.setTextColor(colorAttr(com.google.android.material.R.attr.colorPrimary));
            this.toggleButton.setText(R.string.btn_accessibility_enabled);
            this.toggleButton.setEnabled(false);
            this.toggleButton.setBackgroundTintList(ColorStateList.valueOf(
                    colorAttr(com.google.android.material.R.attr.colorPrimary)));
            return;
        }
        this.statusText.setText(R.string.service_status_disabled);
        this.statusText.setTextColor(colorAttr(com.google.android.material.R.attr.colorError));
        this.toggleButton.setText(R.string.btn_open_accessibility);
        this.toggleButton.setEnabled(true);
        this.toggleButton.setBackgroundTintList(ColorStateList.valueOf(
                colorAttr(com.google.android.material.R.attr.colorPrimary)));
    }

    private boolean isAccessibilityServiceEnabled() {
        try {
            AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
            if (am == null) {
                return false;
            }
            List<AccessibilityServiceInfo> services = am.getEnabledAccessibilityServiceList(-1);
            for (AccessibilityServiceInfo info : services) {
                if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null
                        && getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void openAccessibilitySettings() {
        try {
            Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_cannot_open_settings, Toast.LENGTH_SHORT).show();
        }
    }

    private void applyRulePreset() {
        String current = this.etRules.getText() == null ? "" : this.etRules.getText().toString();
        String[] presets = {"我=本喵", "你=主人"};
        StringBuilder result = new StringBuilder(current.trim());
        boolean changed = false;
        for (String p : presets) {
            if (!containsRule(result.toString(), p)) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(p);
                changed = true;
            }
        }
        if (!changed) {
            Toast.makeText(this, R.string.preset_exists, Toast.LENGTH_SHORT).show();
            return;
        }
        this.etRules.setText(result.toString());
    }

    private boolean containsRule(String text, String presetLine) {
        String word = presetLine.split("[=＝→]", 2)[0].trim();
        if (word.isEmpty()) {
            return false;
        }
        for (String line : text.split("\n")) {
            String left = line.split("[=＝→]", 2)[0].trim();
            if (!left.isEmpty() && left.equals(word)) {
                return true;
            }
        }
        return false;
    }

    private String joinLines(String[] arr) {
        if (arr == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            if (s == null) {
                continue;
            }
            String t = s.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(t);
        }
        return sb.toString();
    }

    /** 从当前界面读取配置（不落盘）。 */
    private CatConfig buildConfigFromUi() {
        CatConfig c = new CatConfig();
        c.enableAppend = this.cbAppend.isChecked();
        String append = this.etAppendText.getText() == null ? "" : this.etAppendText.getText().toString().trim();
        c.appendText = append.isEmpty() ? "喵" : append;
        c.enableRandomEmoticon = this.cbEmoticon.isChecked();
        c.processingMode = this.rbRealtime.isChecked()
                ? CatConfig.MODE_REALTIME : CatConfig.MODE_PUNCTUATION;

        ArrayList<CatConfig.Rule> rules = new ArrayList<>();
        String rulesText = this.etRules.getText() == null ? "" : this.etRules.getText().toString();
        for (String line : rulesText.split("\n")) {
            CatConfig.Rule r = CatConfig.parseRule(line);
            if (r != null) {
                rules.add(r);
            }
        }
        c.rules = rules;

        ArrayList<String> list = new ArrayList<>();
        String customText = this.etCustomEmoticons.getText() == null ? ""
                : this.etCustomEmoticons.getText().toString().trim();
        if (!customText.isEmpty()) {
            for (String raw : customText.split("\n")) {
                String t = raw.trim();
                if (!t.isEmpty()) {
                    list.add(t);
                }
            }
        }
        c.customEmoticons = list.toArray(new String[0]);
        return c;
    }

    /** 开关 / 追加内容即时生效：只保存这些字段，不动替换规则与自定义颜文字。 */
    private void saveSwitchSettings() {
        try {
            this.config.enableAppend = this.cbAppend.isChecked();
            String append = this.etAppendText.getText() == null ? "" : this.etAppendText.getText().toString().trim();
            this.config.appendText = append.isEmpty() ? "喵" : append;
            this.config.enableRandomEmoticon = this.cbEmoticon.isChecked();
            this.config.processingMode = this.rbRealtime.isChecked()
                    ? CatConfig.MODE_REALTIME : CatConfig.MODE_PUNCTUATION;
            this.config.save(this);
        } catch (Exception ignored) {
            // 开关交互失败时静默，避免打断操作
        }
    }

    /** 保存按钮：替换规则 + 自定义颜文字（一并保存完整配置）。 */
    public void saveConfig() {
        try {
            this.config = buildConfigFromUi();
            this.config.save(this);
            Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_save_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showTestDialog() {
        try {
            CatConfig testCfg = buildConfigFromUi();
            String sample = "今天我很好，你准备好了吗？我们去公园玩吧";
            String processed = TextProcessor.process(sample, testCfg);
            String msg = "断句追加：" + yn(testCfg.enableAppend) + "（"
                    + (testCfg.appendText == null ? "" : testCfg.appendText) + "）"
                    + "\n句末颜文字：" + yn(testCfg.enableRandomEmoticon)
                    + "\n替换规则：" + testCfg.rules.size() + " 条"
                    + "\n自定义颜文字：" + (testCfg.customEmoticons.length > 0
                    ? testCfg.customEmoticons.length + "个" : "使用内置")
                    + "\n\n原始：\n" + sample
                    + "\n\n处理后：\n" + processed;
            androidx.appcompat.app.AlertDialog d = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("预览").setMessage(msg)
                    .setPositiveButton("好的", null).create();
            d.show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_test_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String yn(boolean b) {
        return b ? "开" : "关";
    }

    // ---------------------------------------------------------------- 工具

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int colorAttr(int attrRes) {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(attrRes, tv, true);
        return tv.data;
    }
}
