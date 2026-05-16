package entity

/**
 * Represents a player participating in the Kartentreppe game.
 *
 * A player has a name, score, and a set of cards. During the game,
 * the player can collect cards, play cards, and their score can change.
 *
 * @property name the player’s name (constant)
 * @property score the player’s current score
 * @property hasDestroyed true if the player has destroyed a staircase card
 * @property handCards the list of cards currently in the player’s hand (4–5 cards)
 * @property collectedCards the cards collected by the player during the game
 */

class Player(
    val name: String,
    var score: Int = 0,
    var hasDestroyed: Boolean = false,
    val handCards: MutableList<Card> = mutableListOf(),
    val collectedCards: MutableList<Card> = mutableListOf()
)
