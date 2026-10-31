
package gt.edu.umg.misgastos.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta clara: verde salvia elegante.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF476B56),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE8DE),
    onPrimaryContainer = Color(0xFF263F30),

    secondary = Color(0xFF718C77),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8EFE8),
    onSecondaryContainer = Color(0xFF34443A),

    tertiary = Color(0xFF91AA96),

    background = Color(0xFFF7F8F5),
    onBackground = Color(0xFF34443A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF34443A),

    surfaceVariant = Color(0xFFE8EFE8),
    onSurfaceVariant = Color(0xFF526357),

    outline = Color(0xFF91A195),

    error = Color(0xFFB97970),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF2DEDA),
    onErrorContainer = Color(0xFF633D38)
)

// Paleta oscura: verde salvia suave y superficies profundas.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB1C9B4),
    onPrimary = Color(0xFF263F30),
    primaryContainer = Color(0xFF476B56),
    onPrimaryContainer = Color(0xFFE3EEE3),

    secondary = Color(0xFFB7CBB9),
    onSecondary = Color(0xFF34443A),
    secondaryContainer = Color(0xFF465B4B),
    onSecondaryContainer = Color(0xFFDDE8DE),

    tertiary = Color(0xFF91AA96),

    background = Color(0xFF1C241E),
    onBackground = Color(0xFFE5EAE4),

    surface = Color(0xFF252E27),
    onSurface = Color(0xFFE5EAE4),

    surfaceVariant = Color(0xFF39463C),
    onSurfaceVariant = Color(0xFFC8D3C9),

    outline = Color(0xFF87988A),

    error = Color(0xFFD9A29A),
    onError = Color(0xFF482C28),
    errorContainer = Color(0xFF633D38),
    onErrorContainer = Color(0xFFF2DEDA)
)

@Composable
fun MisGastosAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
