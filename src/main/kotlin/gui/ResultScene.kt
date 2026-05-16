package gui

import entity.Card
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.util.Font.FontWeight
import tools.aqua.bgw.core.Color

/**
 * This screen shows after the game ends.
 * It displays both players, their scores and their collected cards.
 */
class ResultScene(
    private val app: SopraApplication,
    private val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {

    private val loader = CardImageLoader()

    /**
     * Text for player 1 name.
     */
    private val p1Name = Label(
        posX = 200.0, posY = 100.0,
        width = 400.0, height = 50.0,
        text = "SPIELER 1",
        visual = ColorVisual.WHITE,
        font = Font(size = 26, color = Color.BLACK, fontWeight = FontWeight.BOLD)
    )

    /**
     * Score of player 1.
     */
    private val p1Score = Label(
        posX = 250.0, posY = 160.0,
        width = 300.0, height = 40.0,
        text = "SCORE: 0",
        visual = ColorVisual.WHITE,
        font = Font(size = 20, color = Color.BLACK)
    )

    /**
     * Shows all collected cards of player 1.
     */
    private val p1Cards = LinearLayout<CardView>(
        posX = 150.0, posY = 240.0,
        width = 600.0, height = 500.0,
        spacing = 10.0,
        alignment = Alignment.TOP_LEFT,
        visual = ColorVisual.TRANSPARENT
    )

    /**
     * Text for player 2 name.
     */
    private val p2Name = Label(
        posX = 1320.0, posY = 100.0,
        width = 400.0, height = 50.0,
        text = "SPIELER 2",
        visual = ColorVisual.WHITE,
        font = Font(size = 26, color = Color.BLACK, fontWeight = FontWeight.BOLD)
    )

    /**
     * Score of player 2.
     */
    private val p2Score = Label(
        posX = 1370.0, posY = 160.0,
        width = 300.0, height = 40.0,
        text = "SCORE: 0",
        visual = ColorVisual.WHITE,
        font = Font(size = 20, color = Color.BLACK)
    )

    /**
     * Shows all collected cards of player 2.
     */
    private val p2Cards = LinearLayout<CardView>(
        posX = 1220.0, posY = 240.0,
        width = 600.0, height = 500.0,
        spacing = 10.0,
        alignment = Alignment.TOP_LEFT,
        visual = ColorVisual.TRANSPARENT
    )

    /**
     * Button to restart the game and go back to main menu.
     */
    private val restartButton = Button(
        posX = 860.0,
        posY = 820.0,
        width = 200.0, height = 60.0,
        text = "NEUSTART",
        visual = ColorVisual.DARK_GRAY,
        font = Font(size = 22, color = Color.WHITE, fontWeight = FontWeight.BOLD)
    ).apply {
        onMouseClicked = {
            app.showMainMenuScene()
        }
    }

    init {
        background = ColorVisual(240, 240, 240)

        addComponents(
            p1Name, p1Score, p1Cards,
            p2Name, p2Score, p2Cards,
            restartButton
        )
    }

    /**
     * Updates the screen when the game ends.
     * Fills in scores, winner color, and the collected cards.
     */
    override fun refreshToEndGame() {

        val game = rootService.requireGame()
        val p1 = game.player1
        val p2 = game.player2

        p1Name.text = p1.name
        p2Name.text = p2.name

        p1Score.text = "SCORE: ${p1.score}"
        p2Score.text = "SCORE: ${p2.score}"

        when {
            p1.score > p2.score -> {
                p1Name.visual = ColorVisual(255, 220, 120)
                p2Name.visual = ColorVisual.WHITE
            }
            p2.score > p1.score -> {
                p2Name.visual = ColorVisual(255, 220, 120)
                p1Name.visual = ColorVisual.WHITE
            }
            else -> {
                p1Name.visual = ColorVisual.LIGHT_GRAY
                p2Name.visual = ColorVisual.LIGHT_GRAY
            }
        }

        p1Cards.clear()
        p2Cards.clear()

        p1.collectedCards.forEach {
            p1Cards.add(createCardView(it))
        }

        p2.collectedCards.forEach {
            p2Cards.add(createCardView(it))
        }
    }

    /**
     * Makes a small card image for the result screen.
     */
    private fun createCardView(card: Card): CardView =
        CardView(
            front = loader.frontImageFor(card.suit, card.value),
            back = loader.backImage
        ).apply {
            width = 80.0
            height = 110.0
            showFront()
        }
}



