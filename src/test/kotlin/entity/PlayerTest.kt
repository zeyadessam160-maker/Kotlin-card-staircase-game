package entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for the [Player] class.
 *
 * These tests ensure that player objects are correctly initialized
 * and that their properties (score, flags, and card lists) behave
 * as expected during gameplay.
 */
class PlayerTest {
    /**
     * Verifies that the [Player] constructor correctly initializes
     * the player's name and default values.
     *
     * Expected:
     * - `name` matches the given string.
     * - `score` is 0 by default.
     * - `hasDestroyed` is false by default.
     * - `handCards` and `collectedCards` start empty.
     */
    @Test
    fun `constructor should correctly assign name and default values`() {
        val player = Player("Zeyad")

        assertEquals("Zeyad", player.name)
        assertEquals(0, player.score)
        assertFalse(player.hasDestroyed)
        assertTrue(player.handCards.isEmpty())
        assertTrue(player.collectedCards.isEmpty())
    }
    /**
     * Tests that the player's score can be updated.
     *
     * Expected:
     * - Assigning a new score value updates the player's score field.
     */
    @Test
    fun `player score can be updated`() {
        val player = Player("Zeyad")
        player.score = 15
        assertEquals(15, player.score)
    }
    /**
     * Tests that the [hasDestroyed] flag can be toggled.
     *
     * Expected:
     * - Default value is false.
     * - Value can be changed to true.
     */
    @Test
    fun `hasDestroyed flag should be changeable`() {
        val player = Player("Zeyad")
        assertFalse(player.hasDestroyed)
        player.hasDestroyed = true
        assertTrue(player.hasDestroyed)
    }

    /**
     * Ensures that the [handCards] list behaves as a mutable list.
     *
     * Expected:
     * - Cards can be added dynamically.
     * - The list size and contents reflect additions.
     */
    @Test
    fun `handCards list should allow adding cards`() {
        val player = Player("Zeyad")
        val card = Card(CardSuit.HEARTS, CardValue.ACE)
        player.handCards.add(card)

        assertEquals(1, player.handCards.size)
        assertTrue(player.handCards.contains(card))
    }

    /**
     * Ensures that the [collectedCards] list behaves as a mutable list.
     *
     * Expected:
     * - Cards can be added dynamically.
     * - The list size and contents reflect additions.
     */
    @Test
    fun `collectedCards list should allow adding cards`() {
        val player = Player("Zeyad")
        val card = Card(CardSuit.SPADES, CardValue.KING)
        player.collectedCards.add(card)

        assertEquals(1, player.collectedCards.size)
        assertTrue(player.collectedCards.contains(card))
    }
}