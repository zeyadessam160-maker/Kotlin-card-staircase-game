package service
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import entity.*
/**
 * Unit tests for the [GameService.startGame] function.
 *
 * Ensures that the method correctly:
 * - Builds and shuffles a full 52-card deck.
 * - Creates players with 5 cards each.
 * - Initializes the staircase with stacks of sizes [5, 4, 3, 2, 1].
 * - Sets the draw and discard stacks properly.
 * - Registers the game state in [RootService].
 * - Starts with player 1 as the current player.
 * - Logs a proper start message.
 */
class GameServiceStartGameTest {

    private var rootService = RootService()
    private var gameService = rootService.gameService

    /**
     * Resets the service instances before each test
     * to ensure clean initialization.
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = rootService.gameService
    }

    /**
     * Tests that [GameService.startGame] properly initializes
     * the game state and all its components.
     */
    @Test
    fun startGame() {
        // --- Act ---
        gameService.startGame("Alice", "Bob")
        val game = rootService.requireGame()

        // --- Assert ---

        // Player names and initialization
        assertEquals("Alice", game.player1.name)
        assertEquals("Bob", game.player2.name)

        // Current player should be player1
        assertSame(game.player1, game.currentPlayer)

        // Each player must have exactly 5 cards
        assertEquals(5, game.player1.handCards.size)
        assertEquals(5, game.player2.handCards.size)

        // Staircase stacks sizes: [5, 4, 3, 2, 1]
        val expectedSizes = listOf(5, 4, 3, 2, 1)
        val actualSizes = game.staircase.map { it.size }
        assertEquals(expectedSizes, actualSizes)

        // Draw stack must contain remaining 27 cards
        assertEquals(27, game.drawStack.size)

        // Discard stack must be empty at start
        assertTrue(game.discardStack.isEmpty())

        // Log should contain exactly one start message
        assertEquals(1, game.log.size)
        assertTrue(game.log.first().contains("Game started: Alice vs Bob"))

        // Flags
        assertFalse(game.hasRemoved)
    }
}

/**
 * Unit tests for the [GameService.compareCards] function.
 *
 * This test verifies that the function correctly evaluates
 * the combinability and total score of two cards according to
 * the defined rules:
 * - Cards can be combined if suit **or** value matches.
 * - Returns 0 if neither matches.
 * - Score calculation: 2–10 → number, J=10, Q=15, K=20, A=1.
 * - Throws [IllegalStateException] if no game is currently active.
 */
class GameServiceCompareCardsTest {

    private var rootService = RootService()
    private var gameService = rootService.gameService

    /**
     * Ensures a clean [RootService] and [GameService] setup before each test.
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = rootService.gameService
        // A dummy game must be started to satisfy requireGame()
        gameService.startGame("Alice", "Bob")
    }

    /**
     * Tests that non-matching cards (different suit and value)
     * correctly return a score of 0.
     */
    @Test
    fun compareCards_nonMatching() {
        val card1 = Card(CardSuit.CLUBS, CardValue.TWO)
        val card2 = Card(CardSuit.HEARTS, CardValue.THREE)
        val result = gameService.compareCards(card1, card2)
        assertEquals(0, result)
    }

    /**
     * Tests that cards matching by value but different suit
     * correctly return the sum of their individual scores.
     */
    @Test
    fun compareCards_sameValueDifferentSuit() {
        val card1 = Card(CardSuit.CLUBS, CardValue.KING)
        val card2 = Card(CardSuit.HEARTS, CardValue.KING)
        val result = gameService.compareCards(card1, card2)
        assertEquals(40, result) // 20 + 20
    }

    /**
     * Tests that cards matching by suit but different value
     * correctly return the sum of their scores.
     */
    @Test
    fun compareCards_sameSuitDifferentValue() {
        val card1 = Card(CardSuit.SPADES, CardValue.QUEEN)
        val card2 = Card(CardSuit.SPADES, CardValue.THREE)
        val result = gameService.compareCards(card1, card2)
        assertEquals(18, result) // 15 + 3
    }

    /**
     * Tests that [IllegalStateException] is thrown if no game
     * has been initialized before calling [compareCards].
     */
    @Test
    fun compareCards_noGameRunning_throws() {
        val localRoot = RootService()
        val localGameService = localRoot.gameService
        val c1 = Card(CardSuit.CLUBS, CardValue.ACE)
        val c2 = Card(CardSuit.SPADES, CardValue.ACE)
        assertThrows(IllegalStateException::class.java) {
            localGameService.compareCards(c1, c2)
        }
    }
}

/**
 * Unit tests for the [GameService.drawCard] function.
 *
 * Verifies that:
 * - The current player successfully draws a card from the draw stack.
 * - The draw stack is refilled from the discard stack if initially empty.
 * - A game ends if the draw stack is empty *and* no card has been removed.
 * - Exceptions are thrown if the player already has 5 cards or no game is running.
 */
class GameServiceDrawCardTest {

    private var rootService = RootService()
    private var gameService = rootService.gameService

    /** Ensures a clean setup before each test. */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = rootService.gameService
    }

    /**
     * Tests that a player successfully draws a card from a non-empty draw stack.
     */
    @Test
    fun drawCard_normalCase() {
        gameService.startGame("Alice", "Bob")
        val game = rootService.requireGame()

        // Reduce hand size to simulate needing a draw
        game.currentPlayer.handCards.removeLast()
        val beforeDrawSize = game.drawStack.size

        gameService.drawCard()

        // Player should have 5 cards again
        assertEquals(5, game.currentPlayer.handCards.size)
        // Draw stack should decrease by 1
        assertEquals(beforeDrawSize - 1, game.drawStack.size)
        // Log should contain a draw entry
        assertTrue(game.log.last().contains("drew a card"))
    }

    /**
     * Tests that [IllegalStateException] is thrown if the player
     * already has 5 cards before drawing.
     */
    @Test
    fun drawCard_playerHasFullHand_throws() {
        gameService.startGame("Alice", "Bob")
        assertThrows(IllegalStateException::class.java) {
            gameService.drawCard()
        }
    }

    /**
     * Tests that when the draw stack is empty and `hasRemoved` is false,
     * the game ends (no exception, no crash).
     */
    @Test
    fun drawCard_emptyDeck_noRemoved_triggersEndGame() {
        gameService.startGame("Alice", "Bob")
        val game = rootService.requireGame()

        // Player has only 4 cards (so drawing is allowed)
        game.currentPlayer.handCards.removeLast()

        // Empty draw stack, simulate endgame condition
        game.drawStack.clear()
        game.hasRemoved = false

        // No exception should be thrown → drawCard() should internally end the game
        assertDoesNotThrow {
            gameService.drawCard()
        }

        // Optional: verify that "Game over." was logged
        assertTrue(game.log.any { it.contains("Game over") })
    }

    /**
     * Tests that when the draw stack is empty but `hasRemoved` is true,
     * the discard stack is shuffled and refilled into the draw stack.
     */
    @Test
    fun drawCard_refillsFromDiscardStack() {
        gameService.startGame("Alice", "Bob")
        val game = rootService.requireGame()

        // Prepare a discard stack with 2 cards
        game.drawStack.clear()
        game.hasRemoved = true
        game.discardStack.add(Card(CardSuit.CLUBS, CardValue.KING))
        game.discardStack.add(Card(CardSuit.SPADES, CardValue.TWO))

        // Player has fewer than 5 cards so they can draw
        game.currentPlayer.handCards.removeLast()
        gameService.drawCard()

        // Assert — after refill and draw:
        // 1 card remains in draw stack
        assertEquals(1, game.drawStack.size)
        // discard stack should now be empty
        assertTrue(game.discardStack.isEmpty())
        // player has 5 cards again
        assertEquals(5, game.currentPlayer.handCards.size)
    }

    /**
     * Tests that calling [drawCard] without an initialized game
     * results in an [IllegalStateException].
     */
    @Test
    fun drawCard_noGameRunning_throws() {
        val localRoot = RootService()
        val localGameService = localRoot.gameService
        assertThrows(IllegalStateException::class.java) {
            localGameService.drawCard()
        }
    }
}


/**
 * Unit tests for the [GameService.endGame] function.
 *
 * Verifies that the game concludes correctly under the two valid end conditions:
 * - The staircase is completely empty.
 * - The draw stack is empty and `hasRemoved` is false.
 *
 * Also ensures that invalid end conditions throw exceptions.
 */
class GameServiceEndGameTest {

    private lateinit var rootService: RootService
    private lateinit var gameService: GameService

    /** Sets up a fresh [RootService] and [GameService] before each test. */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = rootService.gameService
    }

    /**
     * Tests that the game correctly ends when the staircase is empty.
     * Checks score calculation and log messages.
     */
    @Test
    fun endGame_staircaseEmpty_success() {
        gameService.startGame("Alice", "Bob")
        val game = rootService.requireGame()

        // Make staircase empty
        game.staircase.forEach { it.clear() }

        // Give players some collected cards
        game.player1.collectedCards.add(Card(CardSuit.HEARTS, CardValue.KING))
        game.player2.collectedCards.add(Card(CardSuit.SPADES, CardValue.TEN))

        // Call endGame directly
        gameService.endGame()

        // Assertions
        assertTrue(game.log.any { it.contains("Game over") })
        assertTrue(game.log.any { it.contains("Winner") || it.contains("Draw") })
        assertEquals(20, game.player1.score)
        assertEquals(10, game.player2.score)
    }

    /**
     * Tests that the game correctly ends when the draw stack is empty and
     * `hasRemoved` is false (no recent removal).
     */
    @Test
    fun endGame_deckEmptyAndNoRemovals_success() {
        gameService.startGame("Alice", "Bob")
        val game = rootService.requireGame()

        game.drawStack.clear()
        game.hasRemoved = false
        game.player1.collectedCards.add(Card(CardSuit.CLUBS, CardValue.QUEEN))
        game.player2.collectedCards.add(Card(CardSuit.DIAMONDS, CardValue.JACK))

        gameService.endGame()

        assertTrue(game.log.any { it.contains("Game over") })
        assertTrue(game.log.last().contains("Winner") || game.log.last().contains("Draw"))
    }

    /**
     * Tests that [endGame] throws [IllegalStateException] when called
     * while neither valid end condition is met.
     */
    @Test
    fun endGame_invalidConditions_throws() {
        gameService.startGame("Alice", "Bob")
        val game = rootService.requireGame()

        // Game still ongoing: staircase not empty, draw stack not empty
        game.hasRemoved = true

        assertThrows(IllegalStateException::class.java) {
            gameService.endGame()
        }
    }

    /**
     * Tests that calling [endGame] with no initialized game throws
     * an [IllegalStateException].
     */
    @Test
    fun endGame_noGameRunning_throws() {
        val localRoot = RootService()
        val localGameService = localRoot.gameService

        assertThrows(IllegalStateException::class.java) {
            localGameService.endGame()
        }
    }
}