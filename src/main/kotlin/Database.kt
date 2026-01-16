import kotlinx.serialization.Serializable

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

// Persistent JSON-based database
// Data is stored locally at: ~/.winnerpoints_b93_data.json
// To sync across machines, change dataFile to a cloud-synced folder like:
//   - Dropbox: File(System.getProperty("user.home"), "Dropbox/.winnerpoints_b93_data.json")
//   - iCloud: File(System.getProperty("user.home"), "Library/Mobile Documents/com~apple~CloudDocs/.winnerpoints_b93_data.json")
object Database {
    private val json = Json { prettyPrint = true }
    private val dataFile = File(System.getProperty("user.home"), ".winnerpoints_b93_data.json")

    private var data = DatabaseData()

    fun init() {
        load()
    }

    private fun load() {
        if (dataFile.exists()) {
            try {
                val content = dataFile.readText()
                data = json.decodeFromString<DatabaseData>(content)
            } catch (e: Exception) {
                println("Error loading data: ${e.message}")
                data = DatabaseData()
            }
        }
    }

    private fun save() {
        try {
            val content = json.encodeToString(data)
            dataFile.writeText(content)
        } catch (e: Exception) {
            println("Error saving data: ${e.message}")
        }
    }

    // ===== PLAYER OPERATIONS =====

    fun addPlayer(name: String, points: Int = 0, teamId: Int? = null) {
        val newId = (data.players.maxOfOrNull { it.id } ?: 0) + 1
        data = data.copy(players = data.players + PlayerData(newId, name, points, teamId))
        save()
    }

    fun getAllPlayers(): List<PlayerData> {
        return data.players
    }

    fun getPlayersByTeam(teamId: Int): List<PlayerData> {
        return data.players.filter { it.teamId == teamId }
    }

    fun getPlayersWithoutTeam(): List<PlayerData> {
        return data.players.filter { it.teamId == null }
    }

    fun updatePlayerPoints(id: Int, newPoints: Int) {
        data = data.copy(
            players = data.players.map {
                if (it.id == id) it.copy(points = newPoints) else it
            }
        )
        save()
    }

    fun assignPlayerToTeam(playerId: Int, teamId: Int?) {
        data = data.copy(
            players = data.players.map {
                if (it.id == playerId) it.copy(teamId = teamId) else it
            }
        )
        save()
    }

    fun deletePlayer(id: Int) {
        data = data.copy(players = data.players.filter { it.id != id })
        save()
    }

    fun updatePlayerStats(id: Int, newPoints: Int? = null, newTrainingsAttended: Int? = null) {
        data = data.copy(
            players = data.players.map {
                if (it.id == id) {
                    it.copy(
                        points = newPoints ?: it.points,
                        trainingsAttended = newTrainingsAttended ?: it.trainingsAttended
                    )
                } else it
            }
        )
        save()
    }

    fun incrementTrainingsAttended(id: Int) {
        data = data.copy(
            players = data.players.map {
                if (it.id == id) it.copy(trainingsAttended = it.trainingsAttended + 1) else it
            }
        )
        save()
    }

    fun recordTrainingSession(teamId: Int, attendedPlayerIds: Set<Int>, pointsAwarded: Map<Int, Int>, date: String = LocalDate.now().toString(), notes: String = "") {
        // Update player stats
        data = data.copy(
            players = data.players.map { player ->
                var updated = player
                if (player.id in attendedPlayerIds) {
                    updated = updated.copy(trainingsAttended = updated.trainingsAttended + 1)
                }
                if (player.id in pointsAwarded) {
                    updated = updated.copy(points = updated.points + (pointsAwarded[player.id] ?: 0))
                }
                updated
            }
        )

        // Create training record
        val newId = (data.trainings.maxOfOrNull { it.id } ?: 0) + 1
        val training = TrainingData(
            id = newId,
            date = date,
            teamId = teamId,
            attendedPlayerIds = attendedPlayerIds.toList(),
            pointsAwarded = pointsAwarded,
            notes = notes
        )
        data = data.copy(trainings = data.trainings + training)
        save()
    }

    // ===== TRAINING OPERATIONS =====

    fun getAllTrainings(): List<TrainingData> {
        return data.trainings.sortedByDescending { it.date }
    }

    fun getTrainingsByTeam(teamId: Int): List<TrainingData> {
        return data.trainings.filter { it.teamId == teamId }.sortedByDescending { it.date }
    }

    fun getTrainingById(id: Int): TrainingData? {
        return data.trainings.find { it.id == id }
    }

    fun deleteTraining(id: Int) {
        val training = data.trainings.find { it.id == id } ?: return

        // Undo the points and attendance from this training
        data = data.copy(
            players = data.players.map { player ->
                var updated = player
                // Remove attendance if player attended this training
                if (player.id in training.attendedPlayerIds) {
                    updated = updated.copy(trainingsAttended = maxOf(0, updated.trainingsAttended - 1))
                }
                // Remove points awarded in this training
                if (player.id in training.pointsAwarded) {
                    val pointsToRemove = training.pointsAwarded[player.id] ?: 0
                    updated = updated.copy(points = maxOf(0, updated.points - pointsToRemove))
                }
                updated
            },
            trainings = data.trainings.filter { it.id != id }
        )
        save()
    }

    // ===== TEAM OPERATIONS =====

    fun addTeam(name: String) {
        val newId = (data.teams.maxOfOrNull { it.id } ?: 0) + 1
        data = data.copy(teams = data.teams + TeamData(newId, name))
        save()
    }

    fun getAllTeams(): List<TeamData> {
        return data.teams
    }

    fun getTeamWithPlayers(teamId: Int): TeamWithPlayers? {
        val team = data.teams.find { it.id == teamId } ?: return null
        val teamPlayers = getPlayersByTeam(teamId)
        return TeamWithPlayers(team, teamPlayers)
    }

    fun deleteTeam(id: Int) {
        // Remove team assignment from players first
        data = data.copy(
            players = data.players.map {
                if (it.teamId == id) it.copy(teamId = null) else it
            },
            teams = data.teams.filter { it.id != id }
        )
        save()
    }
}


// ===== DATA CLASSES =====

@Serializable
data class DatabaseData(
    val players: List<PlayerData> = emptyList(),
    val teams: List<TeamData> = emptyList(),
    val trainings: List<TrainingData> = emptyList()
)

@Serializable
data class PlayerData(
    val id: Int,
    val name: String,
    val points: Int,
    val teamId: Int? = null,
    val trainingsAttended: Int = 0
)

@Serializable
data class TeamData(
    val id: Int,
    val name: String
)

@Serializable
data class TrainingData(
    val id: Int,
    val date: String,                           // ISO format: "2026-01-15"
    val teamId: Int,                            // Which team this training was for
    val attendedPlayerIds: List<Int>,           // Players who showed up
    val pointsAwarded: Map<Int, Int> = emptyMap(), // Player ID -> points awarded
    val notes: String = ""                      // Optional notes about the training
)

data class TeamWithPlayers(
    val team: TeamData,
    val players: List<PlayerData>
) {
    val totalPoints: Int get() = players.sumOf { it.points }
}

