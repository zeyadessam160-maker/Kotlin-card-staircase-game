package entity

/**
 * Represents the full state of the Kartentreppe game.
 *
 * This class manages the players, the staircase (piles of cards),
 * draw and discard stacks, and keeps a log of the game’s actions.
 * Since the state of the game changes during play, this is a regular
 * class (not a data class).
 *
 * @property player1 the first player in the game
 * @property player2 the second player in the game
 * @property currentPlayer the player whose turn it currently is
 * @property log a list of messages recording gameplay events
 * @property hasRemoved true if a card from staircase is removed since last shuffle
 * @property drawStack the deck from which cards are drawn
 * @property discardStack the pile of discarded cards
 * @property staircase a list of card stacks representing the game’s staircase
 */


class KartentreppeGame(
    // players and turn
    val player1: Player,
    val player2: Player,
    var currentPlayer: Player,

    // log & flags
    val log: MutableList<String> = mutableListOf(),
    var hasRemoved: Boolean = false,

    // piles & staircase
    val drawStack: MutableList<Card> = mutableListOf(),
    val discardStack: MutableList<Card> = mutableListOf(),
    val staircase: MutableList<ArrayDeque<Card>> = mutableListOf()
)