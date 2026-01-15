// Simple in-memory database (no external dependencies needed)
object Database {
    private var nextPlayerId = 1
    private var nextTeamId = 1
    private val players = mutableListOf<PlayerData>()
    private val teams = mutableListOf<TeamData>()

    fun init() {
        // Nothing to initialize for in-memory storage
    }

    // ===== PLAYER OPERATIONS =====

    fun addPlayer(name: String, points: Int = 0, teamId: Int? = null) {
        players.add(PlayerData(nextPlayerId++, name, points, teamId))
    }

    fun getAllPlayers(): List<PlayerData> {
        return players.toList()
    }

    fun getPlayersByTeam(teamId: Int): List<PlayerData> {
        return players.filter { it.teamId == teamId }
    }

    fun getPlayersWithoutTeam(): List<PlayerData> {
        return players.filter { it.teamId == null }
    }

    fun updatePlayerPoints(id: Int, newPoints: Int) {
        val index = players.indexOfFirst { it.id == id }
        if (index != -1) {
            players[index] = players[index].copy(points = newPoints)
        }
    }

    fun assignPlayerToTeam(playerId: Int, teamId: Int?) {
        val index = players.indexOfFirst { it.id == playerId }
        if (index != -1) {
            players[index] = players[index].copy(teamId = teamId)
        }
    }

    fun deletePlayer(id: Int) {
        players.removeIf { it.id == id }
    }

    // ===== TEAM OPERATIONS =====

    fun addTeam(name: String) {
        teams.add(TeamData(nextTeamId++, name))
    }

    fun getAllTeams(): List<TeamData> {
        return teams.toList()
    }

    fun getTeamWithPlayers(teamId: Int): TeamWithPlayers? {
        val team = teams.find { it.id == teamId } ?: return null
        val teamPlayers = getPlayersByTeam(teamId)
        return TeamWithPlayers(team, teamPlayers)
    }

    fun deleteTeam(id: Int) {
        // Remove team assignment from players first
        players.forEachIndexed { index, player ->
            if (player.teamId == id) {
                players[index] = player.copy(teamId = null)
            }
        }
        teams.removeIf { it.id == id }
    }
}

// ===== DATA CLASSES =====

data class PlayerData(
    val id: Int,
    val name: String,
    val points: Int,
    val teamId: Int? = null
)

data class TeamData(
    val id: Int,
    val name: String
)

data class TeamWithPlayers(
    val team: TeamData,
    val players: List<PlayerData>
) {
    val totalPoints: Int get() = players.sumOf { it.points }
}

