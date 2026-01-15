import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

// Screens in the app
enum class Screen {
    WELCOME,
    TEAM_DASHBOARD,
    PLAYER_STATS,
    TRAINING_SESSION
}

fun main() = application {
    Database.init()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Winner Points B93"
    ) {
        MaterialTheme {
            App()
        }
    }
}

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.WELCOME) }
    var selectedTeam by remember { mutableStateOf<TeamData?>(null) }
    var selectedPlayer by remember { mutableStateOf<PlayerData?>(null) }

    when (currentScreen) {
        Screen.WELCOME -> WelcomeScreen(
            onTeamSelected = { team ->
                selectedTeam = team
                currentScreen = Screen.TEAM_DASHBOARD
            }
        )
        Screen.TEAM_DASHBOARD -> TeamDashboardScreen(
            team = selectedTeam!!,
            onBack = {
                selectedTeam = null
                currentScreen = Screen.WELCOME
            },
            onPlayerSelected = { player ->
                selectedPlayer = player
                currentScreen = Screen.PLAYER_STATS
            },
            onTrainingSession = {
                currentScreen = Screen.TRAINING_SESSION
            }
        )
        Screen.PLAYER_STATS -> PlayerStatsScreen(
            player = selectedPlayer!!,
            onBack = {
                selectedPlayer = null
                currentScreen = Screen.TEAM_DASHBOARD
            }
        )
        Screen.TRAINING_SESSION -> TrainingSessionScreen(
            team = selectedTeam!!,
            onBack = {
                currentScreen = Screen.TEAM_DASHBOARD
            }
        )
    }
}

@Composable
fun WelcomeScreen(onTeamSelected: (TeamData) -> Unit) {
    var teams by remember { mutableStateOf(Database.getAllTeams()) }
    var showAddTeamDialog by remember { mutableStateOf(false) }
    var newTeamName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo placeholder
            Text(
                text = "⚽",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f)
            )

            Text(
                text = "Winner Points B93",
                style = MaterialTheme.typography.h3,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Track your team's performance",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Select your team",
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Team list
            if (teams.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    elevation = 2.dp
                ) {
                    Text(
                        text = "No teams yet. Add your first team!",
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teams) { team ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTeamSelected(team) },
                            elevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = team.name,
                                    style = MaterialTheme.typography.h6
                                )

                                IconButton(
                                    onClick = {
                                        Database.deleteTeam(team.id)
                                        teams = Database.getAllTeams()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete team",
                                        tint = MaterialTheme.colors.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add team button
            Button(
                onClick = { showAddTeamDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Team")
            }
        }
    }

    // Add team dialog
    if (showAddTeamDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTeamDialog = false
                newTeamName = ""
            },
            title = { Text("Add New Team") },
            text = {
                OutlinedTextField(
                    value = newTeamName,
                    onValueChange = { newTeamName = it },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTeamName.isNotBlank()) {
                            Database.addTeam(newTeamName)
                            teams = Database.getAllTeams()
                            newTeamName = ""
                            showAddTeamDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTeamDialog = false
                        newTeamName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TeamDashboardScreen(
    team: TeamData,
    onBack: () -> Unit,
    onPlayerSelected: (PlayerData) -> Unit,
    onTrainingSession: () -> Unit
) {
    var players by remember { mutableStateOf(Database.getPlayersByTeam(team.id).sortedBy { it.name.lowercase() }) }
    var newPlayerName by remember { mutableStateOf("") }
    var showAddPlayerDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Points: ${players.sumOf { it.points }}",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Training Session button (Award Points)
                Button(
                    onClick = onTrainingSession,
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = players.isNotEmpty()
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Training Session")
                }

                // Add Player button
                Button(
                    onClick = { showAddPlayerDialog = true },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Player")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Players header
            Text(
                text = "Players (${players.size})",
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Players list - alphabetically sorted, clickable
            if (players.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Text(
                        text = "No players yet. Add your first player!",
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(players) { player ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayerSelected(player) },
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Player avatar placeholder
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${player.points} points • ${player.trainingsAttended} trainings",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                // Arrow indicator
                                Text(
                                    text = "›",
                                    style = MaterialTheme.typography.h5,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Player dialog
    if (showAddPlayerDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddPlayerDialog = false
                newPlayerName = ""
            },
            title = { Text("Add New Player") },
            text = {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Player Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlayerName.isNotBlank()) {
                            Database.addPlayer(
                                name = newPlayerName,
                                points = 0,
                                teamId = team.id
                            )
                            players = Database.getPlayersByTeam(team.id).sortedBy { it.name.lowercase() }
                            newPlayerName = ""
                            showAddPlayerDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddPlayerDialog = false
                        newPlayerName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PlayerStatsScreen(player: PlayerData, onBack: () -> Unit) {
    // Get fresh player data
    val currentPlayer = Database.getAllPlayers().find { it.id == player.id } ?: player

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Player Stats",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Player avatar and name
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentPlayer.name,
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Points card
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = 4.dp,
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${currentPlayer.points}",
                            style = MaterialTheme.typography.h3,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "Points",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Trainings card
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = 4.dp,
                    backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${currentPlayer.trainingsAttended}",
                            style = MaterialTheme.typography.h3,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Trainings",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Average points per training
            if (currentPlayer.trainingsAttended > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Average Points per Training",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "%.2f".format(currentPlayer.points.toFloat() / currentPlayer.trainingsAttended),
                            style = MaterialTheme.typography.h4,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Delete player button
            Button(
                onClick = {
                    Database.deletePlayer(currentPlayer.id)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Player")
            }
        }
    }
}

@Composable
fun TrainingSessionScreen(team: TeamData, onBack: () -> Unit) {
    var players by remember { mutableStateOf(Database.getPlayersByTeam(team.id).sortedBy { it.name.lowercase() }) }
    var attendedPlayers by remember { mutableStateOf(setOf<Int>()) }
    var pointsAwarded by remember { mutableStateOf(mapOf<Int, Int>()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Training Session",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mark attendance and award points",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("= Present at training")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colors.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("= Points to award")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Players list
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players) { player ->
                    val isAttended = player.id in attendedPlayers
                    val playerPoints = pointsAwarded[player.id] ?: 0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp,
                        backgroundColor = if (isAttended)
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            MaterialTheme.colors.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Attendance checkbox
                            Checkbox(
                                checked = isAttended,
                                onCheckedChange = { checked ->
                                    attendedPlayers = if (checked) {
                                        attendedPlayers + player.id
                                    } else {
                                        attendedPlayers - player.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF4CAF50)
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Player name
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            // Points controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (playerPoints > 0) {
                                            pointsAwarded = pointsAwarded + (player.id to playerPoints - 1)
                                        }
                                    },
                                    enabled = playerPoints > 0
                                ) {
                                    Text(
                                        text = "−",
                                        style = MaterialTheme.typography.h5,
                                        color = if (playerPoints > 0) MaterialTheme.colors.primary else Color.Gray
                                    )
                                }

                                Text(
                                    text = "$playerPoints",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier.width(32.dp),
                                    textAlign = TextAlign.Center,
                                    color = if (playerPoints > 0) MaterialTheme.colors.primary else Color.Gray
                                )

                                IconButton(
                                    onClick = {
                                        pointsAwarded = pointsAwarded + (player.id to playerPoints + 1)
                                    }
                                ) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.h5,
                                        color = MaterialTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${attendedPlayers.size}",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Present",
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${players.size - attendedPlayers.size}",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.error
                        )
                        Text(
                            text = "Absent",
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${pointsAwarded.values.sum()}",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "Points",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    Database.recordTrainingSession(attendedPlayers, pointsAwarded)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = attendedPlayers.isNotEmpty() || pointsAwarded.values.any { it > 0 }
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Training Session")
            }
        }
    }
}