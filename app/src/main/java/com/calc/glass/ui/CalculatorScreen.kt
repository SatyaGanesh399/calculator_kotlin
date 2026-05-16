package com.calc.glass.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calc.glass.Action
import com.calc.glass.CalcState
import com.calc.glass.Op

@Composable
fun CalculatorScreen() {
    var state by remember { mutableStateOf(CalcState()) }
    val onAction: (Action) -> Unit = { state = state.input(it) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient())
    ) {
        // Soft glowing orbs behind the glass for the frosted look.
        BackgroundOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Display(state)
            Spacer(Modifier.height(20.dp))
            Keypad(onAction)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BackgroundGradient(): Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1B0E3A), // deep indigo
        Color(0xFF3A1366), // violet
        Color(0xFF6B1F86), // magenta
        Color(0xFFB12569)  // rose
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

@Composable
private fun BackgroundOrbs() {
    // Two diffuse colored orbs to give the gradient depth — they sit behind
    // the glass surfaces and bleed through where the buttons are translucent.
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFFF82BC).copy(alpha = 0.55f),
            radius = size.minDimension * 0.55f,
            center = Offset(size.width * 0.15f, size.height * 0.20f)
        )
        drawCircle(
            color = Color(0xFF6FB8FF).copy(alpha = 0.45f),
            radius = size.minDimension * 0.65f,
            center = Offset(size.width * 0.95f, size.height * 0.85f)
        )
        drawCircle(
            color = Color(0xFFB47CFF).copy(alpha = 0.35f),
            radius = size.minDimension * 0.45f,
            center = Offset(size.width * 0.85f, size.height * 0.25f)
        )
    }
}

@Composable
private fun Display(state: CalcState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(32.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = state.expression.ifBlank { " " },
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 20.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = state.display,
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun Keypad(onAction: (Action) -> Unit) {
    // Five rows, four columns. Zero spans two columns.
    val rowMod = Modifier
        .fillMaxWidth()
        .aspectRatio(4f) // each row is 1/4 the keypad width tall
    val spacing = 10.dp

    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        Row(rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            Key("C",  ButtonAccent.Function) { onAction(Action.Clear) }
            Key("±",  ButtonAccent.Function) { onAction(Action.PlusMinus) }
            Key("%",  ButtonAccent.Function) { onAction(Action.Percent) }
            Key("÷",  ButtonAccent.Operator) { onAction(Action.Operator(Op.Div)) }
        }
        Row(rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            Key("7") { onAction(Action.Digit(7)) }
            Key("8") { onAction(Action.Digit(8)) }
            Key("9") { onAction(Action.Digit(9)) }
            Key("×",  ButtonAccent.Operator) { onAction(Action.Operator(Op.Mul)) }
        }
        Row(rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            Key("4") { onAction(Action.Digit(4)) }
            Key("5") { onAction(Action.Digit(5)) }
            Key("6") { onAction(Action.Digit(6)) }
            Key("−",  ButtonAccent.Operator) { onAction(Action.Operator(Op.Sub)) }
        }
        Row(rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            Key("1") { onAction(Action.Digit(1)) }
            Key("2") { onAction(Action.Digit(2)) }
            Key("3") { onAction(Action.Digit(3)) }
            Key("+",  ButtonAccent.Operator) { onAction(Action.Operator(Op.Add)) }
        }
        Row(rowMod, horizontalArrangement = Arrangement.spacedBy(spacing)) {
            // "0" is a wide key spanning two grid cells.
            GlassButton(
                label = "0",
                onClick = { onAction(Action.Digit(0)) },
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
            )
            Key(".") { onAction(Action.Dot) }
            Key("=",  ButtonAccent.Equals) { onAction(Action.Equals) }
        }
    }
}

@Composable
private fun RowScope.Key(
    label: String,
    accent: ButtonAccent = ButtonAccent.Neutral,
    onClick: () -> Unit
) {
    GlassButton(
        label = label,
        onClick = onClick,
        accent = accent,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    )
}
