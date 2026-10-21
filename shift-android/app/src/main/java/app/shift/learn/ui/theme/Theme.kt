package app.shift.learn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Tokens from the UI Direction sheet. White dominates in light theme; machine greys elsewhere.
private val Black = Color(0xFF0E0F11)
private val Graphite = Color(0xFF2B2F36)
private val Steel = Color(0xFF5B6270) // text grey: 5.5:1 on Mist, 6.1:1 on white
private val Aluminium = Color(0xFFC9CDD2)
private val Mist = Color(0xFFF2F3F5)
private val Hairline = Color(0xFFE4E6EA)

private val Light = lightColorScheme(
    primary = Black, onPrimary = Color.White,
    secondary = Graphite, onSecondary = Color.White,
    background = Color.White, onBackground = Black,
    surface = Color.White, onSurface = Black,
    surfaceVariant = Mist, onSurfaceVariant = Steel,
    outline = Aluminium, outlineVariant = Hairline,
    inverseSurface = Black, inverseOnSurface = Color.White,
)

// Dark surface values (#17191C) are my choice, not derived from the reference. Check contrast in review.
private val Dark = darkColorScheme(
    primary = Color.White, onPrimary = Black,
    secondary = Aluminium, onSecondary = Black,
    background = Black, onBackground = Mist,
    surface = Color(0xFF17191C), onSurface = Mist,
    surfaceVariant = Graphite, onSurfaceVariant = Aluminium,
    outline = Steel, outlineVariant = Graphite,
    inverseSurface = Mist, inverseOnSurface = Black,
)

@Composable
fun ShiftTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        shapes = Shapes(
            small = RoundedCornerShape(14.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
