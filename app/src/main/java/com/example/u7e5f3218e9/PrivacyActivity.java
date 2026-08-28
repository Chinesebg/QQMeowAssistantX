package com.example.u7e5f3218e9;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 隐私权限说明：以表格列出本应用申请的权限及其用途。
 * 「无障碍服务权限」「联网权限」为高风险权限，加粗标红。
 */
public class PrivacyActivity extends AppCompatActivity {

    private static final class PermissionItem {
        final String name;
        final String fullName;
        final String purpose;
        final boolean highlight;

        PermissionItem(String name, String fullName, String purpose, boolean highlight) {
            this.name = name;
            this.fullName = fullName;
            this.purpose = purpose;
            this.highlight = highlight;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeManager.themeRes(this));
        super.onCreate(savedInstanceState);
        setContentView(buildContent());
    }

    private LinearLayout buildContent() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle(R.string.settings_privacy);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(32));

        TextView intro = new TextView(this);
        intro.setText(R.string.privacy_intro);
        TextViewCompat.setTextAppearance(intro,
                com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
        intro.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        content.addView(intro, new LinearLayout.LayoutParams(-1, -2));

        content.addView(buildTable(buildItems()),
                new LinearLayout.LayoutParams(-1, -2));

        scroll.addView(content, new LinearLayout.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        return root;
    }

    private List<PermissionItem> buildItems() {
        List<PermissionItem> items = new ArrayList<>();
        items.add(new PermissionItem(
                getString(R.string.privacy_perm_accessibility),
                getString(R.string.privacy_perm_accessibility_full),
                getString(R.string.privacy_perm_accessibility_purpose), true));
        items.add(new PermissionItem(
                getString(R.string.privacy_perm_internet),
                getString(R.string.privacy_perm_internet_full),
                getString(R.string.privacy_perm_internet_purpose), true));
        items.add(new PermissionItem(
                getString(R.string.privacy_perm_overlay),
                getString(R.string.privacy_perm_overlay_full),
                getString(R.string.privacy_perm_overlay_purpose), false));
        items.add(new PermissionItem(
                getString(R.string.privacy_perm_shizuku),
                getString(R.string.privacy_perm_shizuku_full),
                getString(R.string.privacy_perm_shizuku_purpose), false));
        return items;
    }

    private View buildTable(List<PermissionItem> items) {
        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);

        // 表头
        table.addView(buildRow(getString(R.string.privacy_col_permission), null,
                getString(R.string.privacy_col_purpose), false, true));
        table.addView(buildDivider());

        // 数据行
        for (int i = 0; i < items.size(); i++) {
            PermissionItem item = items.get(i);
            table.addView(buildRow(item.name, item.fullName, item.purpose, item.highlight, false));
            if (i < items.size() - 1) {
                table.addView(buildDivider());
            }
        }

        MaterialCardView card = new MaterialCardView(this);
        card.setCardElevation(0);
        card.setStrokeWidth(0);
        card.setRadius(dp(16));
        card.setCardBackgroundColor(colorAttr(com.google.android.material.R.attr.colorSurfaceVariant));
        card.addView(table, new LinearLayout.LayoutParams(-1, -2));
        return card;
    }

    /** 表格单行：左列=权限名（+完整权限字符串），右列=用途。header 为表头。 */
    private View buildRow(String left, String leftSub, String right, boolean highlight, boolean header) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));

        LinearLayout leftCol = new LinearLayout(this);
        leftCol.setOrientation(LinearLayout.VERTICAL);

        TextView leftTv = new TextView(this);
        leftTv.setText(left);
        if (header) {
            TextViewCompat.setTextAppearance(leftTv,
                    com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
            leftTv.setTypeface(Typeface.DEFAULT_BOLD);
            leftTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        } else {
            TextViewCompat.setTextAppearance(leftTv,
                    com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            if (highlight) {
                leftTv.setTypeface(Typeface.DEFAULT_BOLD);
                leftTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorError));
            } else {
                leftTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurface));
            }
        }
        leftCol.addView(leftTv, new LinearLayout.LayoutParams(-1, -2));

        if (leftSub != null && !leftSub.isEmpty()) {
            TextView subTv = new TextView(this);
            subTv.setText(leftSub);
            TextViewCompat.setTextAppearance(subTv,
                    com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
            subTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
            subTv.setTypeface(Typeface.MONOSPACE);
            leftCol.addView(subTv, new LinearLayout.LayoutParams(-1, -2));
        }

        TextView rightTv = new TextView(this);
        rightTv.setText(right);
        if (header) {
            TextViewCompat.setTextAppearance(rightTv,
                    com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
            rightTv.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            TextViewCompat.setTextAppearance(rightTv,
                    com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
        }
        rightTv.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));

        row.addView(leftCol, new LinearLayout.LayoutParams(0, -2, 1.0f));
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
        rightLp.setMarginStart(dp(16));
        row.addView(rightTv, rightLp);
        return row;
    }

    private View buildDivider() {
        View v = new View(this);
        v.setBackgroundColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(1));
        lp.setMarginStart(dp(16));
        lp.setMarginEnd(dp(16));
        v.setLayoutParams(lp);
        return v;
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
