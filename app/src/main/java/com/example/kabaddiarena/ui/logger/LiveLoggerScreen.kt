package com.example.kabaddiarena.ui.logger

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kabaddiarena.ui.components.KabaddiCourt

@Composable
fun LiveLoggerScreen(
    viewModel: LiveLoggerViewModel,
    onFinishMatch: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var selectedX by remember { mutableStateOf<Float?>(null) }
    var selectedY by remember { mutableStateOf<Float?>(null) }
    var showPlayerDialog by remember { mutableStateOf(false) }
    var showSquadDialog by remember { mutableStateOf(false) }
    var showFinishConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Selection Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Squad:", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(8.dp))
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isFinished) {
                    item {
                        IconButton(
                            onClick = { showSquadDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Manage Squad")
                        }
                    }
                }
                items(state.squadPlayers) { player ->
                    val isSelected = state.selectedPlayerId == player.id
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = !state.isFinished) { viewModel.selectPlayer(if (isSelected) null else player.id) }
                            .border(
                                width = 2.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            ),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = player.jerseyNumber,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Raid Points: ${state.totalRaidPoints}", style = MaterialTheme.typography.labelLarge)
                    Text(text = "Tackle Points: ${state.totalTacklePoints}", style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    text = "${"%.1f".format(state.raidSuccessRate)}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Court Heatmap
        val courtEvents = remember(state.events, state.selectedPlayerId) {
            if (state.selectedPlayerId == null) state.events
            else state.events.filter { it.playerId == state.selectedPlayerId }
        }
        
        KabaddiCourt(
            events = courtEvents,
            onCoordSelected = { x, y ->
                selectedX = x
                selectedY = y
            },
            modifier = Modifier.weight(1f),
            homeTeam = state.homeTeam,
            opponentTeam = state.opponentTeam
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Logging Controls
        Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            if (selectedX != null && selectedY != null && !state.isFinished) {
                val isOpponentCourt = selectedY!! < 0.5f
                val bonusPoint = if (selectedY!! < 0.25f) 1 else 0
                val bonusSuffix = if (bonusPoint > 0) " (+B)" else ""

                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOpponentCourt) Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isOpponentCourt) "RAIDING" else "DEFENDING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isOpponentCourt) {
                                // Raider options (Our team is attacking)
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { 
                                            viewModel.logRaid("SUCCESS", 1 + bonusPoint, selectedX, selectedY)
                                            selectedX = null; selectedY = null
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) { Text("Touch$bonusSuffix", style = MaterialTheme.typography.labelSmall) }
                                    
                                    Button(
                                        onClick = { 
                                            viewModel.logRaid(if (bonusPoint > 0) "BONUS" else "EMPTY", bonusPoint, selectedX, selectedY)
                                            selectedX = null; selectedY = null
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        contentPadding = PaddingValues(0.dp)
                                    ) { Text(if (bonusPoint > 0) "Bonus" else "Empty", style = MaterialTheme.typography.labelSmall) }
                                }
                                
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { 
                                            viewModel.logRaid("FAILURE", bonusPoint, selectedX, selectedY)
                                            selectedX = null; selectedY = null
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(0.dp)
                                    ) { Text("Tackled$bonusSuffix", style = MaterialTheme.typography.labelSmall) }
                                    
                                    Button(
                                        onClick = { 
                                            viewModel.logRaid("FAILURE", bonusPoint, selectedX, selectedY)
                                            selectedX = null; selectedY = null
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                        contentPadding = PaddingValues(0.dp)
                                    ) { Text("S-Tackle$bonusSuffix", style = MaterialTheme.typography.labelSmall) }
                                }
                            } else {
                                // Defender options (Our team is defending)
                                Button(
                                    onClick = { 
                                        viewModel.logTackle("SUCCESS", 1, selectedX, selectedY)
                                        selectedX = null; selectedY = null
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text("Tackle", style = MaterialTheme.typography.labelSmall) }
                                
                                Button(
                                    onClick = { 
                                        viewModel.logTackle("SUPER_TACKLE", 2, selectedX, selectedY)
                                        selectedX = null; selectedY = null
                                    },
                                    modifier = Modifier.weight(1.2f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text("Super Tackle", style = MaterialTheme.typography.labelSmall) }
                                
                                Button(
                                    onClick = { 
                                        viewModel.logTackle("FAILURE", -1, selectedX, selectedY)
                                        selectedX = null; selectedY = null
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text("Failed", style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isFinished) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (state.isFinished) "Match Completed - View Summary for Details" 
                                   else "Tap on the court to log an event",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.isFinished) MaterialTheme.colorScheme.onSecondaryContainer 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewModel.undo() },
                modifier = Modifier.weight(1f),
                enabled = !state.isFinished
            ) { Text("Undo") }

            Button(
                onClick = { 
                    if (state.isFinished) onFinishMatch() 
                    else showFinishConfirmation = true 
                },
                modifier = Modifier.weight(1f),
                colors = if (state.isFinished) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) else ButtonDefaults.buttonColors()
            ) { Text(if (state.isFinished) "View Summary" else "Finish Match") }
        }
    }

    if (showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishConfirmation = false },
            title = { Text("Complete Match?") },
            text = { Text("Are you sure you want to finish this match? You won't be able to log any more events or undo actions.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.finishMatch {
                        showFinishConfirmation = false
                        onFinishMatch()
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    if (showPlayerDialog) {
        var name by remember { mutableStateOf("") }
        var jersey by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            title = { Text("Add New Player to DB") },
            text = {
                Column {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(value = jersey, onValueChange = { jersey = it }, label = { Text("Jersey #") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && jersey.isNotBlank()) {
                        viewModel.addQuickPlayer(name, jersey)
                        showPlayerDialog = false
                    }
                }) { Text("Add") }
            }
        )
    }

    if (showSquadDialog) {
        AlertDialog(
            onDismissRequest = { showSquadDialog = false },
            title = { Text("Select Playing Squad") },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active: ${state.squadPlayerIds.size}/7", style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { showPlayerDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(" New Player")
                        }
                    }
                    HorizontalDivider()
                    LazyColumn {
                        items(state.allPlayers) { player ->
                            val isInSquad = state.squadPlayerIds.contains(player.id)
                            ListItem(
                                headlineContent = { Text("${player.name} (#${player.jerseyNumber})") },
                                trailingContent = {
                                    Checkbox(
                                        checked = isInSquad,
                                        onCheckedChange = { viewModel.togglePlayerInSquad(player.id) }
                                    )
                                },
                                modifier = Modifier.clickable { viewModel.togglePlayerInSquad(player.id) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSquadDialog = false }) { Text("Done") }
            }
        )
    }
}
