package service

import entity.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Simple tests for the RootService.
 * Just checks that the service behaves normally.
 */
class RootServiceTest {

    private var rootService = RootService()

    /**
     * Runs before each test to reset the RootService.
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
    }

    /**
     * Checks that both services exist and are linked to RootService.
     */
    @Test
    fun services_areLinkedToRootService() {
        assertNotNull(rootService.gameService)
        assertNotNull(rootService.playerService)
        assertNotSame(rootService.gameService, rootService.playerService)
    }

    /**
     * Checks that requireGame throws if no game was set yet.
     */
    @Test
    fun requireGame_beforeInitialization_throws() {
        assertThrows(IllegalStateException::class.java) {
            rootService.requireGame()
        }
    }

    /**
     * Checks that setting a game works and requireGame returns it.
     */
    @Test
    fun setGame_and_requireGame_success() {
        val player1 = Player("Alice")
        val player2 = Player("Bob")
        val game = KartentreppeGame(
            player1 = player1,
            player2 = player2,
            currentPlayer = player1,
            log = mutableListOf("Game created."),
            hasRemoved = false,
            drawStack = mutableListOf(),
            discardStack = mutableListOf(),
            staircase = MutableList(5) { ArrayDeque<Card>() }
        )

        rootService.setGame(game)
        val returnedGame = rootService.requireGame()

        assertSame(game, returnedGame)
    }

    /**
     * Checks that adding a refreshable registers it in both services.
     */
    @Test
    fun addRefreshable_registersInBothServices() {
        var calledInGame = false
        var calledInPlayer = false

        val mockRefreshable = object : Refreshable {
            override fun refreshAfterStartGame() {
                calledInGame = true
            }
            override fun refreshAfterTurn() {
                calledInPlayer = true
            }
        }

        rootService.addRefreshable(mockRefreshable)

        // simulate callbacks
        rootService.gameService.onAllRefreshables { refreshAfterStartGame() }
        rootService.playerService.onAllRefreshables { refreshAfterTurn() }

        assertTrue(calledInGame)
        assertTrue(calledInPlayer)
    }

    /**
     * Checks that setting a second game replaces the first.
     */
    @Test
    fun setGame_overwritesPreviousGame() {
        val p1 = Player("A")
        val p2 = Player("B")

        val game1 = KartentreppeGame(
            player1 = p1,
            player2 = p2,
            currentPlayer = p1,
            log = mutableListOf("g1"),
            hasRemoved = false,
            drawStack = mutableListOf(),
            discardStack = mutableListOf(),
            staircase = MutableList(5) { ArrayDeque<Card>() }
        )

        val game2 = KartentreppeGame(
            player1 = p2,
            player2 = p1,
            currentPlayer = p2,
            log = mutableListOf("g2"),
            hasRemoved = false,
            drawStack = mutableListOf(),
            discardStack = mutableListOf(),
            staircase = MutableList(5) { ArrayDeque<Card>() }
        )

        rootService.setGame(game1)
        rootService.setGame(game2)

        assertSame(game2, rootService.requireGame())
    }

    /**
     * Checks that multiple refreshables both get called.
     */
    @Test
    fun addRefreshable_multipleListenersGetCalled() {
        var calls = 0

        val r1 = object : Refreshable {
            override fun refreshAfterTurn() { calls++ }
        }
        val r2 = object : Refreshable {
            override fun refreshAfterTurn() { calls++ }
        }

        rootService.addRefreshable(r1)
        rootService.addRefreshable(r2)

        rootService.playerService.onAllRefreshables { refreshAfterTurn() }

        assertEquals(2, calls)
    }
}
