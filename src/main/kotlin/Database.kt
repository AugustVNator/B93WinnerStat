// Simple in-memory database (no external dependencies needed)
object Database {
    private var nextId = 1
    private val players = mutableListOf<PlayerData>()

    fun init() {
        // Nothing to initialize for in-memory storage
    }

    fun addPlayer(name: String, points: Int = 0) {
        players.add(PlayerData(nextId++, name, points))
    }

    fun getAllPlayers(): List<PlayerData> {
        return players.toList()
    }

    fun updatePlayerPoints(id: Int, newPoints: Int) {
        val index = players.indexOfFirst { it.id == id }
        if (index != -1) {
            players[index] = players[index].copy(points = newPoints)
        }
    }

    fun deletePlayer(id: Int) {
        players.removeIf { it.id == id }
    }
}

data class PlayerData(
    val id: Int,
    val name: String,
    val points: Int
)

