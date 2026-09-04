package org.daidai.browser.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Daidai orange — deep, bitter, warm. */
private val LightColors = lightColorScheme(
    primary = Color(0xFF9C4A00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF351000),
    secondary = Color(0xFF4E7A27),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3E8B8),
    onSecondaryContainer = Color(0xFF122000),
    background = Color(0xFFFFFBF8),
    onBackground = Color(0xFF221A14),
    surface = Color(0xFFFFFBF8),
    onSurface = Color(0xFF221A14),
    surfaceVariant = Color(0xFFEEE0D3),
    onSurfaceVariant = Color(0xFF504539),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB77C),
    onPrimary = Color(0xFF522300),
    primaryContainer = Color(0xFF743500),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFB7CC96),
    onSecondary = Color(0xFF233910),
    secondaryContainer = Color(0xFF3A5023),
    onSecondaryContainer = Color(0xFFD3E8B8),
    background = Color(0xFF17120D),
    onBackground = Color(0xFFECE0D7),
    surface = Color(0xFF17120D),
    onSurface = Color(0xFFECE0D7),
    surfaceVariant = Color(0xFF40342A),
    onSurfaceVariant = Color(0xFFD3C4B6),
)

@Composable
fun DaidaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You tint on Android 12+; the daidai orange everywhere else.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
