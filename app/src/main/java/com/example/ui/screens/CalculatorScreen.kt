package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.Translator

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lang = viewModel.appLanguage

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Math Display Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            val scrollState = rememberScrollState()

            if (viewModel.isColumnDivisionActive) {
                // Column Division Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (lang == "ru") "Деление в столбик" else "Column Division",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Interactive Inputs Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dividend Box
                        val isDividendSelected = viewModel.activeColDivisionField == 0
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.activeColDivisionField = 0
                                }
                                .border(
                                    width = if (isDividendSelected) 2.dp else 1.dp,
                                    color = if (isDividendSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDividendSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (lang == "ru") "Делимое" else "Dividend",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.colDivisionDividend.ifEmpty { "0" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDividendSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }

                        // Divisor Box
                        val isDivisorSelected = viewModel.activeColDivisionField == 1
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.activeColDivisionField = 1
                                }
                                .border(
                                    width = if (isDivisorSelected) 2.dp else 1.dp,
                                    color = if (isDivisorSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDivisorSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (lang == "ru") "Делитель" else "Divisor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.colDivisionDivisor.ifEmpty { "0" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDivisorSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Render columns
                    val steps = viewModel.solveLongDivision(viewModel.colDivisionDividend, viewModel.colDivisionDivisor)
                    val divisionString = generateRussianStyleDivisionString(
                        viewModel.colDivisionDividend,
                        viewModel.colDivisionDivisor,
                        steps
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = divisionString,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Division Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val divisorVal = viewModel.colDivisionDivisor.toLongOrNull() ?: 1L
                        val resultQuotient = if (divisorVal != 0L) {
                            ((viewModel.colDivisionDividend.toLongOrNull() ?: 0L) / divisorVal).toString()
                        } else "0"

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.appendToInput("($resultQuotient)")
                                viewModel.isColumnDivisionActive = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = if (lang == "ru") "Вставить" else "Use Result")
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.appendToInput("($resultQuotient)^")
                                viewModel.isColumnDivisionActive = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = if (lang == "ru") "В степень" else "To Power")
                        }
                    }
                }
            } else {
                // Regular Expression Display
                AnimatedContent(
                    targetState = viewModel.calculatorInput,
                    transitionSpec = {
                        if (viewModel.isCalculated) {
                            (slideInVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) { height -> height } + fadeIn(animationSpec = tween(350)))
                                .togetherWith(slideOutVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { height -> -height / 2 } + fadeOut(animationSpec = tween(250)))
                        } else {
                            fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                        }
                    },
                    label = "expression_display_transition"
                ) { inputText ->
                    val displayValue = if (inputText.isEmpty()) "0" else viewModel.formatCalculatorResult(inputText)
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = if (displayValue.length > 10) 30.sp else 42.sp
                        ),
                        color = if (viewModel.isCalculatorError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 4.dp)
                            .testTag("calculator_display")
                    )
                }

                // Faded Live Preview / Result
                if (viewModel.calculatorPreviewResult.isNotEmpty()) {
                    Text(
                        text = viewModel.formatCalculatorResult(viewModel.calculatorPreviewResult),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .testTag("calculator_preview")
                    )
                } else if (viewModel.calculatorLastResult.isNotEmpty() && viewModel.calculatorInput == viewModel.calculatorLastResult) {
                    Text(
                        text = "Ans = ${viewModel.formatCalculatorResult(viewModel.calculatorLastResult)}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Tactile Keypad (with smooth vertical unroll and button compression!)
        val isSci = viewModel.isScientificMode
        val buttonHeight by animateDpAsState(
            targetValue = if (isSci) 44.dp else 62.dp,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "button_height"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 0: controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val topRow = listOf("C", "±", "÷", if (isSci) "🧮" else "🔬")
                for (key in topRow) {
                    CalculatorButton(
                        text = key,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (key == "🔬" || key == "🧮") {
                                viewModel.toggleScientificMode()
                            } else {
                                viewModel.onCalculatorKeyPress(key)
                            }
                        },
                        isScientific = isSci,
                        modifier = Modifier
                            .weight(1f)
                            .height(buttonHeight)
                    )
                }
            }

            // Scientific Drawer unrolling smoothly
            AnimatedVisibility(
                visible = isSci,
                enter = expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sciRows = listOf(
                        listOf("sin", "cos", "tan", "log", "√"),
                        listOf("ln", "π", "e", "(", ")"),
                        listOf("^", "!", "÷R", "mod", "x²")
                    )
                    for (row in sciRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (key in row) {
                                val isSelectedColDiv = key == "÷R" && viewModel.isColumnDivisionActive
                                CalculatorButton(
                                    text = key,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (key == "÷R") {
                                            viewModel.isColumnDivisionActive = !viewModel.isColumnDivisionActive
                                        } else {
                                            viewModel.onCalculatorKeyPress(key)
                                        }
                                    },
                                    isScientific = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(buttonHeight),
                                    containerColorOverride = if (isSelectedColDiv) MaterialTheme.colorScheme.primary else null,
                                    contentColorOverride = if (isSelectedColDiv) MaterialTheme.colorScheme.onPrimary else null
                                )
                            }
                        }
                    }
                }
            }

            // Standard Keys
            val stdRows = listOf(
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=", "⌫") // Swapped "=" and "⌫"!
            )
            for (row in stdRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (key in row) {
                        CalculatorButton(
                            text = key,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onCalculatorKeyPress(key)
                            },
                            isScientific = isSci,
                            modifier = Modifier
                                .weight(1f)
                                .height(buttonHeight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    isScientific: Boolean,
    modifier: Modifier = Modifier,
    containerColorOverride: Color? = null,
    contentColorOverride: Color? = null
) {
    val isToggle = text == "🔬" || text == "🧮"
    val isOperator = text == "+" || text == "-" || text == "×" || text == "÷" || text == "=" || text == "^"
    val isAction = text == "C" || text == "±" || text == "%" || text == "⌫" || text == "(" || text == ")"
    val isFunction = text == "sin" || text == "cos" || text == "tan" || text == "ln" || text == "log" || text == "√" || text == "÷R" || text == "mod" || text == "x²"
    val isConst = text == "π" || text == "e"

    val containerColor = containerColorOverride ?: when {
        text == "=" -> MaterialTheme.colorScheme.primary
        text == "C" -> MaterialTheme.colorScheme.errorContainer
        isToggle -> MaterialTheme.colorScheme.tertiaryContainer
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isAction -> MaterialTheme.colorScheme.tertiaryContainer
        isFunction -> MaterialTheme.colorScheme.secondaryContainer
        isConst -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    }

    val contentColor = contentColorOverride ?: when {
        text == "=" -> MaterialTheme.colorScheme.onPrimary
        text == "C" -> MaterialTheme.colorScheme.onErrorContainer
        isToggle -> MaterialTheme.colorScheme.onTertiaryContainer
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isAction -> MaterialTheme.colorScheme.onTertiaryContainer
        isFunction -> MaterialTheme.colorScheme.onSecondaryContainer
        isConst -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)) // Using beautiful modern rounded corner shape for all buttons
            .background(containerColor)
            .clickable(onClick = onClick)
            .testTag("btn_$text")
    ) {
        if (text == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Backspace",
                tint = contentColor,
                modifier = Modifier.size(if (isScientific) 20.dp else 24.dp)
            )
        } else if (text == "🔬") {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = "Switch to Scientific",
                tint = contentColor,
                modifier = Modifier.size(if (isScientific) 20.dp else 24.dp)
            )
        } else if (text == "🧮") {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = "Switch to Standard",
                tint = contentColor,
                modifier = Modifier.size(if (isScientific) 20.dp else 24.dp)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (isOperator || isAction || isFunction) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (text.length > 2) 15.sp else if (isScientific) 18.sp else 22.sp
                ),
                color = contentColor
            )
        }
    }
}

// Visual generator for Russian столбик division style
private fun generateRussianStyleDivisionString(
    dividendStr: String,
    divisorStr: String,
    steps: List<com.example.ui.viewmodel.CalculatorViewModel.LongDivisionStep>
): String {
    val divLong = divisorStr.toLongOrNull() ?: return ""
    val dvdLong = dividendStr.toLongOrNull() ?: return ""
    if (divLong == 0L) return "Деление на ноль / Division by zero"
    if (steps.isEmpty()) return "$dividendStr │ $divisorStr"

    val sb = java.lang.StringBuilder()
    // First line: dividend │ divisor
    val firstStep = steps[0]
    sb.append("${dividendStr} │ ${divisorStr}\n")

    // Second line: subtractValue │ divisor-underline
    val firstSub = firstStep.subtractValue
    val padSize = dividendStr.length - firstSub.length - firstStep.padding
    val divisorLine = "─".repeat(divisorStr.length.coerceAtLeast(6))
    sb.append("-${firstSub}${" ".repeat(padSize)} │ ${divisorLine}\n")

    // Third line: underline │ final quotient
    val underline = "─".repeat(firstSub.length)
    val finalQuotient = (dvdLong / divLong).toString()
    sb.append(" ${underline}${" ".repeat(padSize)} │ ${finalQuotient}\n")

    // Subsequent steps
    for (i in 1 until steps.size) {
        val step = steps[i]
        // Step dividend
        val divVal = step.currentDividend
        val padLength = step.padding
        val pad = " ".repeat(padLength + 1)
        sb.append("${pad}${divVal}\n")

        // Step subtraction
        val subVal = step.subtractValue
        sb.append("${pad.dropLast(1)}-${subVal}\n")

        // Step underline
        val und = "─".repeat(subVal.length)
        sb.append("${pad}${und}\n")
    }

    // Final remainder
    if (steps.isNotEmpty()) {
        val lastStep = steps.last()
        val spacesCount = dividendStr.length - lastStep.remainder.length
        sb.append("${" ".repeat(spacesCount)}  ${lastStep.remainder} (остаток / rem)\n")
    }

    return sb.toString()
}
