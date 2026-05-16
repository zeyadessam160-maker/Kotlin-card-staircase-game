package entity

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Stack
/**
 * Unit tests for the [KartentreppeGame] class.
 *
 * These tests verify that the game's initialization, mutable properties,
 * and internal data structures (draw stack, discard stack, staircase, and logs)
 * behave as expected. The tests simulate basic operations without involving
 * the GUI or service layers.
 */
class KartentreppeTest {

    /**
     * Ensures that a new [KartentreppeGame] instance correctly initializes
     * all properties to their default state.
     *
     * Expected:
     * - The provided players are correctly assigned.
     * - The current player is correctly set.
     * - `hasRemoved` is false.
     * - All collections (`log`, `drawStack`, `discardStack`, `staircase`) start empty.
     */
    @Test
    fun `initialization sets players and empty state`() {
        val p1 = Player("Alice")
        val p2 = Player("Bob")
        val game = KartentreppeGame(player1 = p1, player2 = p2, currentPlayer = p1)

        assertSame(p1, game.player1)
        assertSame(p2, game.player2)
        assertSame(p1, game.currentPlayer)

        assertFalse(game.hasRemoved)
        assertTrue(game.log.isEmpty())
        assertTrue(game.drawStack.isEmpty())
        assertTrue(game.discardStack.isEmpty())
        assertTrue(game.staircase.isEmpty())
    }
    /**
     * Checks that the [currentPlayer] property of [KartentreppeGame]
     * can be reassigned to switch turns.
     *
     * Expected:
     * - When reassigned, the current player updates correctly.
     */
    @Test
    fun `currentPlayer can be switched`() {
        val p1 = Player("A")
        val p2 = Player("B")
        val game = KartentreppeGame(p1, p2, currentPlayer = p1)

        game.currentPlayer = p2
        assertSame(p2, game.currentPlayer)
    }
    /**
     * Tests that both `log` and `hasRemoved` properties are mutable.
     *
     * Expected:
     * - Entries can be added to the `log` list.
     * - The `hasRemoved` flag can be changed from false to true.
     */
    @Test
    fun `log and hasRemoved are mutable`() {
        val game = KartentreppeGame(Player("A"), Player("B"), currentPlayer = Player("A"))
        game.log.add("A drew a card")
        game.hasRemoved = true

        assertEquals(listOf("A drew a card"), game.log)
        assertTrue(game.hasRemoved)
    }
    /**
     * Validates that the `drawStack` and `discardStack` lists behave
     * like mutable lists when adding and removing cards.
     *
     * Expected:
     * - Cards can be added and removed dynamically.
     * - Removed cards can be moved between lists.
     * - The state of both lists reflects the performed operations.
     */
    @Test
    fun `draw and discard stacks behave like mutable lists`() {
        val game = KartentreppeGame(Player("A"), Player("B"), currentPlayer = Player("A"))

        val c1 = Card(CardSuit.HEARTS, CardValue.SEVEN)
        val c2 = Card(CardSuit.SPADES, CardValue.KING)

        game.drawStack.add(c1)
        game.drawStack.add(c2)
        assertEquals(2, game.drawStack.size)
        assertTrue(game.drawStack.containsAll(listOf(c1, c2)))

        val removed = game.drawStack.removeAt(0)
        game.discardStack.add(removed)

        assertEquals(1, game.drawStack.size)
        assertEquals(1, game.discardStack.size)
        assertSame(removed, game.discardStack.first())
    }
    /**
     * Tests the structure and behavior of the `staircase` list of [Stack]s.
     *
     * Expected:
     * - The staircase can hold multiple stacks of cards.
     * - Each stack maintains LIFO (Last-In-First-Out) order.
     * - Pushing and popping elements behaves correctly.
     */

}