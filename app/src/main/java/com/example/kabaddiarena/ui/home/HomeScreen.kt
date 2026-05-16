package com.example.kabaddiarena.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kabaddiarena.data.local.entity.MatchEntity
import com.example.kabaddiarena.data.local.entity.PlayerStats
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogger: (Long) -> Unit,
    onNavigateToSummary: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val matches by viewModel.allMatches.collectAsState()
    val topPerformers by viewModel.topPerformers.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var homeTeamName by remember { mutableStateOf("") }
    var opponentName by remember { mutableStateOf("") }
    var matchToDelete by remember { mutableStateOf<MatchEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredMatches = matches.filter {
        it.homeTeam.contains(searchQuery, ignoreCase = true) ||
                it.opponentTeam.contains(searchQuery, ignoreCase = true)
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Match")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(gradient, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    Text(
                        text = "Kabaddi Arena",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "Coach Analysis Dashboard",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White.copy(alpha = 0.8f))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search matches...", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Top Performers Leaderboard
            if (topPerformers.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Top Performers (Overall)",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(topPerformers) { player ->
                            LeaderboardCard(player)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recent Matches",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )

            if (filteredMatches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Text(
                            if (searchQuery.isEmpty()) "No matches yet. Start your first analysis!"
                            else "No matches found for \"$searchQuery\"",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredMatches) { match ->
                        MatchItem(
                            match = match,
                            onClick = {
                                if (match.isFinished) onNavigateToSummary(match.id)
                                else onNavigateToLogger(match.id)
                            },
                            onLongClick = { matchToDelete = match }
                        )
                    }
                }
            }
        }

        if (matchToDelete != null) {
            AlertDialog(
                onDismissRequest = { matchToDelete = null },
                title = { Text("Delete Session?") },
                text = { Text("This will permanently remove the data for this match.") },
                confirmButton = {
                    Button(
                        onClick = {
                            matchToDelete?.let { viewModel.deleteMatch(it.id) }
                            matchToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { matchToDelete = null }) { Text("Cancel") }
                }
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Analysis Session") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = homeTeamName,
                            onValueChange = { homeTeamName = it },
                            label = { Text("Your Team Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = opponentName,
                            onValueChange = { opponentName = it },
                            label = { Text("Opponent Team Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text("Squad Management", style = MaterialTheme.typography.titleSmall)
                        Text("You can add your initial playing squad and manage substitutes inside the match logger using the (+) button.",
                            style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (homeTeamName.isNotBlank() && opponentName.isNotBlank()) {
                                viewModel.createNewMatch(homeTeamName, opponentName) { id ->
                                    onNavigateToLogger(id)
                                }
                                showDialog = false
                            }
                        }
                    ) { Text("Start Session") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun LeaderboardCard(player: PlayerStats) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(48.dp).align(Alignment.BottomEnd).offset(x = 12.dp, y = 12.dp),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "#${player.jerseyNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${player.totalPoints} Pts",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun MatchItem(match: MatchEntity, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.homeTeam} vs ${match.opponentTeam}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(match.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            val statusColor = if (match.isFinished) Color(0xFF4CAF50) else Color(0xFFFF9800)
            val statusText = if (match.isFinished) "Summary" else "Live"
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
