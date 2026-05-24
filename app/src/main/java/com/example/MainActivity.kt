package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Mantra
import com.example.ui.theme.*
import com.example.viewmodel.JaapViewModel
import com.example.viewmodel.JaapViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var jaapViewModel: JaapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                jaapViewModel = viewModel(
                    factory = JaapViewModelFactory(context.applicationContext as android.app.Application)
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainNavigationScreen(
                        viewModel = jaapViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (::jaapViewModel.isInitialized) {
            jaapViewModel.pauseSession()
            if (jaapViewModel.omSynthRunning.value) {
                jaapViewModel.toggleOmSynth()
            }
        }
    }
}

@Composable
fun MainNavigationScreen(
    viewModel: JaapViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("DHYANA", "MANTRAS", "SADHANABOARD")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekLinenBg)
    ) {
        // Spiritual Header with Devanagari Motif
        TitleHeader()

        // Tab Row Setup
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = SleekCharcoalBrown,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SleekSaffronOrange
                )
            },
            divider = {
                Spacer(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(SleekBorderBeige)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            letterSpacing = 1.8.sp,
                            fontSize = 13.sp,
                            color = if (selectedTab == index) SleekSaffronOrange else SleekCharcoalBrown.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.testTag("tab_$index")
                )
            }
        }

        // Display contents based on selected tab index
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (selectedTab) {
                0 -> DhyanaTab(viewModel = viewModel)
                1 -> MantrasTab(viewModel = viewModel)
                2 -> SadhanaHistoryTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TitleHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ॐ",
                fontSize = 28.sp,
                color = SleekSaffronOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = "JAAP SADHANA",
                color = SleekSaddleBrown,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.5.sp,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "ॐ",
                fontSize = 28.sp,
                color = SleekSaffronOrange,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Text(
            text = "SACRED BEADS MEDITATION SACRED SPHERE",
            color = SleekCharcoalBrown.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun DhyanaTab(viewModel: JaapViewModel) {
    val currentCount by viewModel.currentJaapCount.collectAsStateWithLifecycle()
    val completedMalas by viewModel.completedMalas.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val isSessionActive by viewModel.isSessionActive.collectAsStateWithLifecycle()
    val selectedMantra by viewModel.selectedMantra.collectAsStateWithLifecycle()
    val malaMaterial by viewModel.selectedMalaMaterial.collectAsStateWithLifecycle()
    val omSynthRunning by viewModel.omSynthRunning.collectAsStateWithLifecycle()
    val autoPlayMode by viewModel.autoPlayMode.collectAsStateWithLifecycle()
    val autoPlayIntervalMs by viewModel.autoPlayIntervalMs.collectAsStateWithLifecycle()
    val showSuccessDialog by viewModel.showSaveSuccessDialog.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Mantra Indicator Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, SleekBorderBeige),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_mantra_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ACTIVE SADHANA MANTA",
                        color = SleekTerracotta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedMantra?.name ?: "Om",
                        color = SleekCharcoalBrown,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Deity: ${selectedMantra?.deity ?: "Cosmic Absolute"}",
                        color = SleekSaffronOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${selectedMantra?.translation ?: ""}\"",
                        color = SleekCharcoalBrown.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }

        // Bead Necklace Canvas Slider Visual representation
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Drawing the beautiful moving beads
                BeadedMalaCanvas(
                    currentCount = currentCount,
                    malaMaterial = malaMaterial,
                    modifier = Modifier.fillMaxSize()
                )

                // Timer & Mala Counter Floating Overlays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer Display
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, SleekBorderBeige, RoundedCornerShape(8.dp))
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DHYANA TIME",
                            fontSize = 8.sp,
                            color = SleekTerracotta,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = formatDuration(elapsedSeconds),
                            fontSize = 14.sp,
                            color = SleekCharcoalBrown,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mala counts display (how many circles of 108)
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, SleekBorderBeige, RoundedCornerShape(8.dp))
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MALAS COMPLETED",
                            fontSize = 8.sp,
                            color = SleekSaddleBrown,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$completedMalas ($currentCount/108)",
                            fontSize = 14.sp,
                            color = SleekSaffronOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Magnificent Tap Board (ॐ Button)
        item {
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .testTag("bead_tap_target"),
                contentAlignment = Alignment.Center
            ) {
                // Outer decorative ring (Spiritual Altar Mandala)
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .drawBehind {
                            drawCircle(
                                color = SleekBorderBeige,
                                radius = size.minDimension / 2,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            // Draw rays/nodes along the outer circle to style it like a sun/lotus
                            val nodesCount = 12
                            for (j in 0 until nodesCount) {
                                val deg = Math.toRadians((j * (360.0 / nodesCount)))
                                val offX = (size.width / 2) + ((size.width / 2) * Math.cos(deg)).toFloat()
                                val offY = (size.height / 2) + ((size.height / 2) * Math.sin(deg)).toFloat()
                                drawCircle(
                                    color = SleekSaffronOrange.copy(alpha = 0.5f),
                                    radius = 4.dp.toPx(),
                                    center = Offset(offX, offY)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        SleekSoftCream
                                    )
                                )
                            )
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    listOf(SleekSaffronOrange, SleekSaddleBrown, SleekTerracotta, SleekSaffronOrange)
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                scope.launch {
                                    scaleAnim.animateTo(
                                        0.92f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                                    )
                                    scaleAnim.animateTo(
                                        1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                                    )
                                }
                                viewModel.incrementCount()
                            }
                            .offset(y = (0).dp)
                            .offset(x = (0).dp)
                    ) {
                        // High Resolution Sanskrit OM Centerpiece Text
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ॐ",
                                fontSize = 64.sp,
                                color = SleekSaddleBrown,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.offset(y = (-5).dp)
                            )
                            Text(
                                text = "TAP TO COUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTerracotta.copy(alpha = 0.8f),
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }

        // Count modifier adjustment tray (-1 bead, Reset, Save Session)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtract 1
                IconButton(
                    onClick = { viewModel.decrementCount() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White,
                        contentColor = SleekSaffronOrange
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.dp, SleekBorderBeige, CircleShape)
                        .testTag("decrement_bead_button")
                ) {
                    Text("-1", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SleekSaffronOrange)
                }

                // Reset Session Counts
                IconButton(
                    onClick = { viewModel.resetCount() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White,
                        contentColor = SleekCharcoalBrown
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.dp, SleekBorderBeige, CircleShape)
                        .testTag("reset_bead_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Current Counters",
                        tint = SleekCharcoalBrown.copy(alpha = 0.8f)
                    )
                }

                // Primary Save Sadhana Button
                Button(
                    onClick = { viewModel.saveCurrentSession() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekSaddleBrown,
                        contentColor = Color.White
                    ),
                    enabled = currentCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .width(180.dp)
                        .testTag("save_sadhana_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🙏", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(
                            text = "SAVE SADHANA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Mala material choice chips
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT MALA MATERIAL",
                    fontSize = 11.sp,
                    color = SleekCharcoalBrown.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val materials = listOf("Rudraksha", "Sandalwood", "Tulsi", "Kamal Gatta")
                    materials.forEach { material ->
                        val isSelected = malaMaterial == material
                        val chipBg = if (isSelected) SleekSaffronOrange else Color.White
                        val chipBorder = if (isSelected) SleekSaffronOrange else SleekBorderBeige
                        val chipText = if (isSelected) Color.White else SleekCharcoalBrown

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectMalaMaterial(material) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = material.replace(" ", "\n"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = chipText,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Sound Engine and Auto-jaap controls
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SleekSoftCream
                ),
                border = BorderStroke(1.dp, SleekBorderBeige),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("control_tray_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MEDITATION ASSIST ENGINE",
                        fontSize = 11.sp,
                        color = SleekTerracotta,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // OM Sound Generator Toggle
                        Button(
                            onClick = { viewModel.toggleOmSynth() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (omSynthRunning) SleekSaffronOrange else Color.White,
                                contentColor = if (omSynthRunning) Color.White else SleekCharcoalBrown
                            ),
                            border = BorderStroke(1.dp, if (omSynthRunning) SleekSaffronOrange else SleekBorderBeige),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("om_synth_toggle_button")
                        ) {
                            Text(
                                text = if (omSynthRunning) "🔊" else "🔇",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (omSynthRunning) "OM: ON" else "OM: OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Auto Play Hands-free counting Toggle
                        Button(
                            onClick = { viewModel.toggleAutoPlay() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (autoPlayMode) SleekSaffronOrange else Color.White,
                                contentColor = if (autoPlayMode) Color.White else SleekCharcoalBrown
                            ),
                            border = BorderStroke(1.dp, if (autoPlayMode) SleekSaffronOrange else SleekBorderBeige),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("auto_play_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Toggle automatic increment mode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (autoPlayMode) "AUTO-JAAP: YES" else "AUTO-JAAP: NO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Adjustable Sliderr for Auto play interval
                    AnimatedVisibility(
                        visible = autoPlayMode,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val intervalSecs = autoPlayIntervalMs / 1000f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Auto-Sadhana speed",
                                    fontSize = 11.sp,
                                    color = SleekCharcoalBrown.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", intervalSecs)} seconds",
                                    fontSize = 11.sp,
                                    color = SleekSaddleBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = intervalSecs,
                                onValueChange = { speedSec ->
                                    viewModel.setAutoPlayInterval((speedSec * 1000).toLong())
                                },
                                valueRange = 2f..10f,
                                colors = SliderDefaults.colors(
                                    thumbColor = SleekSaffronOrange,
                                    activeTrackColor = SleekSaffronOrange,
                                    inactiveTrackColor = SleekBorderBeige
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("auto_speed_slider")
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Saved Success Celebration Alert Card
    if (showSuccessDialog) {
        Dialog(onDismissRequest = { viewModel.closeSuccessDialog() }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SleekSoftCream,
                border = BorderStroke(2.dp, SleekSaffronOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("sadhana_saved_success_modal")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ॐ",
                        fontSize = 54.sp,
                        color = SleekSaffronOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SADHANA EMBODIED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekSaddleBrown,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your chanted devotion has been recorded in the cosmic akashic records.",
                        color = SleekCharcoalBrown,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    DividerSection()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Total Chants Completed:",
                        color = SleekCharcoalBrown.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$currentCount Japas",
                        color = SleekSaffronOrange,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "($completedMalas full malas completed)",
                        color = SleekTerracotta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.closeSuccessDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekSaddleBrown,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "PRANAM (ACCEPT)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DividerSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SleekBorderBeige)
        )
        Text(
            text = "❖",
            color = SleekSaffronOrange,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(SleekBorderBeige)
        )
    }
}

@Composable
fun MantrasTab(viewModel: JaapViewModel) {
    val mantrasList by viewModel.mantras.collectAsStateWithLifecycle()
    val selectedMantra by viewModel.selectedMantra.collectAsStateWithLifecycle()
    var displayAddDialog by remember { mutableStateOf(false) }

    var customName by remember { mutableStateOf("") }
    var customDeity by remember { mutableStateOf("") }
    var customTranslation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MANTRAS DICTIONARY",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                color = SleekCharcoalBrown.copy(alpha = 0.6f)
            )

            // Compose custom mantra button
            Button(
                onClick = { displayAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = SleekSaffronOrange
                ),
                border = BorderStroke(1.dp, SleekBorderBeige),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("add_custom_mantra_dialog_trigger")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add custom chant", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ADD CUSTOM", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mantrasList) { mantra ->
                val isSelected = selectedMantra?.id == mantra.id
                val borderStroke = if (isSelected) {
                    BorderStroke(2.dp, SleekSaffronOrange)
                } else {
                    BorderStroke(1.dp, SleekBorderBeige)
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SleekSoftCream else Color.White
                    ),
                    border = borderStroke,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectMantra(mantra) }
                        .testTag("mantra_item_${mantra.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = mantra.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SleekSaddleBrown else SleekCharcoalBrown
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Deity Icon/Label
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) SleekSaffronOrange.copy(0.15f) else Color.White
                                        )
                                        .border(
                                            width = if (isSelected) 0.dp else 1.dp,
                                            color = if (isSelected) Color.Transparent else SleekBorderBeige,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = mantra.deity,
                                        fontSize = 9.sp,
                                        color = if (isSelected) SleekSaffronOrange else SleekCharcoalBrown.copy(0.6f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!mantra.isPreset) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteMantra(mantra) },
                                        modifier = Modifier.size(24.dp).testTag("delete_custom_mantra_${mantra.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete custom mantra",
                                            tint = SindoorRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = mantra.translation,
                            fontSize = 11.sp,
                            color = SleekCharcoalBrown.copy(alpha = 0.7f),
                            lineHeight = 15.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        if (mantra.countSelected > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Count selected",
                                    tint = SleekTerracotta,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Selected for Sadhana: ${mantra.countSelected} times",
                                    fontSize = 9.sp,
                                    color = SleekTerracotta.copy(0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Add Custom Mantra Dialog Setup
    if (displayAddDialog) {
        Dialog(onDismissRequest = { displayAddDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(2.dp, SleekBorderBeige),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("custom_mantra_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "ADD SACRED MANTA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekSaddleBrown,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Mantra Chanting Text") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekSaffronOrange,
                            unfocusedBorderColor = SleekBorderBeige,
                            focusedLabelColor = SleekSaffronOrange,
                            unfocusedLabelColor = SleekCharcoalBrown.copy(alpha = 0.6f),
                            focusedTextColor = SleekCharcoalBrown,
                            unfocusedTextColor = SleekCharcoalBrown
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_mantra_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customDeity,
                        onValueChange = { customDeity = it },
                        label = { Text("Spiritual Deity / Target Focus") },
                        placeholder = { Text("e.g. Lord Shiva, Inner Peace") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekSaffronOrange,
                            unfocusedBorderColor = SleekBorderBeige,
                            focusedLabelColor = SleekSaffronOrange,
                            unfocusedLabelColor = SleekCharcoalBrown.copy(alpha = 0.6f),
                            focusedTextColor = SleekCharcoalBrown,
                            unfocusedTextColor = SleekCharcoalBrown
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_mantra_deity_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customTranslation,
                        onValueChange = { customTranslation = it },
                        label = { Text("Devotional Translation / Intention") },
                        placeholder = { Text("Describe the auspicious meaning of this chant") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekSaffronOrange,
                            unfocusedBorderColor = SleekBorderBeige,
                            focusedLabelColor = SleekSaffronOrange,
                            unfocusedLabelColor = SleekCharcoalBrown.copy(alpha = 0.6f),
                            focusedTextColor = SleekCharcoalBrown,
                            unfocusedTextColor = SleekCharcoalBrown
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("add_mantra_translation_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { displayAddDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = SleekCharcoalBrown.copy(0.7f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CANCEL")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (customName.isNotBlank()) {
                                    viewModel.addCustomMantra(customName, customDeity, customTranslation)
                                    // Clear fields
                                    customName = ""
                                    customDeity = ""
                                    customTranslation = ""
                                    displayAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekSaddleBrown,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_custom_mantra_button")
                        ) {
                            Text("SAVE")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SadhanaHistoryTab(viewModel: JaapViewModel) {
    val totalJaapCount by viewModel.totalJaaps.collectAsStateWithLifecycle()
    val totalSessions by viewModel.totalSessions.collectAsStateWithLifecycle()
    val totalSeconds by viewModel.totalDurationSeconds.collectAsStateWithLifecycle()
    val historyLogs by viewModel.sessions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Analytics Summary Dashboard Header
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "AKASHIC SADHANABOARD",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                color = SleekCharcoalBrown.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Grid Dashboard Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Main stats highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "TOTAL CHANTS",
                        value = totalJaapCount.toString(),
                        subtitle = "${totalJaapCount / 108} Full Malas",
                        color = SleekSaffronOrange,
                        modifier = Modifier.weight(1f).testTag("stat_total_chants")
                    )
                    StatsCard(
                        title = "SESSIONS DONE",
                        value = totalSessions.toString(),
                        subtitle = "Spiritual routines",
                        color = SleekTerracotta,
                        modifier = Modifier.weight(1f).testTag("stat_total_sessions")
                    )
                }

                StatsCard(
                    title = "MEDITATION MINS",
                    value = formatHoursMinutes(totalSeconds),
                    subtitle = "Vedic focused mindfulness time",
                    color = SleekSaddleBrown,
                    modifier = Modifier.fillMaxWidth().testTag("stat_total_duration")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            DividerSection()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MEDITATION RECORD CHRONICLE",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                color = SleekCharcoalBrown.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        if (historyLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty History",
                            tint = SleekCharcoalBrown.copy(0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recorded sessions. Begin your Japa chants above!",
                            color = SleekCharcoalBrown.copy(0.4f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(historyLogs) { session ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SleekBorderBeige),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("session_card_${session.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.mantraName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekSaddleBrown,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SleekSaffronOrange.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = session.malaType,
                                        fontSize = 8.sp,
                                        color = SleekSaffronOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Deity: ${session.deity}  •  ${formatDuration(session.durationSeconds)}",
                                fontSize = 11.sp,
                                color = SleekCharcoalBrown.copy(0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(
                                    Date(session.timestamp)
                                ),
                                fontSize = 10.sp,
                                color = SleekCharcoalBrown.copy(0.4f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "${session.count} chants",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SleekSaffronOrange
                                )
                                Text(
                                    text = "${session.completedMalas} Malas",
                                    fontSize = 10.sp,
                                    color = SleekCharcoalBrown.copy(alpha = 0.5f)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteSession(session.id) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("delete_session_btn_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove meditation entry",
                                    tint = SindoorRed.copy(0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SleekBorderBeige),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SleekCharcoalBrown.copy(0.5f),
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = SleekCharcoalBrown.copy(0.6f)
            )
        }
    }
}

// Helper time formatter methods

fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}

fun formatHoursMinutes(totalSeconds: Long): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return if (mins > 0) {
        String.format(Locale.getDefault(), "%d min %02d sec", mins, secs)
    } else {
        String.format(Locale.getDefault(), "%02d sec", secs)
    }
}

// Elegant BeadedMalaCanvas that rotates and slides beads under Compose physics
@Composable
fun BeadedMalaCanvas(
    currentCount: Int,
    malaMaterial: String,
    modifier: Modifier = Modifier
) {
    // Canvas spring physics motion smooth sliding!
    val animatedCount by animateFloatAsState(
        targetValue = currentCount.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "BeadRotationSlider"
    )

    // Wood seed palette selection
    val beadColor = when (malaMaterial) {
        "Rudraksha" -> RudrakshaColor
        "Sandalwood" -> SandalwoodColor
        "Tulsi" -> TulsiColor
        "Kamal Gatta" -> KamalGattaColor
        else -> RudrakshaColor
    }

    Canvas(
        modifier = modifier
            .testTag("beaded_canvas")
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        // Push the circle center lower to draw a wide curved horizontal arch on Screen
        val centerY = height * 1.55f
        
        val radiusX = width * 0.44f
        val radiusY = height * 1.35f

        // Draw a soft gold physical sacred string/thread looping under the beads
        val arcPath = Path().apply {
            for (degree in 30..150) {
                val rad = Math.toRadians((180 - degree).toDouble())
                val x = centerX + radiusX * Math.cos(rad).toFloat()
                val y = centerY - radiusY * Math.sin(rad).toFloat()
                if (degree == 30) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        drawPath(
            path = arcPath,
            color = MutedMarigold.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )

        val visibleBeadsCount = 13 // Symmetric odd counts
        val centerIndex = visibleBeadsCount / 2 // apex index

        for (i in 0 until visibleBeadsCount) {
            // Real-time slide offsets
            val offsetFromCenter = i - centerIndex - (animatedCount % 1.0f)
            
            // Map offsets to angles around the top of our wide arch
            // Math.PI / 2 is the exact apex top center (90.0 degrees)
            val angleRad = (Math.PI / 2.0) + (offsetFromCenter * 0.082) // angular spacing between beads
            
            val beadX = centerX + radiusX * Math.cos(angleRad).toFloat()
            val beadY = centerY - radiusY * Math.sin(angleRad).toFloat()

            // Is this bead sits at the top counting checkpoint?
            val isCenterBead = i == centerIndex
            val sizeMultiplier = if (isCenterBead) {
                1.4f - (animatedCount % 1.0f) * 0.2f
            } else if (i == centerIndex + 1) {
                1.2f + (animatedCount % 1.0f) * 0.2f
            } else {
                1.0f
            }

            val beadRadius = 14.5.dp.toPx() * sizeMultiplier

            // 1. Draw subtle 3D shadow cast
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = beadRadius,
                center = Offset(beadX + 2.5.dp.toPx(), beadY + 3.dp.toPx())
            )

            // 2. Draw auric spiritual lighting radiating from the counting bead
            if (isCenterBead) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AuraAmber.copy(0.6f), Color.Transparent),
                        center = Offset(beadX, beadY),
                        radius = beadRadius * 3f
                    ),
                    radius = beadRadius * 3f,
                    center = Offset(beadX, beadY)
                )
            }

            // 3. Render 3D Sphere gradient shaders
            val surfaceHighlight = when (malaMaterial) {
                "Rudraksha" -> Color(0xFF8A5344)
                "Sandalwood" -> Color.White
                "Tulsi" -> Color(0xFFA6715B)
                "Kamal Gatta" -> Color(0xFF636363)
                else -> Color.White
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(surfaceHighlight, beadColor, beadColor.copy(alpha = 0.85f)),
                    center = Offset(beadX - (beadRadius * 0.35f), beadY - (beadRadius * 0.35f)),
                    radius = beadRadius * 1.3f
                ),
                radius = beadRadius,
                center = Offset(beadX, beadY)
            )

            // 4. Bead carvings
            when (malaMaterial) {
                "Rudraksha" -> {
                    // Draw ancient sacred texture cracks
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = beadRadius * 0.85f,
                        center = Offset(beadX, beadY),
                        style = Stroke(width = 1.2f.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.2f),
                        radius = beadRadius * 0.55f,
                        center = Offset(beadX, beadY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                "Kamal Gatta" -> {
                    // Oval seeds texture details
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.45f),
                        radius = beadRadius * 0.75f,
                        center = Offset(beadX, beadY),
                        style = Stroke(width = 1.5f.dp.toPx())
                    )
                }
                "Sandalwood", "Tulsi" -> {
                    // Wood rings texture details
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.15f),
                        radius = beadRadius * 0.75f,
                        center = Offset(beadX, beadY),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // 5. Crown Halo ring outlining the center bead active position
            if (isCenterBead) {
                drawCircle(
                    color = TempleGold,
                    radius = beadRadius + 3.dp.toPx(),
                    center = Offset(beadX, beadY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
