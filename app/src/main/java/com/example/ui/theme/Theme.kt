package com.example.ui.theme

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

private val LightColorScheme = lightColorScheme(
  primary = Emerald500,
  onPrimary = Color.White,
  primaryContainer = Emerald100,
  onPrimaryContainer = Emerald900,
  secondary = Emerald600,
  onSecondary = Color.White,
  secondaryContainer = Emerald50,
  onSecondaryContainer = Emerald800,
  tertiary = Amber500,
  onTertiary = Color.White,
  tertiaryContainer = Amber100,
  onTertiaryContainer = Amber700,
  background = GrayBackground,
  onBackground = GrayTextPrimary,
  surface = GraySurface,
  onSurface = GrayTextPrimary,
  surfaceVariant = GraySurfaceVariant,
  onSurfaceVariant = GrayTextSecondary,
  outline = GrayBorder,
  outlineVariant = Color(0xFFD1D5DB)
)

private val DarkColorScheme = darkColorScheme(
  primary = Emerald500,
  onPrimary = Color.White,
  primaryContainer = Emerald800,
  onPrimaryContainer = Emerald100,
  secondary = Emerald600,
  onSecondary = Color.White,
  secondaryContainer = Emerald900,
  onSecondaryContainer = Emerald200,
  tertiary = Amber400,
  onTertiary = Color.Black,
  tertiaryContainer = Amber700,
  onTertiaryContainer = Amber100,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  outlineVariant = Color(0xFF475569)
)

@Composable
fun SaleSnapTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Pin our intentional brand palette by default
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
