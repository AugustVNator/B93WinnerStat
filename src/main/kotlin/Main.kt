import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

// Screens in the app
enum class Screen {
    WELCOME,
    TEAM_DASHBOARD
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
fun TeamDashboardScreen(team: TeamData, onBack: () -> Unit) {
    var players by remember { mutableStateOf(Database.getPlayersByTeam(team.id)) }
    var newPlayerName by remember { mutableStateOf("") }

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

                Column {
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

            // Add player form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = { Text("New Player Name") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (newPlayerName.isNotBlank()) {
                                Database.addPlayer(
                                    name = newPlayerName,
                                    points = 0,
                                    teamId = team.id
                                )
                                players = Database.getPlayersByTeam(team.id)
                                newPlayerName = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Players header
            Text(
                text = "Players (${players.size})",
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Players list
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
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${player.points} points",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.primary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            Database.updatePlayerPoints(player.id, player.points + 1)
                                            players = Database.getPlayersByTeam(team.id)
                                        }
                                    ) {
                                        Text("+1")
                                    }

                                    Button(
                                        onClick = {
                                            Database.updatePlayerPoints(player.id, player.points - 1)
                                            players = Database.getPlayersByTeam(team.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.secondary
                                        )
                                    ) {
                                        Text("-1")
                                    }

                                    IconButton(
                                        onClick = {
                                            Database.deletePlayer(player.id)
                                            players = Database.getPlayersByTeam(team.id)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colors.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}