package service
import entity.KartentreppeGame
import entity.Card
import entity.Player
import entity.CardSuit
import entity.CardValue

/**
 * Service for **game-wide logic** of Kartentreppe.
 *
 * Responsibilities:
 * - Create and initialize a new game (`startGame`): build/shuffle 52-card deck, construct staircase (5,4,3,2,1),
 *   deal 5 cards per player, set current player, initialize piles and log.
 * - Handle drawing/refilling (`drawCard`, `refillDrawStack`): keep hand size at 5; when the draw pile is empty,
 *   either end the game (no removals since last shuffle) or refill from the discard pile (shuffle, move, clear).
 * - Provide card-combination scoring helper (`compareCards`) for player actions (e.g., combine).
 * - Detect and finalize the game (`endGame`): compute final scores from collected cards, declare winner/draw,
 *   and trigger UI refresh so the result can be shown.
 *
 * Refresh mechanism:
 * - Inherits from [AbstractRefreshingService] to notify all registered [Refreshable] listeners after state changes:
 *   e.g., `refreshHand`, `refreshDrawStack`, `refreshDiscardStack`,
 *   `refreshStaircase`, `refreshAfterStartGame`, `refreshLog`.
 * - Use `onAllRefreshables { ... }` to broadcast the appropriate UI updates after each operation.
 *
 * Typical call flow:
 * - GUI → [PlayerService] (e.g., combine/discard/destroy) → calls into this service as needed
 *   (`compareCards`, `drawCard`). `drawCard` may end the game if the last card was drawn without any
 *   staircase removals since the last shuffle (checked via `hasRemoved`).
 *
 * Error handling (selected):
 * - Most operations require an active game; use [RootService.requireGame] to
 * enforce and throw `IllegalStateException` if not started.
 * - `startGame` validates non-blank player names (`IllegalArgumentException`).
 * - `drawCard` guards against drawing when the player already has 5
 * cards (by exception or early return pattern, depending on design).
 *
 * Visibility notes:
 * - `startGame`, `drawCard`, `endGame`: internal API for the service layer.
 * - `compareCards`: internal helper used by [PlayerService] combine logic.
 * - `refillDrawStack`: **private**, only called from `drawCard`.
 *
 * See also:
 * - [RootService] (owns the single game state and wires services)
 * - [PlayerService] (player-centric actions)
 * - [KartentreppeGame] (mutable game state)
 * - [Refreshable], [AbstractRefreshingService] (UI refresh infrastructure)
 *
 * @property rootService Central hub providing access to the shared [KartentreppeGame] state and sibling services.
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService(){
    /**
     * Starts a fresh game with two players and initializes all piles.
     *
     * Steps:
     * 1. Build and shuffle a full 52-card deck (4 suits × 13 values).
     * 2. Create the staircase as five stacks with sizes 5, 4, 3, 2, 1 (top cards drawn from the deck).
     * 3. Deal 5 cards to each player's hand.
     * 4. Put the remaining 27 cards into the draw stack; discard stack starts empty.
     * 5. Create the [KartentreppeGame], set the current player to player 1, and log the start.
     * 6. Publish the game via [RootService] and trigger initial UI refreshes.
     *
     * @param player1Name Name of player 1 (starts the game).
     * @param player2Name Name of player 2.
     */
    fun startGame(player1Name: String, player2Name: String) {
        // Validate input
        require(player1Name.isNotBlank() && player2Name.isNotBlank()) {
            "Player names must not be empty."
        }

        // 1) Build & shuffle a full 52-card deck
        val deck = mutableListOf<Card>().apply {
            for (suit in CardSuit.values()) {
                for (value in CardValue.values()) {
                    add(Card(suit, value))
                }
            }
            shuffle()
        }

        // Helper: take n cards from the end of the deck into an ArrayDeque (stack-like)
        fun takeToDeque(n: Int): ArrayDeque<Card> =
            ArrayDeque<Card>().also { dq ->
                repeat(n) { dq.addLast(deck.removeLast()) }
            }

        // 2) Build staircase stacks: sizes 5,4,3,2,1
        val staircase: MutableList<ArrayDeque<Card>> = mutableListOf(
            takeToDeque(5), takeToDeque(4), takeToDeque(3), takeToDeque(2), takeToDeque(1)
        )

        // 3) Create players and deal 5 cards each to their hands
        val player1 = Player(player1Name)
        val player2 = Player(player2Name)
        repeat(5) { player1.handCards.add(deck.removeLast()) }
        repeat(5) { player2.handCards.add(deck.removeLast()) }

        // 4) Remaining deck becomes the draw stack (27 cards); discard starts empty
        val drawStack = mutableListOf<Card>().apply { addAll(deck) } // copy what's left
        val discardStack = mutableListOf<Card>()                     // empty

        // 5) Create game object and publish it via RootService
        val game = KartentreppeGame(
            player1 = player1,
            player2 = player2,
            currentPlayer = player1,
            log = mutableListOf("Game started: $player1Name vs $player2Name"),
            hasRemoved = false,
            drawStack = drawStack,
            discardStack = discardStack,
            staircase = staircase
        )
        rootService.setGame(game)

        // 6) Initial UI refresh (notify all registered Refreshables)
        onAllRefreshables {
            refreshAfterStartGame()
            for (i in 0..4) refreshStaircase(i)
            refreshDrawStack()
            refreshDiscardStack()
            refreshLog()
        }
    }

    /**
     * Cards can be combined if suit OR value matches.
     * If combinable, returns score(card1)+score(card2). Otherwise 0.
     * 2–10 → number, J=10, Q=15, K=20, A=1.
     *
     * @throws IllegalStateException if no game is running.
     */
    internal fun compareCards(card1: Card, card2: Card): Int {
        rootService.requireGame()

        // Not combinable if neither suit nor value matches
        if (card1.suit != card2.suit && card1.value != card2.value) return 0

        return cardPoints(card1) + cardPoints(card2)
    }

    /**
     * Returns the numeric point value of a card according to game rules.
     */
    private fun cardPoints(card: Card): Int = when (card.value) {
        CardValue.TWO -> 2
        CardValue.THREE -> 3
        CardValue.FOUR -> 4
        CardValue.FIVE -> 5
        CardValue.SIX -> 6
        CardValue.SEVEN -> 7
        CardValue.EIGHT -> 8
        CardValue.NINE -> 9
        CardValue.TEN -> 10
        CardValue.JACK -> 10
        CardValue.QUEEN -> 15
        CardValue.KING -> 20
        CardValue.ACE -> 1
    }

    /**
     * Lets the current player draw one card from the draw stack.
     *
     * If the draw stack is empty, the discard stack is shuffled and moved to the draw stack first.
     *
     * Preconditions:
     * - A game must be started.
     * - The current player must have fewer than 5 cards.
     *
     * Postconditions:
     * - The current player has 5 hand cards.
     * - The draw stack is not empty (if refilled).
     *
     * @throws IllegalStateException if no game is running or if the player already has 5 hand cards.
     */
    internal fun drawCard() {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        if (player.handCards.size >= 5)
            throw IllegalStateException("Player already has 5 hand cards.")

        if (game.drawStack.isEmpty()) {
            if (!game.hasRemoved) {
                endGame()
                return
            } else {
                refillDrawStack()
            }
        }

        val drawnCard = game.drawStack.removeLast()
        player.handCards.add(drawnCard)
        game.log.add("${player.name} drew a card.")

        onAllRefreshables {
            refreshHand(player.handCards.lastIndex)
            refreshDrawStack()
            refreshDiscardStack()
            refreshLog()
        }
    }

    /**
     * Refills the draw pile by shuffling all cards from the discard pile and moving them to the draw pile.
     *
     * Preconditions:
     * - A game must be started.
     * - The draw pile must be empty.
     * - The discard pile must contain at least one card.
     *
     * Postconditions:
     * - Discard pile becomes empty.
     * - Draw pile contains the previously discarded cards (now shuffled).
     * - Refresh methods for draw and discard piles are triggered.
     * - `hasRemoved` is reset to false (new shuffle boundary).
     *
     * @throws IllegalStateException if no game is running, the draw pile is not empty,
     *                               or the discard pile is empty.
     */
    private  fun refillDrawStack() {
        val game = rootService.requireGame()

        if (game.drawStack.isNotEmpty()) {
            throw IllegalStateException("Draw pile is not empty.")
        }
        if (game.discardStack.isEmpty()) {
            throw IllegalStateException("Discard pile is empty.")
        }
        // Shuffle discard and move to draw
        val shuffled = game.discardStack.shuffled()
        game.drawStack.addAll(shuffled)
        game.discardStack.clear()

        // After a shuffle, no staircase card has been removed yet since this shuffle
        game.hasRemoved = false
        game.log.add("Draw pile refilled from discard pile.")

        onAllRefreshables {
            refreshDrawStack()
            refreshDiscardStack()
            refreshLog()
        }
    }
    /**
     * Finalizes the game by computing final scores from each player's collected cards,
     * declaring a winner (or draw), and triggering UI refreshes for the end screen.
     *
     * Preconditions (one of):
     * - All staircase stacks are empty, OR
     * - The draw pile is empty and no staircase card has been removed since the last shuffle (`hasRemoved == false`).
     *
     * Scoring:
     * - 2–10 → numeric value, J=10, Q=15, K=20, A=1.
     * - Final score = sum of collected cards' values.
     *
     * Postconditions:
     * - Final results are appended to the log, including each player's collected cards and scores.
     * - UI refreshes are triggered so the end screen can be shown.
     *
     * @throws IllegalStateException if no game is running.
     * @throws IllegalStateException if the end-game condition is not met.
     */
    internal fun endGame() {
        val game = rootService.requireGame()

        val staircaseEmpty = game.staircase.all { it.isEmpty() }
        val deckExhaustedNoRemovals = game.drawStack.isEmpty() && !game.hasRemoved

        if (!staircaseEmpty && !deckExhaustedNoRemovals) {
            throw IllegalStateException("The end game condition was not met.")
        }

        // --- Compute final scores ---
        val p1Score = game.player1.collectedCards.sumOf(::cardPoints)
        val p2Score = game.player2.collectedCards.sumOf(::cardPoints)
        game.player1.score = p1Score
        game.player2.score = p2Score

        // --- Log summary ---
        game.log.add("Game over.")
        game.log.add("${game.player1.name} • Collected (${game.player1.collectedCards.size}): ${cardsSummary(game.player1.collectedCards)} • Score: $p1Score")
        game.log.add("${game.player2.name} • Collected (${game.player2.collectedCards.size}): ${cardsSummary(game.player2.collectedCards)} • Score: $p2Score")
        game.log.add(resultMessage(game.player1.name, p1Score, game.player2.name, p2Score))

        // --- Trigger end-screen refresh ---
        onAllRefreshables {
            refreshLog()
            refreshToEndGame()
        }
    }
    /** Converts a list of cards to a formatted string for logs. */
    private fun cardsSummary(cards: List<Card>): String =
        if (cards.isEmpty()) "—" else cards.joinToString(", ") { "${it.value}${it.suit}" }

    /** Creates a result message indicating winner or draw. */
    private fun resultMessage(p1Name: String, p1Score: Int, p2Name: String, p2Score: Int): String = when {
        p1Score > p2Score -> "Winner: $p1Name ($p1Score : $p2Score)."
        p2Score > p1Score -> "Winner: $p2Name ($p2Score : $p1Score)."
        else -> "Draw: $p1Score : $p2Score."
    }

}


