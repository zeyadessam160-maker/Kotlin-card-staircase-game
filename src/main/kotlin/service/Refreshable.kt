package service

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable{

    /**
     * A single staircase stack changed (card added/removed/replaced).
     *
     * @param index Staircase stack index that changed (0..4).
     */
    fun refreshStaircase(index: Int) { }

    /**
     * A player's hand changed (draw, discard, combine, destroy).
     *
     * @param index The position in the player's hand that changed.
     */
    fun refreshHand(index: Int) { }

    /** The user-visible log changed (a new message was appended). */
    fun refreshLog() { }

    /** The discard pile changed (card added/reset). */
    fun refreshDiscardStack() { }

    /** The draw pile changed (card drawn or stack refilled). */
    fun refreshDrawStack() { }

    /** Initial UI setup right after a new game is created. */
    fun refreshAfterStartGame() { }

    /** End-of-turn updates (e.g., next player highlight). */
    fun refreshAfterTurn() { }
    /**
     * Called once the game has ended.
     *
     * This refresh is used to trigger the transition to the end screen,
     * where final scores, collected cards, and the winner are displayed.
     */
    fun refreshToEndGame() { }
}


