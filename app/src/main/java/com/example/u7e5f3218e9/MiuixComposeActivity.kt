package com.example.u7e5f3218e9

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import androidx.core.view.WindowCompat
import com.example.u7e5f3218e9.config.AppConfig
import com.example.u7e5f3218e9.config.ConfigRepository
import com.example.u7e5f3218e9.ui.LocalBackdrop
import com.example.u7e5f3218e9.ui.MiaoAssistantApp
import com.example.u7e5f3218e9.ui.MiaoAssistantTheme

class MiuixComposeActivity : ComponentActivity() {
    private lateinit var configRepository: ConfigRepository
    private var config by mutableStateOf(AppConfig())
    private var serviceEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        configRepository = ConfigRepository(this)
        config = configRepository.load()
        serviceEnabled = isAccessibilityServiceEnabled()

        setContent {
            val bgBitmap = remember(config.backgroundUri) { loadBackgroundBitmap(config.backgroundUri) }
            val backdrop = if (config.cardLiquidGlass) {
                rememberLayerBackdrop {
                    drawRect(Color.Transparent)
                    drawContent()
                }
            } else {
                null
            }
            Box(Modifier.fillMaxSize()) {
                if (backdrop != null) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop)
                    ) {
                        if (bgBitmap != null) {
                            Image(
                                bitmap = bgBitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                colorFilter = remember(config.backgroundBrightness) {
                                    brightnessColorFilter(config.backgroundBrightness)
                                },
                            )
                        }
                    }
                } else {
                    if (bgBitmap != null) {
                        Image(
                            bitmap = bgBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            colorFilter = remember(config.backgroundBrightness) {
                                brightnessColorFilter(config.backgroundBrightness)
                            },
                        )
                    }
                }
                CompositionLocalProvider(LocalBackdrop provides backdrop) {
                    MiaoAssistantTheme(
                        themeMode = config.themeMode,
                        useMonet = config.useMonet,
                        themeColor = config.themeColor,
                    ) {
                        MiaoAssistantApp(
                            config = config,
                            serviceEnabled = serviceEnabled,
                            onConfigChange = ::saveConfig,
                            onOpenAccessibilitySettings = ::openAccessibilitySettings,
                            onInvalidRules = { count ->
                                Toast.makeText(this@MiuixComposeActivity, "已忽略 $count 行无效规则", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        config = configRepository.load()
        serviceEnabled = isAccessibilityServiceEnabled()
    }

    private fun saveConfig(updated: AppConfig) {
        config = updated
        configRepository.save(updated)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val am = getSystemService(ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
                ?: return false
            val services = am.getEnabledAccessibilityServiceList(-1)
            services.any { info ->
                info.resolveInfo?.serviceInfo?.packageName == packageName
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun brightnessColorFilter(brightness: Int): ColorFilter? {
        if (brightness == 0) return null
        val offset = brightness * 255f / 100f
        return ColorFilter.colorMatrix(
            ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, offset,
                    0f, 1f, 0f, 0f, offset,
                    0f, 0f, 1f, 0f, offset,
                    0f, 0f, 0f, 1f, 0f,
                ),
            ),
        )
    }

    private fun loadBackgroundBitmap(uri: String?): Bitmap? {
        if (uri.isNullOrEmpty()) return null
        return try {
            val input = contentResolver.openInputStream(Uri.parse(uri)) ?: return null
            val bmp = BitmapFactory.decodeStream(input)
            input.close()
            bmp
        } catch (_: Exception) {
            null
        }
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (_: Exception) {
            Toast.makeText(this, R.string.toast_cannot_open_settings, Toast.LENGTH_SHORT).show()
        }
    }
}
