package entity
/**
 * Represents a single playing card in the Kartentreppe game.
 *
 * Each card has a fixed suit and value. Once created, these values
 * do not change (immutable).
 *
 * @property suit the card's suit (e.g., hearts, clubs, spades, diamonds)
 * @property value the card's face value (e.g., seven, king, ace)
 */

data class Card(
    val suit: CardSuit,
    val value: CardValue
)
