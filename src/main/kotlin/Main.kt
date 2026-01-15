import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // Initialize database on startup
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
    var players by remember { mutableStateOf(Database.getAllPlayers()) }
    var newPlayerName by remember { mutableStateOf("") }
    var newPlayerPoints by remember { mutableStateOf("0") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Winner Points B93",
                style = MaterialTheme.typography.h4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add player form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Player", style = MaterialTheme.typography.h6)

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = { Text("Player Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPlayerPoints,
                        onValueChange = { newPlayerPoints = it },
                        label = { Text("Points") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newPlayerName.isNotBlank()) {
                                Database.addPlayer(
                                    newPlayerName,
                                    newPlayerPoints.toIntOrNull() ?: 0
                                )
                                players = Database.getAllPlayers()
                                newPlayerName = ""
                                newPlayerPoints = "0"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Player")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Players list
            Text("Players (${players.size})", style = MaterialTheme.typography.h6)

            Spacer(modifier = Modifier.height(8.dp))

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
                                    style = MaterialTheme.typography.body1
                                )
                                Text(
                                    text = "Points: ${player.points}",
                                    style = MaterialTheme.typography.body2
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        Database.updatePlayerPoints(player.id, player.points + 1)
                                        players = Database.getAllPlayers()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.primary
                                    )
                                ) {
                                    Text("+1")
                                }

                                Button(
                                    onClick = {
                                        Database.deletePlayer(player.id)
                                        players = Database.getAllPlayers()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.error
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}