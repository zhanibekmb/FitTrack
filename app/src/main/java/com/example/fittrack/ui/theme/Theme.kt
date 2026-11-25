package com.example.fittrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Green80, onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005235), onPrimaryContainer = Color(0xFFB0F1CC),
    secondary = Slate80, onSecondary = Color(0xFF21352A),
    secondaryContainer = Color(0xFF374B3F), onSecondaryContainer = Color(0xFFD1E8DA),
    tertiary = Blue80, onTertiary = Color(0xFF003543),
    tertiaryContainer = Color(0xFF1C4D5C), onTertiaryContainer = Color(0xFFC1E9FA),
    background = Color(0xFF101511), onBackground = Color(0xFFE0E4DE),
    surface = Color(0xFF101511), onSurface = Color(0xFFE0E4DE),
    surfaceVariant = Color(0xFF404943), onSurfaceVariant = Color(0xFFC0C9C1),
    surfaceContainerLowest = Color(0xFF0B100C), surfaceContainerLow = Color(0xFF191E19),
    surfaceContainer = Color(0xFF1D221D), surfaceContainerHigh = Color(0xFF272C27),
    surfaceContainerHighest = Color(0xFF323731), surfaceTint = Green80,
    outline = Color(0xFF8A938B), outlineVariant = Color(0xFF404943),
    inverseSurface = Color(0xFFE0E4DE), inverseOnSurface = Color(0xFF2D322C), inversePrimary = Green40
)
private val LightColorScheme = lightColorScheme(
    primary = Green40, onPrimary = Color.White,
    primaryContainer = Color(0xFFB0F1CC), onPrimaryContainer = Color(0xFF002112),
    secondary = Slate40, onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E8DA), onSecondaryContainer = Color(0xFF0C2015),
    tertiary = Blue40, onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC1E9FA), onTertiaryContainer = Color(0xFF001F28),
    background = Color(0xFFF6FBF3), onBackground = Color(0xFF181D18),
    surface = Color(0xFFF6FBF3), onSurface = Color(0xFF181D18),
    surfaceVariant = Color(0xFFDCE5DC), onSurfaceVariant = Color(0xFF404943),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF0F5ED),
    surfaceContainer = Color(0xFFEAEFE7), surfaceContainerHigh = Color(0xFFE4E9E1),
    surfaceContainerHighest = Color(0xFFDEE3DB), surfaceTint = Green40,
    outline = Color(0xFF707971), outlineVariant = Color(0xFFC0C9C1),
    inverseSurface = Color(0xFF2D322C), inverseOnSurface = Color(0xFFEFF2EA), inversePrimary = Green80
)
private val FitTrackShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FitTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, shapes = FitTrackShapes, content = content)
}
