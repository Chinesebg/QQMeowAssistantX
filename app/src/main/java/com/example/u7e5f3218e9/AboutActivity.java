package com.example.u7e5f3218e9;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.themeRes(this));
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private ViewGroup buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle(R.string.settings_about);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24), dp(16), dp(24), dp(32));

        // App 图标
        ImageView appIcon = new ImageView(this);
        Drawable icon = loadAppIcon();
        if (icon != null) {
            appIcon.setImageDrawable(icon);
        }
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(96), dp(96));
        iconLp.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(appIcon, iconLp);

        // App 名 + 版本
        TextView name = new TextView(this);
        name.setText(getString(R.string.app_name));
        TextViewCompat.setTextAppearance(name, com.google.android.material.R.style.TextAppearance_Material3_HeadlineSmall);
        name.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        name.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(-1, -2);
        nameLp.setMargins(0, dp(16), 0, 0);
        content.addView(name, nameLp);

        TextView version = new TextView(this);
        version.setText("v" + versionName());
        TextViewCompat.setTextAppearance(version, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        version.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        version.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(version, new LinearLayout.LayoutParams(-1, -2));

        // DEVS / CONTRIBS 可点击按钮
        addGroupButton(content, getString(R.string.about_devs), v -> showDevelopersSheet());
        addGroupButton(content, getString(R.string.about_contribs), v -> showContributorsSheet());

        scroll.addView(content, new LinearLayout.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        return root;
    }

    private void addGroupButton(LinearLayout parent, String title, View.OnClickListener onClick) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardElevation(0);
        card.setStrokeWidth(1);
        card.setStrokeColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        card.setRadius(dp(16));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(onClick);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView titleTv = new TextView(this);
        titleTv.setText(title);
        TextViewCompat.setTextAppearance(titleTv, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        titleTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        row.addView(titleTv, new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView chev = new TextView(this);
        chev.setText("›");
        chev.setTextSize(24);
        chev.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        chev.setGravity(Gravity.CENTER);
        row.addView(chev, new LinearLayout.LayoutParams(-2, -2));

        card.addView(row, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
        cardLp.setMargins(0, dp(16), 0, dp(8));
        parent.addView(card, cardLp);
    }

    private void showDevelopersSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(0, dp(12), 0, dp(24));

        content.addView(makeDragHandle());
        content.addView(sheetTitle(R.string.about_devs));

        content.addView(makeAuthorRow(R.drawable.avatar2, "QiCaiJie114514", v -> {
            dialog.dismiss();
            openUrl("https://github.com/QiCaiJie114514");
        }));
        content.addView(makeAuthorRowPlaceholder(getString(R.string.about_original_author_placeholder), v -> {
        }));

        dialog.setContentView(content);
        dialog.show();
    }

    private void showContributorsSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(0, dp(12), 0, dp(24));

        content.addView(makeDragHandle());
        content.addView(sheetTitle(R.string.about_contribs));

        content.addView(makeAuthorRow(R.drawable.avatar_chinesebg, "Chinesebg", v -> {
            dialog.dismiss();
            openUrl("https://github.com/Chinesebg");
        }));
        content.addView(makeAuthorRow(R.drawable.avatar3, getString(R.string.about_contrib_do_not), v -> {
            dialog.dismiss();
            showImagePopup(R.drawable.src1);
        }));

        dialog.setContentView(content);
        dialog.show();
    }

    private View makeDragHandle() {
        View handle = new View(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        bg.setCornerRadius(dp(2));
        handle.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(32), dp(4));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        handle.setLayoutParams(lp);
        return handle;
    }

    private TextView sheetTitle(int res) {
        TextView tv = new TextView(this);
        tv.setText(res);
        TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        tv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(8), 0, dp(8));
        tv.setLayoutParams(lp);
        return tv;
    }

    private View makeAuthorRow(int avatarRes, String name, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(24), dp(10), dp(24), dp(10));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue bg = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, bg, true);
        row.setBackgroundResource(bg.resourceId);
        row.setOnClickListener(onClick);

        row.addView(makeCircularAvatar(avatarRes), new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView nameTv = new TextView(this);
        nameTv.setText(name);
        TextViewCompat.setTextAppearance(nameTv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        nameTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        nLp.setMarginStart(dp(16));
        row.addView(nameTv, nLp);

        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private View makeAuthorRowPlaceholder(String name, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(24), dp(10), dp(24), dp(10));
        row.setClickable(true);
        row.setFocusable(true);
        TypedValue bg = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, bg, true);
        row.setBackgroundResource(bg.resourceId);
        row.setOnClickListener(onClick);

        row.addView(makePlaceholderAvatar(), new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView nameTv = new TextView(this);
        nameTv.setText(name);
        TextViewCompat.setTextAppearance(nameTv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
        nameTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        nLp.setMarginStart(dp(16));
        row.addView(nameTv, nLp);

        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private ImageView makeCircularAvatar(int resId) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(resId);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setClipToOutline(true);
        iv.setOutlineProvider(circleOutline());
        return iv;
    }

    private ImageView makePlaceholderAvatar() {
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.ic_person);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(colorAttr(com.google.android.material.R.attr.colorSurfaceVariant));
        bg.setStroke(dp(1), colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        iv.setBackground(bg);
        iv.setColorFilter(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        return iv;
    }

    private ViewOutlineProvider circleOutline() {
        return new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        };
    }

    private void showImagePopup(int imageRes) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(16), dp(12), dp(16), dp(24));

        content.addView(makeDragHandle());

        ImageView img = new ImageView(this);
        img.setImageResource(imageRes);
        img.setAdjustViewBounds(true);
        content.addView(img, new LinearLayout.LayoutParams(-1, -2));

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

    private Drawable loadAppIcon() {
        try {
            return getPackageManager().getApplicationIcon(getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String versionName() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName != null ? info.versionName : "1.0";
        } catch (Exception e) {
            return "1.0";
        }
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
