package entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
/**
 * Unit tests for the [Card] data class.
 *
 * These tests ensure that card objects correctly store and compare
 * their suit and value combinations. Each test focuses on one
 * expected behavior of the [Card] entity.
 */
class CardTest {
    /**
     * Verifies that the [Card] constructor correctly assigns
     * the given [CardSuit] and [CardValue].
     *
     * Expected:
     * - The card’s suit and value match the parameters passed to the constructor.
     */
    @Test
    fun `constructor should correctly assign suit and value`() {
        val card = Card(CardSuit.HEARTS, CardValue.ACE)
        assertEquals(CardSuit.HEARTS, card.suit)
        assertEquals(CardValue.ACE, card.value)
    }
    /**
     * Tests that two [Card] instances with the same [CardSuit]
     * and [CardValue] are considered equal.
     *
     * Expected:
     * - Cards with identical suit and value should be equal.
     */
    @Test
    fun `two cards with same suit and value should be equal`() {
        val card1 = Card(CardSuit.CLUBS, CardValue.KING)
        val card2 = Card(CardSuit.CLUBS, CardValue.KING)
        assertEquals(card1, card2)
    }
    /**
     * Tests that two [Card] instances with different [CardSuit] or [CardValue]
     * are not equal.
     *
     * Expected:
     * - Cards that differ in suit or value should not be equal.
     */
    @Test
    fun `two cards with different suit or value should not be equal`() {
        val card1 = Card(CardSuit.SPADES, CardValue.TEN)
        val card2 = Card(CardSuit.HEARTS, CardValue.TEN)
        val card3 = Card(CardSuit.SPADES, CardValue.NINE)
        assertNotEquals(card1, card2)
        assertNotEquals(card1, card3)
    }

}