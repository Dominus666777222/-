package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.CalculatorDatabase
import com.example.data.repository.HistoryRepository
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.GraphingScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ToolsHubScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ActiveTool
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.ui.viewmodel.CalculatorViewModelFactory
import com.example.util.Translator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge
        enableEdgeToEdge()

        // Room Database Setup
        val database = CalculatorDatabase.getDatabase(this)
        val repository = HistoryRepository(database.historyDao())
        
        // Instantiate ViewModel
        val factory = CalculatorViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[CalculatorViewModel::class.java]

        setContent {
            MyApplicationTheme(themeMode = viewModel.appTheme) {
                val lang = viewModel.appLanguage
                val currentScreen = viewModel.currentScreen

                // Global Back Handler to intercept back gesture
                val backEnabled = currentScreen != AppScreen.Calculator || 
                                  (currentScreen == AppScreen.Tools && viewModel.activeTool != ActiveTool.Menu)
                BackHandler(enabled = backEnabled) {
                    if (currentScreen == AppScreen.Tools && viewModel.activeTool != ActiveTool.Menu) {
                        viewModel.activeTool = ActiveTool.Menu
                    } else {
                        viewModel.currentScreen = AppScreen.Calculator
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val screenOrder = listOf(
                                AppScreen.Calculator,
                                AppScreen.Tools,
                                AppScreen.Graphing,
                                AppScreen.Settings,
                                AppScreen.History
                            )
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    val initialIdx = screenOrder.indexOf(initialState).coerceAtLeast(0)
                                    val targetIdx = screenOrder.indexOf(targetState).coerceAtLeast(0)
                                    if (targetIdx > initialIdx) {
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                        ) { width -> (width * 0.12f).toInt() } + fadeIn(
                                            animationSpec = tween(durationMillis = 280)
                                        )) togetherWith (slideOutHorizontally(
                                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                        ) { width -> (-width * 0.12f).toInt() } + fadeOut(
                                            animationSpec = tween(durationMillis = 140)
                                        ))
                                    } else {
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                        ) { width -> (-width * 0.12f).toInt() } + fadeIn(
                                            animationSpec = tween(durationMillis = 280)
                                        )) togetherWith (slideOutHorizontally(
                                            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                                        ) { width -> (width * 0.12f).toInt() } + fadeOut(
                                            animationSpec = tween(durationMillis = 140)
                                        ))
                                    }
                                },
                                label = "screen_transition",
                                modifier = Modifier.fillMaxSize()
                            ) { screen ->
                                when (screen) {
                                    AppScreen.Calculator -> CalculatorScreen(viewModel = viewModel)
                                    AppScreen.Graphing -> GraphingScreen(viewModel = viewModel)
                                    AppScreen.Tools -> ToolsHubScreen(viewModel = viewModel)
                                    AppScreen.Settings -> SettingsScreen(viewModel = viewModel)
                                    AppScreen.History -> HistoryScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.horizontalFadingEdges(
    scrollState: ScrollState,
    length: Dp = 24.dp
) = this.graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
}.drawWithContent {
    drawContent()
    val size = this.size
    val lengthPx = length.toPx()
    
    // Left fade
    if (scrollState.value > 0) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startX = 0f,
                endX = lengthPx
            ),
            blendMode = BlendMode.DstIn
        )
    }
    
    // Right fade
    if (scrollState.value < scrollState.maxValue) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent),
                startX = size.width - lengthPx,
                endX = size.width
            ),
            blendMode = BlendMode.DstIn
        )
    }
}

@Composable
fun MainTopTabBar(
    viewModel: CalculatorViewModel,
    lang: String
) {
    val haptic = LocalHapticFeedback.current
    var menuExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scrollable Tab bar
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
                .horizontalFadingEdges(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabPill(
                selected = viewModel.currentScreen == AppScreen.Calculator,
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.currentScreen = AppScreen.Calculator 
                },
                label = Translator.translate("calculator", lang),
                icon = Icons.Default.Calculate,
                testTag = "tab_calculator"
            )

            TabPill(
                selected = viewModel.currentScreen == AppScreen.Tools,
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.currentScreen = AppScreen.Tools 
                },
                label = Translator.translate("tools", lang),
                icon = Icons.Default.GridView,
                testTag = "tab_tools"
            )

            TabPill(
                selected = viewModel.currentScreen == AppScreen.Graphing,
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.currentScreen = AppScreen.Graphing 
                },
                label = Translator.translate("graphing", lang),
                icon = Icons.Default.ShowChart,
                testTag = "tab_graphing"
            )
        }

        // 3 Dots Menu Button
        Box {
            IconButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuExpanded = true 
                },
                modifier = Modifier.testTag("three_dots_menu_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu"
                )
            }
            
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(Translator.translate("settings", lang)) },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuExpanded = false
                        viewModel.currentScreen = AppScreen.Settings
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    modifier = Modifier.testTag("menu_settings")
                )
                DropdownMenuItem(
                    text = { Text(Translator.translate("history", lang)) },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuExpanded = false
                        viewModel.currentScreen = AppScreen.History
                    },
                    leadingIcon = {
                        Icon(Icons.Default.History, contentDescription = null)
                    },
                    modifier = Modifier.testTag("menu_history")
                )
            }
        }
    }
}

@Composable
fun TabPill(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    testTag: String
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
    
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            maxLines = 1
        )
    }
}
