package ru.hyper.messenger.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val baseTypography = Typography()

val HyperTypography = Typography(
    displayLarge = baseTypography.displayLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = baseTypography.displayMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-1).sp,
    ),
    displaySmall = baseTypography.displaySmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineLarge = baseTypography.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = baseTypography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = baseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = baseTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = baseTypography.bodyLarge.copy(lineHeight = 24.sp),
    bodyMedium = baseTypography.bodyMedium.copy(lineHeight = 21.sp),
    labelLarge = baseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = baseTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = baseTypography.labelSmall.copy(letterSpacing = 0.5.sp),
)

val HyperShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

@Composable
fun HyperShapesForPreview() = MaterialTheme.shapes
