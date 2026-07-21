package com.example.ui.screens

import com.example.MainTopTabBar
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun GraphingScreen(viewModel: CalculatorViewModel) {
    val lang = viewModel.appLanguage
    val haptic = LocalHapticFeedback.current

    // Unlimited list of formulas initialized with default values
    val formulas = remember { mutableStateListOf("x^2", "sin(x)") }
    var activeInputIndex by remember { mutableStateOf(0) }

    // Graph viewport state
    var scale by remember { mutableStateOf(50f) } // Pixels per math unit
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val textPaintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val textMeasurer = rememberTextMeasurer()

    fun evaluateFunction(expr: String, xValue: Double): Double {
        if (expr.isBlank()) return Double.NaN
        try {
            // Replace variable x with formatted double representation
            val formattedX = String.format(Locale.US, "(%.6f)", xValue)
            val preparedExpr = expr.replace(Regex("\\b[xX]\\b"), formattedX)
            // Call MathEvaluator with isRadian = true for correct graphing behavior
            val evalStr = MathEvaluator.evaluate(preparedExpr, isRadian = true)
            return evalStr.toDoubleOrNull() ?: Double.NaN
        } catch (e: Exception) {
            return Double.NaN
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        MainTopTabBar(viewModel = viewModel, lang = lang)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .testTag("graphing_screen_root"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- 1. Graph Display Canvas ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
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

                    // DYNAMIC GRID STEP CALCULATION
                    // We target grid lines approximately 60 pixels apart on screen
                    val targetMathStep = 60.0 / scale.toDouble()
                    val logBase10 = kotlin.math.log10(targetMathStep)
                    val powerOf10 = java.lang.Math.pow(10.0, kotlin.math.floor(logBase10))
                    val ratio = targetMathStep / powerOf10
                    val unitStep = when {
                        ratio < 1.5 -> powerOf10 * 1.0
                        ratio < 3.5 -> powerOf10 * 2.0
                        ratio < 7.5 -> powerOf10 * 5.0
                        else -> powerOf10 * 10.0
                    }

                    // DRAW HORIZONTAL GRID LINES & DRAW Y-AXIS LABELS
                    // Convert screen top/bottom coordinates to unit ranges
                    val startStepY = (((offsetY - canvasHeight / 2f) / (unitStep * scale)).toInt() - 2)
                    val endStepY = (((offsetY + canvasHeight / 2f) / (unitStep * scale)).toInt() + 2)

                    for (j in startStepY..endStepY) {
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

                            // Y-axis Label
                            val label = if (unitY == 0.0) "" else {
                                if (unitY % 1.0 == 0.0) unitY.toLong().toString()
                                else String.format(Locale.US, "%.4f", unitY).trimEnd('0').trimEnd('.')
                            }
                            if (label.isNotEmpty()) {
                                val posX = when {
                                    originX in 20f..(canvasWidth - 45f) -> originX + 6f
                                    originX < 20f -> 20f
                                    else -> canvasWidth - 45f
                                }
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = label,
                                    topLeft = Offset(posX, py - 16f),
                                    style = TextStyle(color = textPaintColor, fontSize = 10.sp)
                                )
                            }
                        }
                    }

                    // DRAW VERTICAL GRID LINES & DRAW X-AXIS LABELS
                    // Convert screen left/right coordinates to unit ranges
                    val startStepX = (((-offsetX - canvasWidth / 2f) / (unitStep * scale)).toInt() - 2)
                    val endStepX = (((-offsetX + canvasWidth / 2f) / (unitStep * scale)).toInt() + 2)

                    for (i in startStepX..endStepX) {
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

                            // X-axis Label
                            val label = if (unitX == 0.0) "" else {
                                if (unitX % 1.0 == 0.0) unitX.toLong().toString()
                                else String.format(Locale.US, "%.4f", unitX).trimEnd('0').trimEnd('.')
                            }
                            if (label.isNotEmpty()) {
                                val posY = when {
                                    originY in 20f..(canvasHeight - 30f) -> originY + 4f
                                    originY < 20f -> 20f
                                    else -> canvasHeight - 30f
                                }
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = label,
                                    topLeft = Offset(px + 4f, posY),
                                    style = TextStyle(color = textPaintColor, fontSize = 10.sp)
                                )
                            }
                        }
                    }

                    // DRAW ORIGIN "0"
                    if (originX in 20f..(canvasWidth - 20f) && originY in 20f..(canvasHeight - 20f)) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "0",
                            topLeft = Offset(originX + 6f, originY + 4f),
                            style = TextStyle(color = textPaintColor, fontSize = 10.sp)
                        )
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

                    // DRAW ALL EQUATIONS
                    val curveColors = listOf(
                        primaryColor,
                        secondaryColor,
                        Color(0xFF4CAF50), // Green
                        Color(0xFFFF9800), // Orange
                        Color(0xFF9C27B0), // Purple
                        Color(0xFFE91E63), // Pink
                        Color(0xFF00BCD4), // Cyan
                        Color(0xFF3F51B5), // Indigo
                        Color(0xFF8BC34A), // Light Green
                        Color(0xFFE91E63)  // Secondary red
                    )

                    formulas.forEachIndexed { index, formula ->
                        if (formula.isNotBlank()) {
                            val path = Path()
                            var firstPoint = true
                            for (pixelX in 0..canvasWidth.toInt() step 3) {
                                val mathX = (pixelX - originX) / scale
                                val mathY = evaluateFunction(formula, mathX.toDouble())
                                if (!mathY.isNaN() && !mathY.isInfinite()) {
                                    val pixelY = originY - (mathY * scale).toFloat()
                                    if (pixelY in -100f..(canvasHeight + 100f)) {
                                        if (firstPoint) {
                                            path.moveTo(pixelX.toFloat(), pixelY)
                                            firstPoint = false
                                        } else {
                                            path.lineTo(pixelX.toFloat(), pixelY)
                                        }
                                    }
                                }
                            }
                            drawPath(
                                path = path,
                                color = curveColors[index % curveColors.size],
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
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
            val curveColors = listOf(
                primaryColor,
                secondaryColor,
                Color(0xFF4CAF50),
                Color(0xFFFF9800),
                Color(0xFF9C27B0),
                Color(0xFFE91E63),
                Color(0xFF00BCD4),
                Color(0xFF3F51B5),
                Color(0xFF8BC34A),
                Color(0xFFE91E63)
            )

            Column(
                modifier = Modifier
                    .heightIn(max = 140.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                formulas.forEachIndexed { index, formula ->
                    val isActive = activeInputIndex == index
                    val color = curveColors[index % curveColors.size]

                    Card(
                        onClick = { activeInputIndex = index },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) {
                                color.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isActive) BorderStroke(1.5.dp, color) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "y${toSubscript(index + 1)} = ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isActive) color else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (formula.isEmpty()) "..." else formula,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                                color = if (formula.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (formulas.size > 1) {
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        formulas.removeAt(index)
                                        if (activeInputIndex >= formulas.size) {
                                            activeInputIndex = formulas.lastIndex
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Formula", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                }
                            } else {
                                if (formula.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            formulas[index] = ""
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Formula", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Plus (+) Card to add new formulas (unlimited graphing)
                Card(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        formulas.add("")
                        activeInputIndex = formulas.lastIndex
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Graph",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
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
                        listOf("0", ".", "(", ")", "π"),
                        listOf("√", "ln", "log", "abs", "exp")
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
                                    key.any { it.isLetter() || it == '^' || it == '√' } -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val contentCol = when {
                                    isAction -> MaterialTheme.colorScheme.onErrorContainer
                                    isVar -> MaterialTheme.colorScheme.onPrimaryContainer
                                    key.any { it.isLetter() || it == '^' || it == '√' } -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (activeInputIndex in formulas.indices) {
                                            val currentVal = formulas[activeInputIndex]
                                            val newVal = when (key) {
                                                "⌫" -> if (currentVal.isNotEmpty()) currentVal.dropLast(1) else currentVal
                                                "C" -> ""
                                                "sin", "cos", "tan", "abs", "exp", "log", "ln" -> "${currentVal}${key}("
                                                "√" -> "${currentVal}√("
                                                "π" -> "${currentVal}π"
                                                "×" -> "${currentVal}*"
                                                "÷" -> "${currentVal}/"
                                                else -> currentVal + key
                                            }
                                            formulas[activeInputIndex] = newVal
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
}

// Helper subscript utility
fun toSubscript(num: Int): String {
    val numStr = num.toString()
    val sb = StringBuilder()
    for (ch in numStr) {
        val sub = when (ch) {
            '0' -> '₀'
            '1' -> '₁'
            '2' -> '₂'
            '3' -> '₃'
            '4' -> '₄'
            '5' -> '₅'
            '6' -> '₆'
            '7' -> '₇'
            '8' -> '₈'
            '9' -> '₉'
            else -> ch
        }
        sb.append(sub)
    }
    return sb.toString()
}
