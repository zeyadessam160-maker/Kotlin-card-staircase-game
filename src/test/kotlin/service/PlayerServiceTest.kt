package service
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import entity.*

/**
 * Unit tests for [PlayerService.combineCard].
 *
 * Verifies that combining two matching cards:
 * - Removes them from the hand and staircase.
 * - Adds them to the player's collected pile.
 * - Increases the score correctly.
 * - Logs the correct action.
 * - Triggers end-game if staircase becomes empty.
 *
 * Also tests that invalid combinations throw the correct exceptions.
 */
class PlayerServiceCombineCardTest {

    private var rootService = RootService()
    private var playerService = rootService.playerService
    private var gameService = rootService.gameService

    /** Recreates all service instances before each test to ensure isolation. */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerService = rootService.playerService
        gameService = rootService.gameService
        gameService.startGame("Alice", "Bob")
    }

    /**
     * Tests that two matching cards (same suit) are combined correctly,
     * with correct score update and log entry.
     */
    @Test
    fun combineCard_validMatch_success() {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        val handCard = Card(CardSuit.HEARTS, CardValue.FIVE)
        val stairCard = Card(CardSuit.HEARTS, CardValue.TWO)

        // Ensure valid preconditions
        player.handCards.clear()
        player.handCards.add(handCard)
        game.staircase[0].clear()
        game.staircase[0].add(stairCard)
        // Ensure the draw stack has at least one card so drawCard() works
        game.drawStack.add(Card(CardSuit.CLUBS, CardValue.ACE))

        // --- Act ---
        playerService.combineCard(handCard, stairCard)

        // --- Assert ---
        assertTrue(player.collectedCards.containsAll(listOf(handCard, stairCard)))
        assertEquals(7, player.score) // 5 + 2
        assertTrue(game.hasRemoved)
    }

    /**
     * Tests that [IllegalArgumentException] is thrown if the hand card
     * does not exist in the player's hand.
     */
    @Test
    fun combineCard_handCardNotInHand_throws() {
        val game = rootService.requireGame()
        val fakeHandCard = Card(CardSuit.CLUBS, CardValue.THREE)
        val stairCard = game.staircase[0].last()

        assertThrows(IllegalArgumentException::class.java) {
            playerService.combineCard(fakeHandCard, stairCard)
        }
    }

    /**
     * Tests that [IllegalArgumentException] is thrown if the staircase
     * card is not the open (top) card of any stack.
     */
    @Test
    fun combineCard_invalidStairCard_throws() {
        val game = rootService.requireGame()
        val player = game.currentPlayer
        val handCard = player.handCards.first()
        val fakeStairCard = Card(CardSuit.CLUBS, CardValue.TWO) // not on top

        assertThrows(IllegalArgumentException::class.java) {
            playerService.combineCard(handCard, fakeStairCard)
        }
    }

    /**
     * Tests that [IllegalArgumentException] is thrown if cards do not
     * match by suit or value.
     */
    @Test
    fun combineCard_cardsNotMatching_throws() {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        val handCard = Card(CardSuit.HEARTS, CardValue.FOUR)
        val stairCard = Card(CardSuit.SPADES, CardValue.KING)

        player.handCards.clear()
        player.handCards.add(handCard)
        game.staircase[0].clear()
        game.staircase[0].add(stairCard)

        assertThrows(IllegalArgumentException::class.java) {
            playerService.combineCard(handCard, stairCard)
        }
    }

    /**
     * Tests that if the staircase becomes completely empty,
     * [GameService.endGame] is automatically triggered.
     */
    @Test
    fun combineCard_triggersEndGameWhenStairEmpty() {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        val handCard = Card(CardSuit.DIAMONDS, CardValue.TEN)
        val stairCard = Card(CardSuit.DIAMONDS, CardValue.FIVE)

        player.handCards.clear()
        player.handCards.add(handCard)
        game.staircase.forEach { it.clear() } // all stacks empty
        game.staircase[0].add(stairCard)

        playerService.combineCard(handCard, stairCard)

        assertTrue(game.log.any { it.contains("Game over") })
    }
}

/**
 * Unit tests for the [PlayerService.discardCard] function.
 *
 * Verifies that:
 * - The chosen card is removed from the player's hand.
 * - The card is added to the discard stack.
 * - A log entry is created.
 * - A new card is drawn afterward (hand restored to 5).
 * - Exceptions are thrown if the card is invalid or no game is running.
 */
class PlayerServiceDiscardCardTest {

    private var rootService = RootService()
    private var playerService = rootService.playerService

    /** Clean setup before each test to ensure isolation. */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerService = rootService.playerService
        rootService.gameService.startGame("Alice", "Bob")
    }

    /**
     * Tests that discarding a valid card:
     * - Removes it from the player's hand
     * - Adds it to the discard stack
     * - Creates a log entry
     * - Restores hand size back to 5 (due to drawCard)
     */
    @Test
    fun discardCard_valid_success() {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        // Prepare valid discard scenario
        val cardToDiscard = player.handCards.first()
        val initialHandSize = player.handCards.size
        val initialDiscardSize = game.discardStack.size

        // Add some cards to drawStack to prevent empty draw
        repeat(5) {
            game.drawStack.add(Card(CardSuit.CLUBS, CardValue.values()[it]))
        }

        // --- Act ---
        playerService.discardCard(cardToDiscard)

        // --- Assert ---
        assertEquals(initialHandSize, player.handCards.size, "Hand size should return to 5 after drawing.")
        assertEquals(initialDiscardSize + 1, game.discardStack.size, "Discard stack should increase by 1.")
        assertTrue(game.discardStack.last() == cardToDiscard, "Discarded card should be on top of discard stack.")
        assertTrue(game.log.any { it.contains("discarded") }, "Log should contain discard entry.")
    }

    /**
     * Tests that an [IllegalArgumentException] is thrown if the player
     * tries to discard a card not in their hand.
     */
    @Test
    fun discardCard_invalidCard_throws() {
        val game = rootService.requireGame()
        val player = game.currentPlayer
        val invalidCard = Card(CardSuit.SPADES, CardValue.ACE)

        assertThrows(IllegalArgumentException::class.java) {
            playerService.discardCard(invalidCard)
        }
    }

    /**
     * Tests that calling [discardCard] without a running game
     * throws an [IllegalStateException].
     */
    @Test
    fun discardCard_noGameRunning_throws() {
        val newRoot = RootService()
        val newPlayerService = newRoot.playerService
        val c = Card(CardSuit.CLUBS, CardValue.KING)
        assertThrows(IllegalStateException::class.java) {
            newPlayerService.discardCard(c)
        }
    }
}

/**
 * Unit tests for the [PlayerService.destroyCard] function.
 *
 * Verifies that:
 * - A valid visible staircase card can be destroyed.
 * - Player score decreases by 5.
 * - The card moves to the discard pile.
 * - Proper log entry and flags are set.
 * - Throws correct exceptions for invalid states or cards.
 */
class PlayerServiceDestroyCardTest {

    private var rootService = RootService()
    private var playerService = rootService.playerService

    /** Clean setup before each test to ensure isolation. */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerService = rootService.playerService
        rootService.gameService.startGame("Alice", "Bob")
    }

    /**
     * Tests that a valid staircase card is successfully destroyed:
     * - Card removed from staircase
     * - Added to discard stack
     * - Player score reduced by 5
     * - Correct log and flags updated
     */
    @Test
    fun destroyCard_valid_success() {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        // Pick a top card from the first staircase stack
        val targetCard = game.staircase[0].last()

        // Give player at least 10 points so they can afford it
        player.score = 10
        player.hasDestroyed = false

        // Record initial sizes
        val initialDiscardSize = game.discardStack.size
        val initialStackSize = game.staircase[0].size

        // --- Act ---
        playerService.destroyCard(targetCard)

        // --- Assert ---
        assertEquals(initialStackSize - 1, game.staircase[0].size)
        assertEquals(initialDiscardSize + 1, game.discardStack.size)
        assertTrue(game.discardStack.contains(targetCard))
        assertEquals(5, player.score, "Player score should decrease by 5.")
        assertTrue(player.hasDestroyed, "Player should be marked as having destroyed a card.")
        assertTrue(game.hasRemoved, "Game should mark a card as removed.")
        assertTrue(game.log.any { it.contains("destroyed") }, "Log should contain destroy message.")
    }

    /**
     * Tests that an [IllegalStateException] is thrown if player
     * has fewer than 5 points.
     */
    @Test
    fun destroyCard_notEnoughPoints_throws() {
        val game = rootService.requireGame()
        val player = game.currentPlayer
        player.score = 3
        val card = game.staircase[0].last()

        assertThrows(IllegalArgumentException::class.java) {
            playerService.destroyCard(card)
        }
    }
    /**
     * Tests that an [IllegalStateException] is thrown if the player
     * has already destroyed a card this turn.
     */
    @Test
    fun destroyCard_alreadyDestroyed_throws() {
        val game = rootService.requireGame()
        val player = game.currentPlayer
        player.score = 10
        player.hasDestroyed = true
        val card = game.staircase[0].last()

        assertThrows(IllegalArgumentException::class.java) {
            playerService.destroyCard(card)
        }
    }

    /**
     * Tests that an [IllegalArgumentException] is thrown if the selected
     * card is not a visible (top) staircase card.
     */
    @Test
    fun destroyCard_notVisibleCard_throws() {
        val game = rootService.requireGame()
        val player = game.currentPlayer
        player.score = 10
        player.hasDestroyed = false

        // Pick a non-top card (second-to-last)
        val invalidCard = game.staircase[0][game.staircase[0].size - 2]

        assertThrows(IllegalArgumentException::class.java) {
            playerService.destroyCard(invalidCard)
        }
    }

    /**
     * Tests that calling [destroyCard] with no active game
     * throws an [IllegalStateException].
     */
    @Test
    fun destroyCard_noGameRunning_throws() {
        val newRoot = RootService()
        val newPlayerService = newRoot.playerService
        val card = Card(CardSuit.CLUBS, CardValue.ACE)

        assertThrows(IllegalStateException::class.java) {
            newPlayerService.destroyCard(card)
        }
    }
}

/**
 * Unit tests for the [PlayerService.startTurn] function.
 *
 * Ensures that:
 * - The current player switches correctly between player1 and player2.
 * - The new player's per-turn flags (like `hasDestroyed`) are reset.
 * - The log is updated with the correct turn message.
 * - Throws [IllegalStateException] if the current player cannot be determined
 *   or if no game is active.
 */
class PlayerServiceStartTurnTest {

    private var rootService = RootService()
    private var playerService = rootService.playerService

    /** Ensures clean setup before each test. */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerService = rootService.playerService
        rootService.gameService.startGame("Alice", "Bob")
    }

    /**
     * Tests that the turn switches correctly from player1 to player2.
     */
    @Test
    fun startTurn_switchesToNextPlayer_success() {
        val game = rootService.requireGame()

        val currentBefore = game.currentPlayer
        assertEquals(game.player1, currentBefore)

        // Simulate player1 having destroyed a card (should reset for next turn)
        game.player1.hasDestroyed = true

        playerService.startTurn()

        val currentAfter = game.currentPlayer
        assertEquals(game.player2, currentAfter, "Turn should switch to player2.")
        assertFalse(currentAfter.hasDestroyed, "New player's 'hasDestroyed' flag should reset.")
        assertTrue(game.log.last().contains(game.player2.name), "Log should include new player's name.")
    }

    /**
     * Tests that a second call switches turn back to player1 again.
     */
    @Test
    fun startTurn_switchesBackToPlayer1_success() {
        val game = rootService.requireGame()
        playerService.startTurn() // -> now Bob
        playerService.startTurn() // -> back to Alice

        assertEquals(game.player1, game.currentPlayer, "Turn should return to player1.")
        assertFalse(game.player1.hasDestroyed, "New turn should reset hasDestroyed.")
        assertTrue(game.log.last().contains(game.player1.name), "Log should include player1's name.")
    }

    /**
     * Tests that calling [startTurn] with no game running throws an exception.
     */
    @Test
    fun startTurn_noGameRunning_throws() {
        val newRoot = RootService()
        val newPlayerService = newRoot.playerService

        assertThrows(IllegalStateException::class.java) {
            newPlayerService.startTurn()
        }
    }

    /**
     * Tests that calling [startTurn] with an invalid current player reference
     * (neither player1 nor player2) throws an exception.
     */
    @Test
    fun startTurn_invalidCurrentPlayer_throws() {
        val game = rootService.requireGame()
        game.currentPlayer = Player("Ghost") // unknown player reference

        assertThrows(IllegalStateException::class.java) {
            playerService.startTurn()
        }
    }
}
