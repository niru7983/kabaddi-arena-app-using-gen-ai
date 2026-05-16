package com.example.kabaddiarena.ui.summary

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kabaddiarena.ui.components.KabaddiCourt

@Composable
fun PointGraph(points: List<Int>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    if (points.isEmpty()) return
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxPoints = points.maxOrNull()?.coerceAtLeast(1) ?: 1
        val minPoints = points.minOrNull()?.coerceAtMost(0) ?: 0
        val range = (maxPoints - minPoints).coerceAtLeast(1).toFloat()
        
        val path = Path()
        points.forEachIndexed { index, p ->
            val x = (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * width
            val y = height - ((p.toFloat() - minPoints) / range) * height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Baseline (0)
        if (minPoints < 0) {
            val baselineY = height - ((0f - minPoints) / range) * height
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(0f, baselineY),
                end = androidx.compose.ui.geometry.Offset(width, baselineY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun PerformanceSummaryScreen(viewModel: PerformanceSummaryViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val selectedPerformance = if (state.selectedPlayerId == null) {
        state.teamPerformance
    } else {
        state.playerPerformances.find { it.player?.id == state.selectedPlayerId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Match Analytics",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Player/Team Selection
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = state.selectedPlayerId == null,
                    onClick = { viewModel.selectPlayer(null) },
                    label = { Text("Team Overview") }
                )
            }
            items(state.playerPerformances) { perf ->
                FilterChip(
                    selected = state.selectedPlayerId == perf.player?.id,
                    onClick = { viewModel.selectPlayer(perf.player?.id) },
                    label = { Text(perf.player?.name ?: "Unknown") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedPerformance != null) {
            // Social Media Ready Performance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedPerformance.player?.name ?: "KABADDI ARENA",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (selectedPerformance.player != null) "Player Performance Card" else "Team Analysis Report",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.2f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryStatItem("Points", selectedPerformance.totalPoints.toString())
                        SummaryStatItem("Success", "${selectedPerformance.raidSuccess.toInt()}%")
                        SummaryStatItem("Tackles", selectedPerformance.tacklePoints.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Point Graph
            Text(
                text = "Point Progression",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            PointGraph(
                points = selectedPerformance.pointsTimeline,
                modifier = Modifier.height(120.dp).fillMaxWidth().padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Video Link Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=kabaddi+best+raids"))
                    context.startActivity(intent)
                },
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Watch Best Raid (Simulated Video)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (state.selectedPlayerId == null) "Team Strategic Heatmap" else "Individual Heatmap",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
            )
            
            KabaddiCourt(
                events = selectedPerformance.events,
                onCoordSelected = { _, _ -> }, // View only
                modifier = Modifier.height(400.dp).fillMaxWidth(),
                homeTeam = state.homeTeam,
                opponentTeam = state.opponentTeam
            )
            
            Text(
                text = "Visualizing successful raids (green) and failures (red)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // AI Coaching Tip Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "GenAI Coach Insights",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"${selectedPerformance.coachingTip}\"",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { 
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out this Kabaddi performance! Total Points: ${selectedPerformance?.totalPoints}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Performance Card"))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text("Share Performance Card")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}
