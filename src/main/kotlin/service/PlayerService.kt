package service
import entity.KartentreppeGame
import entity.Card
import entity.Player
import entity.CardSuit
import entity.CardValue


/**
 * Service class responsible for handling all **player-specific actions** in the Kartentreppe game.
 *
 * It provides the logic for interactions directly controlled by a player during their turn:
 * combining cards with the staircase, discarding cards, destroying cards, and starting a new turn.
 *
 * This class communicates with [RootService] to access and modify the shared [KartentreppeGame] state.
 * It inherits from [AbstractRefreshingService] so that all UI components implementing [Refreshable]
 * can be updated after each state-changing action.
 *
 * ### Responsibilities:
 * - Manage player actions that modify the game state.
 * - Enforce the rules for valid moves (turn order, hand contents, available points).
 * - Notify all registered [Refreshable] listeners after relevant updates.
 *
 * @property rootService Reference to the [RootService] providing access to the current [KartentreppeGame]
 *                       and to other services such as [GameService].
 *
 * @see GameService
 * @see AbstractRefreshingService
 * @see Refreshable
 */
class PlayerService(private val rootService: RootService) : AbstractRefreshingService(){
    /**
     * Combines one of the current player's hand cards with the open (top) card of a staircase stack,
     * provided both cards share the same suit or value.
     *
     * Once combined, both cards are removed from play, added to the player's collected cards,
     * and the player earns points equal to the sum of both cards' values. The top card of the affected
     * staircase stack is revealed (if any remain), and the player immediately draws one replacement card.
     *
     * Preconditions:
     * - A game is currently running and it is the current player's turn.
     * - [handCard] exists in the current player's hand.
     * - [stairCard] is the open (top) card of one of the staircase stacks.
     *
     * Postconditions:
     * - Both [handCard] and [stairCard] are removed from their locations.
     * - Player's score increases by the total of both card values.
     * - Both cards are added to the player's [Player.collectedCards].
     * - The next card below the removed staircase card (if any) becomes visible.
     * - The player draws one card from the draw pile via [GameService.drawCard].
     * - If the last staircase card was removed, [GameService.endGame] is called.
     *
     * @param handCard The card from the current player's hand.
     * @param stairCard The open card selected from the staircase.
     *
     * @throws IllegalStateException If no game is running or it's not the current player's turn.
     * @throws IllegalArgumentException If [handCard] is not in hand, [stairCard] is not open, or
     * the cards do not match by suit or value.
     */

    fun combineCard(handCard: Card, stairCard: Card) {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        // --- Preconditions ---
        require(player.handCards.contains(handCard)) {
            "handCard is not in the current player's hand."
        }

        // Find the staircase stack whose open (top) card is stairCard
        val stackIndex = game.staircase.indexOfFirst { it.isNotEmpty() && it.last() == stairCard }
        require(stackIndex != -1) {
            "stairCard is not the open (top) card of any staircase stack."
        }

        val gained = rootService.gameService.compareCards(handCard, stairCard)
        require(gained != 0) {
            "Cards do not match by suit or value."
        }

        // --- Apply changes ---
        val handIndex = player.handCards.indexOf(handCard)
        player.handCards.remove(handCard)
        game.staircase[stackIndex].removeLast()
        game.hasRemoved = true

        // 2) award points and move both cards to collected
        player.score += gained
        player.collectedCards.add(handCard)
        player.collectedCards.add(stairCard)

        // 3) log & refresh
        game.log.add("${player.name} combined $handCard with $stairCard and gained $gained points.")
        onAllRefreshables {
            refreshStaircase(stackIndex)  // top card changed (or stack emptied)
            refreshHand(handIndex)        // refresh the removed card slot
            refreshLog()
        }

        // --- End condition A: staircase completely empty -> end game immediately
        if (game.staircase.all { it.isEmpty() }) {
            rootService.gameService.endGame()
            return
        }

        // --- Otherwise: draw a replacement card (may end game via condition B)
        rootService.gameService.drawCard()
        rootService.playerService.startTurn()

    }

    /**
     * Discards one card from the current player's hand onto the discard pile.
     *
     * Used when the player cannot or does not want to combine a card with the staircase.
     * After discarding, the player draws one replacement card and the turn ends.
     *
     * Preconditions:
     * - A game is running and it is the current player's turn.
     * - [card] is contained in the current player's hand.
     * - No other end-of-turn action has been performed this turn (not enforced here if no turn state exists).
     *
     * Postconditions:
     * - [card] is removed from the player's hand and placed on top of the discard pile.
     * - No points are gained or lost.
     * - A replacement card is drawn via [GameService.drawCard], then the turn is ended via [endTurn].
     *
     * @param card The hand card to discard.
     *
     * @throws IllegalStateException If no game is running.
     * @throws IllegalArgumentException If [card] is not in the current player's hand.
     */
    fun discardCard(card: Card) {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        // Validate the card is actually in hand
        if (!player.handCards.contains(card)) {
            throw IllegalArgumentException("Card to discard is not in the current player's hand.")
        }
        val handIndex = player.handCards.indexOf(card)
        // Move from hand to discard (top of discard is the list's end)
        player.handCards.remove(card)
        game.discardStack.add(card)

        // Log + refresh
        game.log.add("${player.name} discarded $card.")
        onAllRefreshables {
            refreshHand(handIndex)
            refreshDiscardStack()
            refreshLog()
        }

        // Draw replacement, then end the turn
        rootService.gameService.drawCard()
        rootService.playerService.startTurn()
    }

    /**
     * Destroys a visible (face-up) staircase card and places it on the discard pile.
     *
     * Preconditions:
     * - A game is running and it is the current player's turn.
     * - The player has **at least 5 points**.
     * - The player has **not** already destroyed a card this turn (`hasDestroyed == false`).
     * - [card] is the **open (top)** card of one of the staircase stacks.
     *
     * Effects:
     * - Removes [card] from its staircase stack; the card below (if any) becomes the new top (face-up).
     * - Adds [card] to the discard pile.
     * - Subtracts **5 points** from the current player.
     * - Sets `player.hasDestroyed = true`.
     * - Sets `game.hasRemoved = true` (a staircase card was removed since the last shuffle).
     * - If the staircase becomes completely empty after removal, ends the game.
     *
     * Postconditions:
     * - The discard pile and affected staircase stack are refreshed, as well as the log.
     * - The current player **remains on their turn** (no turn switch, no draw here).
     *
     * @param card The face-up staircase card to destroy.
     *
     * @throws IllegalStateException If no game is running,
     * player's points < 5, or the player already destroyed a card this turn.
     * @throws IllegalArgumentException If [card] is not currently face-up on the staircase
     * (i.e., not the top of a stack).
     */
    fun destroyCard(card: Card) {
        val game = rootService.requireGame()
        val player = game.currentPlayer

        // --- Preconditions ---
        require(player.score >= 5) {
            "Player needs at least 5 points to destroy a card."
        }
        require(!player.hasDestroyed) {
            "Player already destroyed a card this turn."
        }

        // Find which staircase stack has this card on top (face-up)
        val stackIndex = game.staircase.indexOfFirst { it.isNotEmpty() && it.last() == card }
        require(stackIndex != -1) {
            "Selected card is not a face-up (top) staircase card."
        }
 
        // --- Apply action ---
        game.staircase[stackIndex].removeLast()
        game.discardStack.add(card)
        player.score -= 5
        player.hasDestroyed = true
        game.hasRemoved = true

        // --- Log + refresh ---
        game.log.add("${player.name} destroyed $card (−5 points).")
        onAllRefreshables {
            refreshStaircase(stackIndex)
            refreshDiscardStack()
            refreshLog()
        }

        // --- End condition: staircase completely empty → end game immediately ---
        if (game.staircase.all { it.isEmpty() }) {
            rootService.gameService.endGame()
        }
    }

    /**
     * Initializes a new turn for the next player in the Kartentreppe game.
     *
     * This method switches the active player, resets per-turn state flags, and prepares
     * the game for the next move. It also triggers UI updates so the new player can start.
     *
     * Preconditions:
     * - A game is currently running.
     * - The previous player's turn has ended properly (no ongoing actions).
     *
     * Postconditions:
     * - The next player becomes the active player.
     * - The new player's `hasDestroyed` flag is reset to `false`.
     * - The GUI is refreshed via [Refreshable.refreshAfterTurn] and [Refreshable.refreshLog].
     *
     * @throws IllegalStateException If no game is running or the active player cannot be determined.
     */
    fun startTurn() {
        val game = rootService.requireGame()

        val current = game.currentPlayer
        val next = when (current) {
            game.player1 -> game.player2
            game.player2 -> game.player1
            else -> throw IllegalStateException("Unknown current player.")
        }

        // Switch turn
        game.currentPlayer = next

        // Reset per-turn state
        next.hasDestroyed = false

        // Log + refresh
        game.log.add("It's now ${next.name}'s turn.")

        onAllRefreshables {
            refreshAfterTurn()
            refreshLog()
        }
    }

}