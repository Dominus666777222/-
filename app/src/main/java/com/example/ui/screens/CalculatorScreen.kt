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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.Translator
import com.example.MainTopTabBar

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lang = viewModel.appLanguage

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Local Tab Bar keeps top region 100% stable in height!
        MainTopTabBar(viewModel = viewModel, lang = lang)

        Column(
            modifier = Modifier
                .weight(1f)
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

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tactile Keypad
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
                // Scientific Drawer unrolling smoothly at the top!
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
                            listOf("^", "!", "%", "1/x", "x²")
                        )
                        for (row in sciRows) {
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
                                        isScientific = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(buttonHeight)
                                    )
                                }
                            }
                        }
                    }
                }

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

                // Standard Keys
                val stdRows = listOf(
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "=", "⌫")
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
    val isOperator = text == "+" || text == "-" || text == "×" || text == "÷" || text == "=" || text == "^" || text == "%"
    val isAction = text == "C" || text == "±" || text == "⌫" || text == "(" || text == ")"
    val isFunction = text == "sin" || text == "cos" || text == "tan" || text == "ln" || text == "log" || text == "√" || text == "1/x" || text == "x²"
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
            .clip(RoundedCornerShape(16.dp))
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
