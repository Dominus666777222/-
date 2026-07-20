package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.ConverterCategory
import com.example.util.Translator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val lang = viewModel.appLanguage

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


            // --- STANDARD DUAL-INPUT TRANSLATOR LAYOUT ---
            
            // Consistent Header Panel (Always visible, prevents layout shifts)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (viewModel.converterCategory == ConverterCategory.CURRENCY) {
                                val translatedStatus = when (viewModel.rateSyncStatus) {
                                    "Fallbacks Active" -> if (lang == "ru") "Резервные курсы активны" else "Fallbacks Active"
                                    "Rates Synced via Gemini" -> if (lang == "ru") "Курсы синхронизированы" else "Rates Synced via Gemini"
                                    "Syncing with Gemini..." -> if (lang == "ru") "Идёт синхронизация..." else "Syncing with Gemini..."
                                    else -> viewModel.rateSyncStatus
                                }
                                Text(
                                    text = translatedStatus,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (lang == "ru") "Курсы обновляются через Gemini" else "Rates updated via Gemini API",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            } else {
                                val headerTitle = when (viewModel.converterCategory) {
                                    ConverterCategory.LENGTH -> if (lang == "ru") "Конвертер длины" else "Length Converter"
                                    ConverterCategory.TEMPERATURE -> if (lang == "ru") "Температура" else "Temperature"
                                    ConverterCategory.WEIGHT -> if (lang == "ru") "Вес и масса" else "Weight & Mass"
                                    ConverterCategory.AREA -> if (lang == "ru") "Площадь" else "Area"
                                    ConverterCategory.VOLUME -> if (lang == "ru") "Объём" else "Volume"
                                    ConverterCategory.DATA_STORAGE -> if (lang == "ru") "Типы памяти" else "Data Storage"
                                    ConverterCategory.NUMBER_SYSTEMS -> if (lang == "ru") "Системы счисления" else "Number Systems"
                                    ConverterCategory.TIME -> if (lang == "ru") "Время" else "Time"
                                    ConverterCategory.SPEED -> if (lang == "ru") "Скорость" else "Speed"
                                    ConverterCategory.PRESSURE -> if (lang == "ru") "Давление" else "Pressure"
                                    ConverterCategory.ENERGY -> if (lang == "ru") "Энергия" else "Energy"
                                    ConverterCategory.FORCE -> if (lang == "ru") "Сила" else "Force"
                                    ConverterCategory.FREQUENCY -> if (lang == "ru") "Частота" else "Frequency"
                                    ConverterCategory.DENSITY -> if (lang == "ru") "Плотность" else "Density"
                                    ConverterCategory.ANGLE -> if (lang == "ru") "Угол" else "Angle"
                                    else -> ""
                                }
                                val headerDesc = when (viewModel.converterCategory) {
                                    ConverterCategory.LENGTH -> if (lang == "ru") "Перевод метрических и имперских единиц" else "Convert metric and imperial units"
                                    ConverterCategory.TEMPERATURE -> if (lang == "ru") "Перевод градусов Цельсия, Фаренгейта, Кельвина" else "Convert Celsius, Fahrenheit, Kelvin"
                                    ConverterCategory.WEIGHT -> if (lang == "ru") "Перевод килограммов, граммов, фунтов, унций" else "Convert kg, grams, pounds, ounces"
                                    ConverterCategory.AREA -> if (lang == "ru") "Метры², гектары, акры, квадратные мили" else "Square meters, hectares, acres"
                                    ConverterCategory.VOLUME -> if (lang == "ru") "Литры, миллилитры, галлоны, стаканы" else "Liters, milliliters, gallons, cups"
                                    ConverterCategory.DATA_STORAGE -> if (lang == "ru") "Байты, килобайты, мегабайты, гигабайты" else "Bytes, kilobytes, megabytes, gigabytes"
                                    ConverterCategory.NUMBER_SYSTEMS -> if (lang == "ru") "Перевод между 2-й, 8-й, 10-й, 16-й системами" else "Convert binary, octal, decimal, hex"
                                    ConverterCategory.TIME -> if (lang == "ru") "Секунды, минуты, часы, дни, недели, года" else "Seconds, minutes, hours, days, years"
                                    ConverterCategory.SPEED -> if (lang == "ru") "М/с, км/ч, мили/ч, узлы" else "M/s, km/h, mph, knots"
                                    ConverterCategory.PRESSURE -> if (lang == "ru") "Паскали, бары, атмосферы, торры" else "Pascals, bars, atmospheres, torr"
                                    ConverterCategory.ENERGY -> if (lang == "ru") "Джоули, калории, ватт-часы" else "Joules, calories, watt-hours"
                                    ConverterCategory.FORCE -> if (lang == "ru") "Ньютоны, дины, килограмм-силы" else "Newtons, dynes, kilogram-forces"
                                    ConverterCategory.FREQUENCY -> if (lang == "ru") "Герцы, килогерцы, мегагерцы, гигагерцы" else "Hertz, kilohertz, megahertz, gigahertz"
                                    ConverterCategory.DENSITY -> if (lang == "ru") "г/см³, кг/м³, фунты/фут³" else "g/cm³, kg/m³, lb/ft³"
                                    ConverterCategory.ANGLE -> if (lang == "ru") "Градусы, радианы, градианы" else "Degrees, radians, gradians"
                                    else -> ""
                                }
                                Text(
                                    text = headerTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = headerDesc,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        if (viewModel.converterCategory == ConverterCategory.CURRENCY) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.refreshCurrencyRates()
                                },
                                enabled = !viewModel.isRefreshingRates,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("sync_rates_btn")
                            ) {
                                if (viewModel.isRefreshingRates) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(Translator.translate("rate_sync", lang), fontSize = 12.sp)
                                }
                            }
                        } else {
                            val headerIcon = when (viewModel.converterCategory) {
                                ConverterCategory.LENGTH -> Icons.Default.Straighten
                                ConverterCategory.TEMPERATURE -> Icons.Default.DeviceThermostat
                                ConverterCategory.WEIGHT -> Icons.Default.Scale
                                ConverterCategory.AREA -> Icons.Default.Layers
                                ConverterCategory.VOLUME -> Icons.Default.WaterDrop
                                ConverterCategory.DATA_STORAGE -> Icons.Default.SdCard
                                ConverterCategory.NUMBER_SYSTEMS -> Icons.Default.Numbers
                                ConverterCategory.TIME -> Icons.Default.AccessTime
                                ConverterCategory.SPEED -> Icons.Default.Speed
                                ConverterCategory.PRESSURE -> Icons.Default.Compress
                                ConverterCategory.ENERGY -> Icons.Default.Bolt
                                ConverterCategory.FORCE -> Icons.Default.FitnessCenter
                                ConverterCategory.FREQUENCY -> Icons.Default.Waves
                                ConverterCategory.DENSITY -> Icons.Default.Opacity
                                ConverterCategory.ANGLE -> Icons.Default.Architecture
                                else -> Icons.Default.Category
                            }
                            Icon(
                                imageVector = headerIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(28.dp).padding(end = 4.dp)
                            )
                        }
                    }
                }
            }

            // Dual Conversion Panels
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Source Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Translator.translate("from_unit", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Unit Selection Dropdown
                                UnitDropdownSelector(
                                    selectedUnit = viewModel.converterSourceUnit,
                                    units = viewModel.getCategoryUnits(viewModel.converterCategory),
                                    onUnitSelected = { viewModel.onSourceUnitSelected(it) },
                                    modifier = Modifier.weight(1.2f),
                                    lang = lang
                                )

                                // Number Text Field
                                val isHexInput = viewModel.converterCategory == ConverterCategory.NUMBER_SYSTEMS && viewModel.converterSourceUnit == "Hexadecimal"
                                OutlinedTextField(
                                    value = viewModel.converterSourceValue,
                                    onValueChange = { viewModel.onConverterSourceValueChanged(it) },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = if (isHexInput) KeyboardType.Text else KeyboardType.Number,
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    modifier = Modifier
                                        .weight(1.8f)
                                        .height(50.dp)
                                        .testTag("converter_source_input"),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Floating Swap Button Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        FloatingActionButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onConverterUnitSwapped()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("swap_units_btn"),
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Swap Units")
                        }
                    }

                    // Target Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Translator.translate("to_unit", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Unit Selection Dropdown
                                UnitDropdownSelector(
                                    selectedUnit = viewModel.converterTargetUnit,
                                    units = viewModel.getCategoryUnits(viewModel.converterCategory),
                                    onUnitSelected = { viewModel.onTargetUnitSelected(it) },
                                    modifier = Modifier.weight(1.2f),
                                    lang = lang
                                )

                                // Target Number Text Field (Editable)
                                val isTargetHexInput = viewModel.converterCategory == ConverterCategory.NUMBER_SYSTEMS && viewModel.converterTargetUnit == "Hexadecimal"
                                OutlinedTextField(
                                    value = viewModel.converterTargetValue,
                                    onValueChange = { viewModel.onConverterTargetValueChanged(it) },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = if (isTargetHexInput) KeyboardType.Text else KeyboardType.Number,
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Done
                                     ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                    modifier = Modifier
                                        .weight(1.8f)
                                        .height(50.dp)
                                        .testTag("converter_target_input"),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Save to History Button
            item {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveCurrentConversionToHistory()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("save_conversion_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Translator.translate("save_to_history", lang))
                }
            }
    }
}

@Composable
fun UnitDropdownSelector(
    selectedUnit: String,
    units: List<String>,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    lang: String = "en"
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag("dropdown_trigger_$selectedUnit"),
            color = Color.Transparent,
            border = null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translator.translateUnit(selectedUnit, lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .testTag("dropdown_menu")
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(Translator.translateUnit(unit, lang)) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    },
                    modifier = Modifier.testTag("dropdown_item_$unit")
                )
            }
        }
    }
}

@Composable
private fun parseMarkdown(text: String): AnnotatedString {
    val primaryColor = MaterialTheme.colorScheme.primary
    return remember(text, primaryColor) {
        buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                var currentLine = line
                var isHeader = false
                var headerLevel = 0
                
                // Parse headers (e.g. ## Header)
                if (currentLine.startsWith("#")) {
                    isHeader = true
                    while (currentLine.startsWith("#")) {
                        headerLevel++
                        currentLine = currentLine.drop(1)
                    }
                    currentLine = currentLine.trim()
                }

                // If list item, add a bullet point or formatting
                val isBullet = currentLine.startsWith("- ") || currentLine.startsWith("* ")
                if (isBullet) {
                    currentLine = "  • " + currentLine.drop(2).trim()
                }

                val startIdx = this.length

                // Parse inline formatting: bold (**), italic (*)
                var i = 0
                while (i < currentLine.length) {
                    if (currentLine.startsWith("**", i)) {
                        val endBold = currentLine.indexOf("**", i + 2)
                        if (endBold != -1) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(currentLine.substring(i + 2, endBold))
                            }
                            i = endBold + 2
                        } else {
                            append("**")
                            i += 2
                        }
                    } else if (currentLine.startsWith("*", i)) {
                        val endItalic = currentLine.indexOf("*", i + 1)
                        if (endItalic != -1) {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(currentLine.substring(i + 1, endItalic))
                            }
                            i = endItalic + 1
                        } else {
                            append("*")
                            i += 1
                        }
                    } else {
                        append(currentLine[i])
                        i++
                    }
                }

                val endIdx = this.length
                if (isHeader) {
                    addStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontSize = if (headerLevel <= 2) 20.sp else 16.sp
                        ),
                        start = startIdx,
                        end = endIdx
                    )
                }

                if (index < lines.lastIndex) {
                    append("\n")
                }
            }
        }
    }
}
