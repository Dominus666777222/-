package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.MathEvaluator
import com.example.util.Translator
import java.util.Locale

@Composable
fun GraphingScreen(viewModel: CalculatorViewModel) {
    val lang = viewModel.appLanguage
    val haptic = LocalHapticFeedback.current

    var formulaInput1 by remember { mutableStateOf("x^2") }
    var formulaInput2 by remember { mutableStateOf("sin(x)") }
    var activeInputIndex by remember { mutableStateOf(1) } // 1 for formula 1, 2 for formula 2

    // Graph viewport state
    var scale by remember { mutableStateOf(50f) } // Pixels per math unit
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val textPaintColor = MaterialTheme.colorScheme.onSurface

    fun evaluateFunction(expr: String, xValue: Double): Double {
        if (expr.isBlank()) return Double.NaN
        try {
            // Replace variable x with formatted double representation
            val formattedX = String.format(Locale.US, "(%.6f)", xValue)
            val preparedExpr = expr.replace(Regex("\\b[xX]\\b"), formattedX)
            val evalStr = MathEvaluator.evaluate(preparedExpr)
            return evalStr.toDoubleOrNull() ?: Double.NaN
        } catch (e: Exception) {
            return Double.NaN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("graphing_screen_root"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. Graph Display Canvas ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            // Coordinate Plane Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val originX = (canvasWidth / 2f) + offsetX
                val originY = (canvasHeight / 2f) + offsetY

                // DRAW GRID LINES
                val unitStep = when {
                    scale > 200f -> 0.2
                    scale > 100f -> 0.5
                    scale > 40f -> 1.0
                    scale > 15f -> 5.0
                    else -> 10.0
                }

                // Horizontal grid lines and Y-axis labels
                val maxVerticalUnits = (canvasHeight / scale).toInt() + 4
                val yStartOffset = (-offsetY / scale).toInt()
                for (j in (yStartOffset - maxVerticalUnits)..(yStartOffset + maxVerticalUnits)) {
                    val unitY = j * unitStep
                    val py = originY - (unitY * scale).toFloat()
                    if (py in 0f..canvasHeight) {
                        // Draw grid line
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, py),
                            end = Offset(canvasWidth, py),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // Vertical grid lines and X-axis labels
                val maxHorizontalUnits = (canvasWidth / scale).toInt() + 4
                val xStartOffset = (-offsetX / scale).toInt()
                for (i in (xStartOffset - maxHorizontalUnits)..(xStartOffset + maxHorizontalUnits)) {
                    val unitX = i * unitStep
                    val px = originX + (unitX * scale).toFloat()
                    if (px in 0f..canvasWidth) {
                        // Draw grid line
                        drawLine(
                            color = gridColor,
                            start = Offset(px, 0f),
                            end = Offset(px, canvasHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }

                // AXIS LINES (X and Y axis)
                if (originY in 0f..canvasHeight) {
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, originY),
                        end = Offset(canvasWidth, originY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                if (originX in 0f..canvasWidth) {
                    drawLine(
                        color = axisColor,
                        start = Offset(originX, 0f),
                        end = Offset(originX, canvasHeight),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // DRAW CURVE 1
                if (formulaInput1.isNotBlank()) {
                    val path1 = Path()
                    var firstPoint = true
                    for (pixelX in 0..canvasWidth.toInt() step 3) {
                        val mathX = (pixelX - originX) / scale
                        val mathY = evaluateFunction(formulaInput1, mathX.toDouble())
                        if (!mathY.isNaN() && !mathY.isInfinite()) {
                            val pixelY = originY - (mathY * scale).toFloat()
                            if (pixelY in -100f..(canvasHeight + 100f)) {
                                if (firstPoint) {
                                    path1.moveTo(pixelX.toFloat(), pixelY)
                                    firstPoint = false
                                } else {
                                    path1.lineTo(pixelX.toFloat(), pixelY)
                                }
                            }
                        }
                    }
                    drawPath(
                        path = path1,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // DRAW CURVE 2
                if (formulaInput2.isNotBlank()) {
                    val path2 = Path()
                    var firstPoint = true
                    for (pixelX in 0..canvasWidth.toInt() step 3) {
                        val mathX = (pixelX - originX) / scale
                        val mathY = evaluateFunction(formulaInput2, mathX.toDouble())
                        if (!mathY.isNaN() && !mathY.isInfinite()) {
                            val pixelY = originY - (mathY * scale).toFloat()
                            if (pixelY in -100f..(canvasHeight + 100f)) {
                                if (firstPoint) {
                                    path2.moveTo(pixelX.toFloat(), pixelY)
                                    firstPoint = false
                                } else {
                                    path2.lineTo(pixelX.toFloat(), pixelY)
                                }
                            }
                        }
                    }
                    drawPath(
                        path = path2,
                        color = secondaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // Zoom Overlay Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scale *= 1.25f
                    },
                    modifier = Modifier.size(40.dp).testTag("zoom_in_btn"),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
                }

                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (scale > 5f) scale /= 1.25f
                    },
                    modifier = Modifier.size(40.dp).testTag("zoom_out_btn"),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
                }

                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scale = 50f
                        offsetX = 0f
                        offsetY = 0f
                    },
                    modifier = Modifier.size(40.dp).testTag("zoom_reset_btn"),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Origin", modifier = Modifier.size(20.dp))
                }
            }
        }

        // --- 2. Interactive Formula Inputs ---
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Formula Card 1
            Card(
                onClick = { activeInputIndex = 1 },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeInputIndex == 1) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (activeInputIndex == 1) BorderStroke(1.5.dp, primaryColor) else null
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(primaryColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "y₁ = ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (activeInputIndex == 1) primaryColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (formulaInput1.isEmpty()) "..." else formulaInput1,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (formulaInput1.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (formulaInput1.isNotEmpty() && activeInputIndex == 1) {
                        IconButton(
                            onClick = { formulaInput1 = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Formula Card 2
            Card(
                onClick = { activeInputIndex = 2 },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeInputIndex == 2) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (activeInputIndex == 2) BorderStroke(1.5.dp, secondaryColor) else null
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(secondaryColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "y₂ = ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (activeInputIndex == 2) secondaryColor else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (formulaInput2.isEmpty()) "..." else formulaInput2,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (formulaInput2.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (formulaInput2.isNotEmpty() && activeInputIndex == 2) {
                        IconButton(
                            onClick = { formulaInput2 = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- 3. Custom Interactive Keypad ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val keyboardKeys = listOf(
                    listOf("x", "sin", "cos", "tan", "⌫"),
                    listOf("1", "2", "3", "+", "-"),
                    listOf("4", "5", "6", "×", "÷"),
                    listOf("7", "8", "9", "^", "C"),
                    listOf("0", ".", "(", ")", "π")
                )

                keyboardKeys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { key ->
                            val isAction = key == "⌫" || key == "C"
                            val isVar = key == "x" || key == "π"
                            val containerCol = when {
                                isAction -> MaterialTheme.colorScheme.errorContainer
                                isVar -> MaterialTheme.colorScheme.primaryContainer
                                key.any { it.isLetter() || it == '^' } -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                            val contentCol = when {
                                isAction -> MaterialTheme.colorScheme.onErrorContainer
                                isVar -> MaterialTheme.colorScheme.onPrimaryContainer
                                key.any { it.isLetter() || it == '^' } -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val currentVal = if (activeInputIndex == 1) formulaInput1 else formulaInput2
                                    val newVal = when (key) {
                                        "⌫" -> if (currentVal.isNotEmpty()) currentVal.dropLast(1) else currentVal
                                        "C" -> ""
                                        "sin", "cos", "tan" -> "${currentVal}${key}("
                                        "π" -> "${currentVal}π"
                                        "×" -> "${currentVal}*"
                                        "÷" -> "${currentVal}/"
                                        else -> currentVal + key
                                    }
                                    if (activeInputIndex == 1) {
                                        formulaInput1 = newVal
                                    } else {
                                        formulaInput2 = newVal
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = containerCol,
                                    contentColor = contentCol
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("keypad_key_$key"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper Modifier to clip graphics inside boundaries
fun Modifier.clipToBounds() = this.clip(RoundedCornerShape(16.dp))
