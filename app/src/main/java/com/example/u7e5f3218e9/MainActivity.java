package com.example.u7e5f3218e9;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.Typeface;
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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
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
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BG = 1001;

    private CatConfig config;
    private MaterialSwitch rbPunctuation;
    private MaterialSwitch rbRealtime;
    private MaterialSwitch rbEngineRule;
    private MaterialSwitch rbEngineHybrid;
    private MaterialSwitch rbEngineAi;
    private MaterialSwitch swCustomTail;
    private TextInputEditText etTailText;
    private MaterialSwitch swCustomEmoticon;
    private TextInputEditText etCustomEmoticons;
    private TextInputEditText etRules;
    private TextView statusText;
    private MaterialButton toggleButton;
    private View homeView;
    private View settingsView;
    private View contentView;
    private int bottomInset = 0;
    private BottomNavigationView nav;
    private boolean navHidden = false;
    private boolean fixedBottomBar = true;

    /** 垂直滑动时底栏随滑动隐藏/显示（仅当「固定底栏」关闭时生效）。 */
    private final View.OnScrollChangeListener navScrollListener = new View.OnScrollChangeListener() {
        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (fixedBottomBar) {
                return;
            }
            int dy = scrollY - oldScrollY;
            if (scrollY <= 0) {
                setNavVisible(true);
            } else if (dy > dp(2)) {
                setNavVisible(false);
            } else if (dy < -dp(2)) {
                setNavVisible(true);
            }
        }
    };

    // 态度 / 强度
    private String selectedAttitude;
    private String selectedIntensity;
    private TextView attitudeValue;
    private TextView intensityValue;

    // 背景
    private BackgroundConfig bgConfig;
    private ImageView bgImage;

    // 多语言彩蛋
    private int languageTapCount = 0;
    private long lastLanguageTap = 0;

    private interface SliderListener {
        void onChanged(int value);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.themeRes(this));
        super.onCreate(savedInstanceState);
        if (UiConfig.uiStyle(this).equals(UiConfig.UI_MIUI)) {
            startActivity(new Intent(this, MiuixComposeActivity.class));
            finish();
            return;
        }
        try {
            this.config = CatConfig.load(this);
        } catch (Exception e) {
            this.config = new CatConfig();
        }
        this.selectedAttitude = this.config.attitude != null ? this.config.attitude : CatConfig.DEFAULT_ATTITUDE;
        this.selectedIntensity = this.config.intensity != null ? this.config.intensity : CatConfig.DEFAULT_INTENSITY;
        try {
            this.bgConfig = BackgroundConfig.load(this);
        } catch (Exception e) {
            this.bgConfig = new BackgroundConfig();
        }
        this.fixedBottomBar = UiConfig.fixedBottomBar(this);
        setupEdgeToEdge();
        setContentView(buildRoot());
        applyBackground();
        maybeShowFirstRunDisclaimer();
    }

    // ---------------------------------------------------------------- 布局

    private View buildRoot() {
        FrameLayout root = new FrameLayout(this);

        // 背景图片层
        this.bgImage = new ImageView(this);
        this.bgImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(this.bgImage, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        FrameLayout content = new FrameLayout(this);
        this.contentView = content;
        this.homeView = buildHomeView();
        this.settingsView = buildSettingsView();
        content.addView(this.homeView, new FrameLayout.LayoutParams(-1, -1));
        content.addView(this.settingsView, new FrameLayout.LayoutParams(-1, -1));
        this.settingsView.setVisibility(View.GONE);
        this.homeView.setOnScrollChangeListener(navScrollListener);
        this.settingsView.setOnScrollChangeListener(navScrollListener);

        BottomNavigationView nav = new BottomNavigationView(this);
        this.nav = nav;
        nav.inflateMenu(R.menu.bottom_nav_menu);
        // 让底栏跟随主题色：背景用 colorSurface，选中项用 colorPrimary，指示器用 colorSecondaryContainer
        nav.setBackgroundColor(colorAttr(com.google.android.material.R.attr.colorSurface));
        ColorStateList navTint = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{colorAttr(com.google.android.material.R.attr.colorPrimary),
                        colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant)});
        nav.setItemIconTintList(navTint);
        nav.setItemTextColor(navTint);
        nav.setItemActiveIndicatorColor(ColorStateList.valueOf(
                colorAttr(com.google.android.material.R.attr.colorSecondaryContainer)));
        nav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    homeView.setVisibility(View.VISIBLE);
                    settingsView.setVisibility(View.GONE);
                    setNavVisible(true);
                    return true;
                } else if (id == R.id.navigation_settings) {
                    homeView.setVisibility(View.GONE);
                    settingsView.setVisibility(View.VISIBLE);
                    setNavVisible(true);
                    return true;
                }
                return false;
            }
        });
        nav.setSelectedItemId(R.id.navigation_home);

        main.addView(content, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        main.addView(nav, new LinearLayout.LayoutParams(-1, -2));

        root.addView(main, new FrameLayout.LayoutParams(-1, -1));

        // 沉浸式：内容避开状态栏；底部手势条由 BottomNavigationView 自动避让
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            bottomInset = bars.bottom;
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

        // 处理引擎（规则 / 混合 / AI，参照 nyabox）
        addSectionTitle(root, R.string.section_engine);
        this.rbEngineRule = new MaterialSwitch(this);
        this.rbEngineRule.setText(R.string.engine_rule);
        this.rbEngineRule.setChecked(CatConfig.MODE_ENGINE_RULE.equals(this.config.engineMode));
        this.rbEngineRule.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rbEngineHybrid.setChecked(false);
                rbEngineAi.setChecked(false);
            }
            saveSwitchSettings();
        });
        root.addView(this.rbEngineRule, new LinearLayout.LayoutParams(-1, -2));

        this.rbEngineHybrid = new MaterialSwitch(this);
        this.rbEngineHybrid.setText(R.string.engine_hybrid);
        this.rbEngineHybrid.setChecked(CatConfig.MODE_ENGINE_HYBRID.equals(this.config.engineMode));
        this.rbEngineHybrid.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rbEngineRule.setChecked(false);
                rbEngineAi.setChecked(false);
            }
            saveSwitchSettings();
        });
        root.addView(this.rbEngineHybrid, new LinearLayout.LayoutParams(-1, -2));

        this.rbEngineAi = new MaterialSwitch(this);
        this.rbEngineAi.setText(R.string.engine_ai);
        this.rbEngineAi.setChecked(CatConfig.MODE_ENGINE_AI.equals(this.config.engineMode));
        this.rbEngineAi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                rbEngineRule.setChecked(false);
                rbEngineHybrid.setChecked(false);
            }
            saveSwitchSettings();
        });
        root.addView(this.rbEngineAi, new LinearLayout.LayoutParams(-1, -2));
        addHint(root, R.string.engine_hint);

        // 规则模式自定义句末文字
        addSectionTitle(root, R.string.section_switch);
        this.swCustomTail = new MaterialSwitch(this);
        this.swCustomTail.setText(R.string.switch_append_title);
        this.swCustomTail.setChecked(this.config.tailEnabled);
        this.swCustomTail.setOnCheckedChangeListener((buttonView, isChecked) -> saveSwitchSettings());
        root.addView(this.swCustomTail, new LinearLayout.LayoutParams(-1, -2));

        this.etTailText = new TextInputEditText(this);
        this.etTailText.setInputType(InputType.TYPE_CLASS_TEXT);
        this.etTailText.setSingleLine(true);
        this.etTailText.setText(this.config.tailText != null ? this.config.tailText : CatConfig.DEFAULT_TAIL_TEXT);
        root.addView(wrapTextInput(this.etTailText, getString(R.string.hint_append_text)),
                new LinearLayout.LayoutParams(-1, -2));
        addHint(root, R.string.switch_append_desc);

        // 规则模式自定义颜文字
        this.swCustomEmoticon = new MaterialSwitch(this);
        this.swCustomEmoticon.setText(R.string.switch_emoticon_title);
        this.swCustomEmoticon.setChecked(this.config.emoticonEnabled);
        this.swCustomEmoticon.setOnCheckedChangeListener((buttonView, isChecked) -> saveSwitchSettings());
        root.addView(this.swCustomEmoticon, new LinearLayout.LayoutParams(-1, -2));

        this.etCustomEmoticons = new TextInputEditText(this);
        this.etCustomEmoticons.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        this.etCustomEmoticons.setMinLines(5);
        this.etCustomEmoticons.setGravity(Gravity.TOP | Gravity.START);
        String customEmoticons = this.config.customEmoticons;
        if (customEmoticons == null) {
            customEmoticons = "";
        }
        this.etCustomEmoticons.setText(customEmoticons);
        root.addView(wrapTextInput(this.etCustomEmoticons, getString(R.string.hint_custom_emoticons)),
                new LinearLayout.LayoutParams(-1, -2));
        addHint(root, R.string.switch_emoticon_desc);

        // 语气（态度 × 强度，参照 nyabox）
        addSectionTitle(root, R.string.section_tone);
        this.attitudeValue = addValueRow(root, getString(R.string.label_attitude),
                attitudeName(this.selectedAttitude), v -> showAttitudeDialog());
        this.intensityValue = addValueRow(root, getString(R.string.label_intensity),
                intensityName(this.selectedIntensity), v -> showIntensityDialog());
        addHint(root, R.string.tone_hint);

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
        applyCustomRuleModeLock();

        scroll.addView(root, new LinearLayout.LayoutParams(-1, -2));
        return scroll;
    }

    private View buildSettingsView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(24), dp(16), dp(24));

        // 个性化：主题色 / 深色 / 背景 / 弹窗模糊 / 固定底栏
        List<View> personalization = new ArrayList<>();
        personalization.add(makeSettingsRow(R.drawable.ic_palette, getString(R.string.settings_theme_color), false,
                v -> showThemeDialog()));
        personalization.add(makeSettingsRow(R.drawable.ic_dark_mode, getString(R.string.settings_dark_mode), false,
                v -> showDarkModeDialog()));
        personalization.add(makeSettingsRow(R.drawable.ic_home, getString(R.string.settings_ui_style), true,
                v -> showUiStyleDialog()));
        personalization.add(makeSettingsRow(R.drawable.ic_image, getString(R.string.settings_bg_image), false,
                v -> openImagePicker()));
        personalization.add(makeSettingsRow(R.drawable.ic_clear, getString(R.string.settings_bg_clear), false,
                v -> clearBackground()));
        personalization.add(makeSliderRow(R.string.settings_bg_blur, this.bgConfig.blur, 0, BackgroundConfig.MAX_BLUR,
                v -> {
                    bgConfig.blur = v;
                    bgConfig.save(MainActivity.this);
                    applyBlur();
                }));
        personalization.add(makeSliderRow(R.string.settings_bg_dim, this.bgConfig.brightness,
                BackgroundConfig.MIN_BRIGHTNESS, BackgroundConfig.MAX_BRIGHTNESS,
                v -> {
                    bgConfig.brightness = v;
                    bgConfig.save(MainActivity.this);
                    applyBrightness();
                }));
        personalization.add(makeSliderRow(R.string.settings_dialog_blur, UiConfig.dialogBlur(this),
                0, UiConfig.MAX_DIALOG_BLUR,
                v -> UiConfig.setDialogBlur(MainActivity.this, v)));
        personalization.add(makeSettingsSwitchRow(R.drawable.ic_pin,
                getString(R.string.settings_fixed_bottom_bar),
                getString(R.string.settings_fixed_bottom_bar_desc),
                UiConfig.fixedBottomBar(this),
                (buttonView, isChecked) -> {
                    fixedBottomBar = isChecked;
                    UiConfig.setFixedBottomBar(MainActivity.this, isChecked);
                    if (isChecked) {
                        setNavVisible(true);
                    }
                }));
        addSettingsGroup(root, R.string.settings_section_personalization, personalization);

        // 通知
        List<View> notification = new ArrayList<>();
        notification.add(makeSettingsSwitchRow(R.drawable.ic_notifications,
                getString(R.string.settings_ai_toast),
                getString(R.string.settings_ai_toast_desc),
                UiConfig.aiToastEnabled(this),
                (buttonView, isChecked) -> UiConfig.setAiToastEnabled(MainActivity.this, isChecked)));
        addSettingsGroup(root, R.string.settings_section_notification, notification);

        // AI
        List<View> ai = new ArrayList<>();
        ai.add(makeSettingsRow(R.drawable.ic_ai, getString(R.string.settings_ai), false,
                v -> showAiSettingsDialog()));
        addSettingsGroup(root, R.string.settings_section_ai, ai);

        // 其他：免责声明 / 隐私权限
        List<View> other = new ArrayList<>();
        other.add(makeSettingsRow(R.drawable.ic_info, getString(R.string.settings_disclaimer), true,
                v -> showDisclaimerDialog()));
        other.add(makeSettingsRow(R.drawable.ic_person, getString(R.string.settings_privacy), true,
                v -> startActivity(new Intent(MainActivity.this, PrivacyActivity.class))));
        addSettingsGroup(root, R.string.settings_section_other, other);

        // 关于
        List<View> about = new ArrayList<>();
        about.add(makeSettingsRow(R.drawable.ic_info, getString(R.string.settings_about), true,
                v -> startActivity(new Intent(MainActivity.this, AboutActivity.class))));
        about.add(makeSettingsRow(R.drawable.ic_upgrade, getString(R.string.settings_get_updates), true,
                v -> showGetUpdatesSheet()));
        about.add(makeSettingsRow(R.drawable.ic_language, getString(R.string.settings_language), false,
                v -> onLanguageTap()));
        addSettingsGroup(root, R.string.settings_section_about, about);

        scroll.addView(root, new LinearLayout.LayoutParams(-1, -2));
        return scroll;
    }

    /** 底栏显示/隐藏（随滑动）。参照 LibChecker 的 HideBottomViewOnScrollBehavior：滑出动画结束后置 GONE 以回收占位。 */
    private void setNavVisible(boolean visible) {
        if (this.nav == null) {
            return;
        }
        if (visible == !this.navHidden) {
            return;
        }
        if (!visible && this.nav.getHeight() <= 0) {
            return;
        }
        this.navHidden = !visible;
        if (visible) {
            setContentBottomInset(false);
            this.nav.setVisibility(View.VISIBLE);
            this.nav.animate()
                    .translationY(0f)
                    .setDuration(200L)
                    .start();
        } else {
            final int h = this.nav.getHeight();
            this.nav.animate()
                    .translationY(h)
                    .setDuration(200L)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (navHidden) {
                                nav.setVisibility(View.GONE);
                                setContentBottomInset(true);
                            }
                        }
                    })
                    .start();
        }
    }

    /** 底栏隐藏时给内容区补上底部手势条 inset，避免内容被手势条遮挡。 */
    private void setContentBottomInset(boolean add) {
        if (this.contentView == null) {
            return;
        }
        this.contentView.setPadding(this.contentView.getPaddingLeft(), this.contentView.getPaddingTop(),
                this.contentView.getPaddingRight(), add ? this.bottomInset : 0);
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

    private TextView addValueRow(LinearLayout parent, String title, String value, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue bg = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, bg, true);
        row.setBackgroundResource(bg.resourceId);
        row.setOnClickListener(onClick);

        TextView titleTv = new TextView(this);
        titleTv.setText(title);
        TextViewCompat.setTextAppearance(titleTv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        titleTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        row.addView(titleTv, new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView valueTv = new TextView(this);
        valueTv.setText(value);
        TextViewCompat.setTextAppearance(valueTv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        valueTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorPrimary));
        row.addView(valueTv, new LinearLayout.LayoutParams(-2, -2));

        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
        return valueTv;
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
        icon.setColorFilter(colorAttr(com.google.android.material.R.attr.colorPrimary));
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

    private View makeSliderRow(int titleRes, int value, int min, int max, final SliderListener listener) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(12), dp(16), dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(titleRes);
        TextViewCompat.setTextAppearance(title, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        title.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));

        final TextView valueTv = new TextView(this);
        valueTv.setText(String.valueOf(value));
        TextViewCompat.setTextAppearance(valueTv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        valueTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorPrimary));
        header.addView(valueTv, new LinearLayout.LayoutParams(-2, -2));

        box.addView(header, new LinearLayout.LayoutParams(-1, -2));

        SeekBar seek = new SeekBar(this);
        seek.setMax(max - min);
        seek.setProgress(value - min);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                int actual = progress + min;
                valueTv.setText(String.valueOf(actual));
                if (fromUser) {
                    listener.onChanged(actual);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
            }
        });
        box.addView(seek, new LinearLayout.LayoutParams(-1, -2));
        return box;
    }

    /** 设置页分区标题（rikkahub CardGroup 风格：主色、加粗 titleSmall）。 */
    private void addSettingsSectionTitle(LinearLayout parent, int textRes) {
        TextView tv = new TextView(this);
        tv.setText(textRes);
        TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(colorAttr(com.google.android.material.R.attr.colorPrimary));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(dp(4), dp(20), dp(4), dp(8));
        parent.addView(tv, lp);
    }

    /** 新增一组设置项（rikkahub CardGroup 风格：每项独立圆角卡片，首尾大圆角、中间小圆角）。 */
    private void addSettingsGroup(LinearLayout root, int titleRes, List<View> items) {
        addSettingsSectionTitle(root, titleRes);
        int n = items.size();
        float outer = dp(20);
        float inner = dp(4);
        for (int i = 0; i < n; i++) {
            View row = items.get(i);
            boolean first = i == 0;
            boolean last = i == n - 1;
            float top = (first || n == 1) ? outer : inner;
            float bottom = (last || n == 1) ? outer : inner;
            ShapeAppearanceModel sam = new ShapeAppearanceModel.Builder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, top)
                    .setTopRightCorner(CornerFamily.ROUNDED, top)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, bottom)
                    .setBottomRightCorner(CornerFamily.ROUNDED, bottom)
                    .build();

            MaterialCardView card = new MaterialCardView(this);
            card.setCardElevation(0);
            card.setStrokeWidth(0);
            card.setShapeAppearanceModel(sam);
            // 半透明主题色容器：既随主题色变化，也不再完全遮挡自定义背景图片。
            card.setCardBackgroundColor(withAlpha(
                    colorAttr(com.google.android.material.R.attr.colorSurfaceVariant), 0xCC));
            card.addView(row, new LinearLayout.LayoutParams(-1, -2));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            if (i < n - 1) {
                lp.setMargins(0, 0, 0, dp(2));
            }
            root.addView(card, lp);
        }
    }

    /** 带右侧开关的设置行（整行可点击切换）。 */
    private View makeSettingsSwitchRow(int iconRes, String title, String subtitle, boolean checked,
                                       CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(6), dp(6), dp(6));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(colorAttr(com.google.android.material.R.attr.colorPrimary));
        row.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        TextView titleTv = new TextView(this);
        titleTv.setText(title);
        TextViewCompat.setTextAppearance(titleTv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        titleTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        textBox.addView(titleTv, new LinearLayout.LayoutParams(-2, -2));
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subTv = new TextView(this);
            subTv.setText(subtitle);
            TextViewCompat.setTextAppearance(subTv, com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
            subTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
            textBox.addView(subTv, new LinearLayout.LayoutParams(-2, -2));
        }
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        textLp.setMarginStart(dp(16));
        row.addView(textBox, textLp);

        final MaterialSwitch sw = new MaterialSwitch(this);
        sw.setChecked(checked);
        sw.setOnCheckedChangeListener(listener);
        row.addView(sw, new LinearLayout.LayoutParams(-2, -2));

        row.setClickable(true);
        row.setFocusable(true);
        TypedValue bg = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, bg, true);
        row.setBackgroundResource(bg.resourceId);
        row.setOnClickListener(v -> sw.toggle());
        return row;
    }

    // ---------------------------------------------------------------- 背景

    private void applyBackground() {
        if (this.bgImage == null) {
            return;
        }
        if (this.bgConfig.uri != null && !this.bgConfig.uri.isEmpty()) {
            Bitmap bmp = decodeSampledBitmap(Uri.parse(this.bgConfig.uri));
            this.bgImage.setImageBitmap(bmp);
            this.bgImage.setVisibility(View.VISIBLE);
        } else {
            this.bgImage.setImageBitmap(null);
            this.bgImage.setVisibility(View.GONE);
        }
        applyBlur();
        applyBrightness();
    }

    private void applyBlur() {
        if (this.bgImage == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && this.bgConfig.blur > 0) {
            this.bgImage.setRenderEffect(RenderEffect.createBlurEffect(
                    dp(this.bgConfig.blur), dp(this.bgConfig.blur), Shader.TileMode.CLAMP));
        } else {
            this.bgImage.setRenderEffect(null);
        }
    }

    private void applyBrightness() {
        if (this.bgImage == null) {
            return;
        }
        int b = this.bgConfig.brightness;
        if (b == 0) {
            this.bgImage.clearColorFilter();
            return;
        }
        float offset = b * 255f / BackgroundConfig.MAX_BRIGHTNESS;
        float[] mat = new float[]{
                1, 0, 0, 0, offset,
                0, 1, 0, 0, offset,
                0, 0, 1, 0, offset,
                0, 0, 0, 1, 0
        };
        this.bgImage.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(mat)));
    }

    private Bitmap decodeSampledBitmap(Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) {
                return null;
            }
            BitmapFactory.decodeStream(is, null, bounds);
            is.close();
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }

            int reqW = Math.max(1, getResources().getDisplayMetrics().widthPixels);
            int reqH = Math.max(1, getResources().getDisplayMetrics().heightPixels);
            int sample = 1;
            while (bounds.outWidth / (sample * 2) >= reqW && bounds.outHeight / (sample * 2) >= reqH) {
                sample *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            InputStream is2 = getContentResolver().openInputStream(uri);
            if (is2 == null) {
                return null;
            }
            Bitmap bmp = BitmapFactory.decodeStream(is2, null, opts);
            is2.close();
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_BG);
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_cannot_open_settings, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearBackground() {
        this.bgConfig.uri = "";
        this.bgConfig.save(this);
        applyBackground();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BG && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
            }
            this.bgConfig.uri = uri.toString();
            this.bgConfig.save(this);
            applyBackground();
            Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        }
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

    /** Android 12+ 原生背景模糊：模糊宿主内容（弹窗背后的整屏内容），逻辑见 {@link BlurHelper}。 */
    private void applyHostBlur(boolean on) {
        BlurHelper.applyHostBlur(this, on);
    }

    private void showWithBlur(android.app.Dialog dialog) {
        BlurHelper.showWithBlur(this, dialog);
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
        showWithBlur(dialog);
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
        showWithBlur(dialog);
    }

    private void showUiStyleDialog() {
        int checked = UiConfig.uiStyleIndex(this);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_ui_style)
                .setSingleChoiceItems(UiConfig.UI_STYLE_NAMES, checked, (d, which) -> {
                    String style = UiConfig.UI_STYLE_VALUES[which];
                    UiConfig.setUiStyle(this, style);
                    d.dismiss();
                    if (UiConfig.UI_MIUI.equals(style)) {
                        startActivity(new Intent(MainActivity.this, MiuixComposeActivity.class));
                        finish();
                    } else {
                        recreate();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        showWithBlur(dialog);
    }

    private void showAttitudeDialog() {
        int checked = RuleEngine.attitudeIndexOf(this.selectedAttitude);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.label_attitude)
                .setSingleChoiceItems(RuleEngine.ATTITUDE_NAMES, checked, (d, which) -> {
                    this.selectedAttitude = RuleEngine.ATTITUDES[which];
                    this.attitudeValue.setText(RuleEngine.ATTITUDE_NAMES[which]);
                    saveSwitchSettings();
                    d.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        showWithBlur(dialog);
    }

    private void showIntensityDialog() {
        int checked = RuleEngine.intensityIndexOf(this.selectedIntensity);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.label_intensity)
                .setSingleChoiceItems(RuleEngine.INTENSITY_NAMES, checked, (d, which) -> {
                    this.selectedIntensity = RuleEngine.INTENSITIES[which];
                    this.intensityValue.setText(RuleEngine.INTENSITY_NAMES[which]);
                    saveSwitchSettings();
                    d.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        showWithBlur(dialog);
    }

    private void showAiSettingsDialog() {
        AiConfig ai = AiConfig.load(this);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(16), dp(24), 0);

        TextInputLayout tilBase = new TextInputLayout(this);
        tilBase.setHint(getString(R.string.ai_base_url));
        TextInputEditText etBase = new TextInputEditText(this);
        etBase.setText(ai.baseUrl);
        etBase.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        tilBase.addView(etBase, new LinearLayout.LayoutParams(-1, -2));
        container.addView(tilBase, new LinearLayout.LayoutParams(-1, -2));

        TextInputLayout tilKey = new TextInputLayout(this);
        tilKey.setHint(getString(R.string.ai_api_key));
        TextInputEditText etKey = new TextInputEditText(this);
        etKey.setText(ai.apiKey);
        etKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilKey.addView(etKey, new LinearLayout.LayoutParams(-1, -2));
        container.addView(tilKey, new LinearLayout.LayoutParams(-1, -2));

        TextInputLayout tilModel = new TextInputLayout(this);
        tilModel.setHint(getString(R.string.ai_model));
        TextInputEditText etModel = new TextInputEditText(this);
        etModel.setText(ai.model);
        etModel.setInputType(InputType.TYPE_CLASS_TEXT);
        tilModel.addView(etModel, new LinearLayout.LayoutParams(-1, -2));
        container.addView(tilModel, new LinearLayout.LayoutParams(-1, -2));

        TextInputLayout tilTemp = new TextInputLayout(this);
        tilTemp.setHint(getString(R.string.ai_temperature));
        TextInputEditText etTemp = new TextInputEditText(this);
        etTemp.setText(String.valueOf(ai.temperature));
        etTemp.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tilTemp.addView(etTemp, new LinearLayout.LayoutParams(-1, -2));
        container.addView(tilTemp, new LinearLayout.LayoutParams(-1, -2));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_ai)
                .setView(container)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    AiConfig c = new AiConfig();
                    c.baseUrl = etBase.getText().toString().trim();
                    c.apiKey = etKey.getText().toString().trim();
                    c.model = etModel.getText().toString().trim();
                    String t = etTemp.getText().toString().trim();
                    try {
                        c.temperature = Float.parseFloat(t);
                    } catch (Exception e) {
                        c.temperature = 0.8f;
                    }
                    if (c.baseUrl.isEmpty()) c.baseUrl = AiConfig.DEFAULT_BASE_URL;
                    if (c.model.isEmpty()) c.model = AiConfig.DEFAULT_MODEL;
                    c.save(this);
                    Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        showWithBlur(dialog);
    }

    // ---------------------------------------------------------------- 使用声明 / 免责声明

    /** 首次启动（或升级后）弹出使用声明与免责声明，用户同意后记录版本。 */
    private void maybeShowFirstRunDisclaimer() {
        if (!PrivacyManager.shouldShowDisclaimer(this)) {
            return;
        }
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.disclaimer_title)
                .setMessage(R.string.disclaimer_message)
                .setPositiveButton(R.string.disclaimer_agree, (d, which) ->
                        PrivacyManager.markAccepted(this))
                .setNegativeButton(R.string.disclaimer_disagree, (d, which) -> finish())
                .setCancelable(false)
                .create();
        showWithBlur(dialog);
    }

    /** 「设置 → 其他 → 免责声明」入口：仅展示声明内容。 */
    private void showDisclaimerDialog() {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.disclaimer_title)
                .setMessage(R.string.disclaimer_message)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        showWithBlur(dialog);
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
        showWithBlur(dialog);
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_cannot_open_settings, Toast.LENGTH_SHORT).show();
        }
    }

    private void onLanguageTap() {
        long now = System.currentTimeMillis();
        if (now - this.lastLanguageTap > 1500) {
            this.languageTapCount = 0;
        }
        this.lastLanguageTap = now;
        this.languageTapCount++;
        if (this.languageTapCount >= 7) {
            this.languageTapCount = 0;
            this.lastLanguageTap = 0;
            startActivity(new Intent(MainActivity.this, CatGirlActivity.class));
            return;
        }
        Toast.makeText(this, R.string.toast_language_wip, Toast.LENGTH_SHORT).show();
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

    private String attitudeName(String attitude) {
        return RuleEngine.ATTITUDE_NAMES[RuleEngine.attitudeIndexOf(attitude)];
    }

    private String intensityName(String intensity) {
        return RuleEngine.INTENSITY_NAMES[RuleEngine.intensityIndexOf(intensity)];
    }

    /** 从当前界面读取配置（不落盘）。 */
    private CatConfig buildConfigFromUi() {
        CatConfig c = new CatConfig();
        c.processingMode = this.rbRealtime.isChecked()
                ? CatConfig.MODE_REALTIME : CatConfig.MODE_PUNCTUATION;
        c.engineMode = this.rbEngineHybrid.isChecked() ? CatConfig.MODE_ENGINE_HYBRID
                : this.rbEngineAi.isChecked() ? CatConfig.MODE_ENGINE_AI
                : CatConfig.MODE_ENGINE_RULE;
        c.attitude = this.selectedAttitude;
        c.intensity = this.selectedIntensity;
        c.tailEnabled = this.swCustomTail != null && this.swCustomTail.isChecked();
        String tail = this.etTailText == null || this.etTailText.getText() == null
                ? CatConfig.DEFAULT_TAIL_TEXT : this.etTailText.getText().toString().trim();
        c.tailText = tail.isEmpty() ? CatConfig.DEFAULT_TAIL_TEXT : tail;
        c.emoticonEnabled = this.swCustomEmoticon != null && this.swCustomEmoticon.isChecked();
        c.customEmoticons = this.etCustomEmoticons == null || this.etCustomEmoticons.getText() == null
                ? "" : this.etCustomEmoticons.getText().toString();
        ArrayList<CatConfig.Rule> rules = new ArrayList<>();
        String rulesText = this.etRules.getText() == null ? "" : this.etRules.getText().toString();
        for (String line : rulesText.split("\n")) {
            CatConfig.Rule r = CatConfig.parseRule(line);
            if (r != null) {
                rules.add(r);
            }
        }
        c.rules = rules;
        return c;
    }

    /** 开关 / 态度 / 强度即时生效：只保存这些字段，不动替换规则。 */
    private void saveSwitchSettings() {
        try {
            this.config.processingMode = this.rbRealtime.isChecked()
                    ? CatConfig.MODE_REALTIME : CatConfig.MODE_PUNCTUATION;
            this.config.engineMode = this.rbEngineHybrid.isChecked() ? CatConfig.MODE_ENGINE_HYBRID
                    : this.rbEngineAi.isChecked() ? CatConfig.MODE_ENGINE_AI
                    : CatConfig.MODE_ENGINE_RULE;
            this.config.attitude = this.selectedAttitude;
            this.config.intensity = this.selectedIntensity;
            this.config.tailEnabled = this.swCustomTail != null && this.swCustomTail.isChecked();
            if (this.etTailText != null && this.etTailText.getText() != null) {
                String tail = this.etTailText.getText().toString().trim();
                this.config.tailText = tail.isEmpty() ? CatConfig.DEFAULT_TAIL_TEXT : tail;
            }
            this.config.emoticonEnabled = this.swCustomEmoticon != null && this.swCustomEmoticon.isChecked();
            if (this.etCustomEmoticons != null && this.etCustomEmoticons.getText() != null) {
                this.config.customEmoticons = this.etCustomEmoticons.getText().toString();
            }
            applyCustomRuleModeLock();
            this.config.save(this);
        } catch (Exception ignored) {
            // 开关交互失败时静默，避免打断操作
        }
    }

    /** 规则模式自定义开关开启时，强制锁定处理引擎为「规则」。 */
    private void applyCustomRuleModeLock() {
        if (this.swCustomTail == null || this.swCustomEmoticon == null
                || this.rbEngineRule == null || this.rbEngineHybrid == null || this.rbEngineAi == null) {
            return;
        }
        boolean customOn = this.swCustomTail.isChecked() || this.swCustomEmoticon.isChecked();
        this.rbEngineRule.setEnabled(!customOn);
        this.rbEngineHybrid.setEnabled(true);
        this.rbEngineAi.setEnabled(true);
    }

    /** 保存按钮：替换规则（一并保存完整配置）。 */
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
        final CatConfig testCfg = buildConfigFromUi();
        final String sample = "今天我很好，你准备好了吗？我们去公园玩吧";
        String engine = testCfg.engineMode != null ? testCfg.engineMode : CatConfig.MODE_ENGINE_RULE;
        if (CatConfig.MODE_ENGINE_RULE.equals(engine)) {
            showTestResult(testCfg,
                    RuleEngine.convert(sample, testCfg.intensity, testCfg.attitude, testCfg.rules, true,
                            testCfg.tailEnabled, testCfg.tailText,
                            testCfg.emoticonEnabled, testCfg.customEmoticons), sample);
            return;
        }
        // AI / 混合：异步
        if (UiConfig.aiToastEnabled(this)) {
            Toast.makeText(this, R.string.ai_processing, Toast.LENGTH_SHORT).show();
        }
        new Thread(() -> {
            AiConfig aiCfg = AiConfig.load(MainActivity.this);
            String result = Engine.process(sample, testCfg, aiCfg);
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    showTestResult(testCfg, result, sample);
                }
            });
        }).start();
    }

    private void showTestResult(CatConfig testCfg, String processed, String sample) {
        String msg = "处理引擎：" + engineLabel(testCfg)
                + "\n态度：" + attitudeName(testCfg.attitude)
                + "\n强度：" + intensityName(testCfg.intensity)
                + "\n自定义句末：" + (testCfg.tailEnabled ? testCfg.tailText : "关")
                + "\n替换规则：" + testCfg.rules.size() + " 条"
                + "\n\n原始：\n" + sample
                + "\n\n处理后：\n" + processed;
        androidx.appcompat.app.AlertDialog d = new MaterialAlertDialogBuilder(this)
                .setTitle("预览").setMessage(msg)
                .setPositiveButton("好的", null).create();
        showWithBlur(d);
    }

    private String engineLabel(CatConfig cfg) {
        if (CatConfig.MODE_ENGINE_AI.equals(cfg.engineMode)) return "AI";
        if (CatConfig.MODE_ENGINE_HYBRID.equals(cfg.engineMode)) return "混合";
        return "规则";
    }

    // ---------------------------------------------------------------- 工具

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private int colorAttr(int attrRes) {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(attrRes, tv, true);
        return tv.data;
    }
}
