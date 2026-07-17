package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.ConverterCategory
import com.example.ui.viewmodel.ActiveTool
import com.example.util.Translator
import kotlin.math.roundToInt

data class HubItem(
    val id: String,
    val labelKey: String,
    val icon: ImageVector,
    val activeTool: ActiveTool
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val lang = viewModel.appLanguage
    val activeTool = viewModel.activeTool

    val allHubItems = remember {
        mapOf(
            "currency" to HubItem("currency", "currency", Icons.Default.CurrencyExchange, ActiveTool.Converter(ConverterCategory.CURRENCY)),
            "length" to HubItem("length", "length", Icons.Default.Straighten, ActiveTool.Converter(ConverterCategory.LENGTH)),
            "temp" to HubItem("temp", "temperature", Icons.Default.DeviceThermostat, ActiveTool.Converter(ConverterCategory.TEMPERATURE)),
            "weight" to HubItem("weight", "weight", Icons.Default.Scale, ActiveTool.Converter(ConverterCategory.WEIGHT)),
            "area" to HubItem("area", "area", Icons.Default.Layers, ActiveTool.Converter(ConverterCategory.AREA)),
            "volume" to HubItem("volume", "volume", Icons.Default.WaterDrop, ActiveTool.Converter(ConverterCategory.VOLUME)),
            "data" to HubItem("data", "data_storage", Icons.Default.SdCard, ActiveTool.Converter(ConverterCategory.DATA_STORAGE)),
            "binary" to HubItem("binary", "number_systems", Icons.Default.Numbers, ActiveTool.Converter(ConverterCategory.NUMBER_SYSTEMS)),
            "time" to HubItem("time", "time", Icons.Default.AccessTime, ActiveTool.Converter(ConverterCategory.TIME)),
            "speed" to HubItem("speed", "speed", Icons.Default.Speed, ActiveTool.Converter(ConverterCategory.SPEED)),
            "pressure" to HubItem("pressure", "pressure", Icons.Default.Compress, ActiveTool.Converter(ConverterCategory.PRESSURE)),
            "energy" to HubItem("energy", "energy", Icons.Default.Bolt, ActiveTool.Converter(ConverterCategory.ENERGY)),
            "bmi" to HubItem("bmi", "bmi", Icons.Default.AccessibilityNew, ActiveTool.Bmi),
            "date" to HubItem("date", "date_calc", Icons.Default.CalendarMonth, ActiveTool.DateCalc),
            // New tools (hidden by default):
            "force" to HubItem("force", "force", Icons.Default.FitnessCenter, ActiveTool.Converter(ConverterCategory.FORCE)),
            "frequency" to HubItem("frequency", "frequency", Icons.Default.Waves, ActiveTool.Converter(ConverterCategory.FREQUENCY)),
            "density" to HubItem("density", "density", Icons.Default.Opacity, ActiveTool.Converter(ConverterCategory.DENSITY)),
            "angle" to HubItem("angle", "angle", Icons.Default.Architecture, ActiveTool.Converter(ConverterCategory.ANGLE))
        )
    }

    val visibleItems = remember(viewModel.toolsOrder, viewModel.hiddenTools) {
        viewModel.toolsOrder
            .filter { !viewModel.hiddenTools.contains(it) }
            .mapNotNull { allHubItems[it] }
    }

    val hiddenItems = remember(viewModel.toolsOrder, viewModel.hiddenTools) {
        viewModel.toolsOrder
            .filter { viewModel.hiddenTools.contains(it) }
            .mapNotNull { allHubItems[it] }
    }

    // Animation transition setup for wave-like pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val timeMillis by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 2000,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // State for local dragging
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    AnimatedContent(
        targetState = activeTool,
        transitionSpec = {
            if (targetState is ActiveTool.Menu) {
                (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
            } else {
                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            }
        },
        label = "active_tool_transition"
    ) { currentTool ->
        when (currentTool) {
            is ActiveTool.Menu -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row with title and clean pencil/checkmark edit mode button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Translator.translate("tools", lang),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.isEditMode = !viewModel.isEditMode
                                if (!viewModel.isEditMode) {
                                    draggedIndex = null
                                    dragOffsetX = 0f
                                    dragOffsetY = 0f
                                }
                            },
                            modifier = Modifier.testTag("toggle_edit_mode_btn")
                        ) {
                            Icon(
                                imageVector = if (viewModel.isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (viewModel.isEditMode) "Done" else "Edit tools layout",
                                tint = if (viewModel.isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Visible Grid (implemented using standard Column of Rows for perfect scrolling)
                    val visibleRows = visibleItems.chunked(3)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        visibleRows.forEachIndexed { rowIndex, rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowItems.forEachIndexed { colIndex, item ->
                                    val idx = rowIndex * 3 + colIndex
                                    val isDraggingThis = draggedIndex == idx

                                    // Wave scale pulsation calculation
                                    val itemScale = if (viewModel.isEditMode && draggedIndex != idx) {
                                        val itemOffset = idx * 150
                                        val progress = ((timeMillis + itemOffset) % 2000) / 2000f
                                        1f + 0.03f * kotlin.math.sin(progress * 2 * kotlin.math.PI).toFloat()
                                    } else 1f

                                    // Item gesture and translation modifier
                                    val itemModifier = if (viewModel.isEditMode) {
                                        Modifier
                                            .graphicsLayer {
                                                if (isDraggingThis) {
                                                    translationX = dragOffsetX
                                                    translationY = dragOffsetY
                                                    scaleX = 1.1f
                                                    scaleY = 1.1f
                                                    shadowElevation = 8.dp.toPx()
                                                } else {
                                                    scaleX = itemScale
                                                    scaleY = itemScale
                                                }
                                            }
                                            .pointerInput(idx) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        draggedIndex = idx
                                                        dragOffsetX = 0f
                                                        dragOffsetY = 0f
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffsetX += dragAmount.x
                                                        dragOffsetY += dragAmount.y

                                                        val cellWidthPx = 110.dp.toPx()
                                                        val cellHeightPx = 90.dp.toPx()

                                                        val colShift = (dragOffsetX / cellWidthPx).roundToInt()
                                                        val rowShift = (dragOffsetY / cellHeightPx).roundToInt()

                                                        if (colShift != 0 || rowShift != 0) {
                                                            val curCol = idx % 3
                                                            val curRow = idx / 3
                                                            val targetCol = (curCol + colShift).coerceIn(0, 2)
                                                            val targetRow = (curRow + rowShift).coerceIn(0, (visibleItems.size - 1) / 3)
                                                            val targetIdx = targetRow * 3 + targetCol
                                                            if (targetIdx in visibleItems.indices && targetIdx != idx) {
                                                                val mutableOrder = viewModel.toolsOrder.toMutableList()
                                                                val id1 = visibleItems[idx].id
                                                                val id2 = visibleItems[targetIdx].id
                                                                val o1 = mutableOrder.indexOf(id1)
                                                                val o2 = mutableOrder.indexOf(id2)
                                                                if (o1 != -1 && o2 != -1) {
                                                                    mutableOrder[o1] = id2
                                                                    mutableOrder[o2] = id1
                                                                    viewModel.updateToolsOrder(mutableOrder)
                                                                    draggedIndex = targetIdx
                                                                    dragOffsetX -= colShift * cellWidthPx
                                                                    dragOffsetY -= rowShift * cellHeightPx
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        draggedIndex = null
                                                        dragOffsetX = 0f
                                                        dragOffsetY = 0f
                                                    },
                                                    onDragCancel = {
                                                        draggedIndex = null
                                                        dragOffsetX = 0f
                                                        dragOffsetY = 0f
                                                    }
                                                )
                                            }
                                    } else {
                                        Modifier.clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (item.activeTool is ActiveTool.Converter) {
                                                viewModel.onConverterCategoryChanged(item.activeTool.category)
                                            }
                                            viewModel.activeTool = item.activeTool
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .then(itemModifier)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isDraggingThis) MaterialTheme.colorScheme.surfaceVariant
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .padding(vertical = 5.dp, horizontal = 4.dp)
                                            .testTag("hub_item_${item.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (item.id == "ai") MaterialTheme.colorScheme.tertiaryContainer
                                                        else MaterialTheme.colorScheme.secondaryContainer
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = Translator.translate(item.labelKey, lang),
                                                    tint = if (item.id == "ai") MaterialTheme.colorScheme.tertiary
                                                           else MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                            Text(
                                                text = Translator.translate(item.labelKey, lang),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                textAlign = TextAlign.Center,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        // Delete cross in top-right corner when editing
                                        if (viewModel.isEditMode) {
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.toggleToolVisibility(item.id)
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .padding(2.dp)
                                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Hide tool",
                                                    tint = MaterialTheme.colorScheme.onError,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Hidden Section
                    if (hiddenItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = Translator.translate("hidden", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = Translator.translate("hidden_desc", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val hiddenRows = hiddenItems.chunked(3)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            hiddenRows.forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { item ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(vertical = 5.dp, horizontal = 4.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                                                ) {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = Translator.translate(item.labelKey, lang),
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                                Text(
                                                    text = Translator.translate(item.labelKey, lang),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }

                                            // Plus (+) icon in the top-right corner to restore
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.toggleToolVisibility(item.id)
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .padding(2.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Restore tool",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (rowItems.size < 3) {
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = {
                            val titleStr = when (currentTool) {
                                is ActiveTool.Converter -> Translator.translate(
                                    when (currentTool.category) {
                                        ConverterCategory.CURRENCY -> "currency"
                                        ConverterCategory.LENGTH -> "length"
                                        ConverterCategory.TEMPERATURE -> "temperature"
                                        ConverterCategory.WEIGHT -> "weight"
                                        ConverterCategory.AREA -> "area"
                                        ConverterCategory.VOLUME -> "volume"
                                        ConverterCategory.DATA_STORAGE -> "data_storage"
                                        ConverterCategory.NUMBER_SYSTEMS -> "number_systems"
                                        ConverterCategory.TIME -> "time"
                                        ConverterCategory.SPEED -> "speed"
                                        ConverterCategory.PRESSURE -> "pressure"
                                        ConverterCategory.ENERGY -> "energy"
                                        ConverterCategory.FORCE -> "force"
                                        ConverterCategory.FREQUENCY -> "frequency"
                                        ConverterCategory.DENSITY -> "density"
                                        ConverterCategory.ANGLE -> "angle"
                                    }, lang
                                )
                                is ActiveTool.Bmi -> Translator.translate("bmi", lang)
                                is ActiveTool.DateCalc -> Translator.translate("date_calc", lang)
                                else -> ""
                            }
                            Text(text = titleStr, fontWeight = FontWeight.Bold)
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.activeTool = ActiveTool.Menu
                                },
                                modifier = Modifier.testTag("sub_tool_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentTool) {
                            is ActiveTool.Converter -> {
                                ConverterScreen(viewModel = viewModel)
                            }
                            is ActiveTool.Bmi -> {
                                BmiScreen(viewModel = viewModel)
                            }
                            is ActiveTool.DateCalc -> {
                                DateCalcScreen(viewModel = viewModel)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}
