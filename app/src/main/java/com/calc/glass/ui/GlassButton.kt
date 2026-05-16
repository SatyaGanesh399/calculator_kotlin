package com.calc.glass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

/**
 * A single round "glass" key on the calculator pad.
 *
 * Visually: a translucent vertical gradient pill with a soft 1dp border that
 * fakes the look of a backdrop blur. We avoid Modifier.blur because it only
 * works on API 31+ and can be GPU-expensive on a fast-redrawing keypad.
 */
@Composable
fun GlassButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: ButtonAccent = ButtonAccent.Neutral,
) {
    val (fillTop, fillBottom, borderColor, textColor) = accent.colors()
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(listOf(fillTop, fillBottom))
            )
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interaction,
                indication = rememberRipple(bounded = true, color = Color.White.copy(alpha = 0.35f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class ButtonAccent { Neutral, Operator, Function, Equals }

private data class ButtonColors(
    val top: Color,
    val bottom: Color,
    val border: Color,
    val text: Color
)

private fun ButtonAccent.colors(): ButtonColors = when (this) {
    ButtonAccent.Neutral -> ButtonColors(
        top = Color.White.copy(alpha = 0.18f),
        bottom = Color.White.copy(alpha = 0.06f),
        border = Color.White.copy(alpha = 0.30f),
        text = Color.White
    )
    ButtonAccent.Function -> ButtonColors(
        top = Color.White.copy(alpha = 0.28f),
        bottom = Color.White.copy(alpha = 0.10f),
        border = Color.White.copy(alpha = 0.45f),
        text = Color.White
    )
    ButtonAccent.Operator -> ButtonColors(
        top = Color(0xFFFF8AB6).copy(alpha = 0.55f),
        bottom = Color(0xFFB85CFF).copy(alpha = 0.55f),
        border = Color.White.copy(alpha = 0.55f),
        text = Color.White
    )
    ButtonAccent.Equals -> ButtonColors(
        top = Color(0xFFFFB36B),
        bottom = Color(0xFFFF5A8C),
        border = Color.White.copy(alpha = 0.65f),
        text = Color.White
    )
}
