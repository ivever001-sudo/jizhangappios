package com.example.accountingapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.accountingapp.platform.PlatformStatusBarColors

private val LightColorScheme = lightColorScheme(
    primary = Pink,
    onPrimary = White,
    primaryContainer = PinkLight,
    secondary = Mint,
    onSecondary = White,
    secondaryContainer = MintLight,
    tertiary = Sunshine,
    background = Cream,
    onBackground = BrownDark,
    surface = White,
    onSurface = BrownDark,
    surfaceVariant = CreamDark,
    onSurfaceVariant = BrownMedium,
    outline = BrownLight
)

@Composable
fun AccountingTheme(content: @Composable () -> Unit) {
    // 平台状态栏配置：Android → WindowCompat 沉浸式；iOS → 空（原生支持）
    PlatformStatusBarColors()

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
