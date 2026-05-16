package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.util.Font.FontStyle
import tools.aqua.bgw.util.Font.FontWeight
import tools.aqua.bgw.core.Color

/**
 * This scene shows who starts the new turn.
 * It waits for the player to press "Weiter".
 */
class StartTurnScene(
    private val app: SopraApplication,
    private val rootService: RootService,
) : BoardGameScene(1920, 1080), Refreshable {

    /**
     * Dark box behind the text.
     */
    private val overlayBox = Pane<Label>(
        posX = 710.0,
        posY = 350.0,
        width = 500.0,
        height = 300.0,
        visual = ColorVisual(0, 0, 0, 220)
    )

    /**
     * Label saying next player.
     */
    private val nextPlayerLabel = Label(
        posX = 780.0,
        posY = 380.0,
        width = 400.0,
        height = 50.0,
        text = "Nächster Spieler",
        visual = ColorVisual.TRANSPARENT,
        font = Font(
            size = 28,
            color = Color.WHITE,
            fontWeight = FontWeight.BOLD
        )
    )

    /**
     * Shows the player's name.
     */
    private val playerNameLabel = Label(
        posX = 830.0,
        posY = 460.0,
        width = 300.0,
        height = 50.0,
        text = "",
        visual = ColorVisual(60, 60, 60),
        font = Font(size = 24, color = Color.WHITE)
    )

    /**
     * Button to continue to the game scene.
     */
    private val continueButton = Button(
        posX = 870.0,
        posY = 540.0,
        width = 220.0,
        height = 50.0,
        text = "WEITER",
        visual = ColorVisual(100, 100, 100),
        font = Font(size = 20, color = Color.WHITE)
    ).apply {
        onMouseClicked = {
            app.showGameScene()
        }
    }

    init {
        background = ColorVisual(0, 0, 0, 150)
        addComponents(
            overlayBox,
            nextPlayerLabel,
            playerNameLabel,
            continueButton
        )
    }

    /**
     * Updates the scene with the new player's name.
     */
    override fun refreshAfterTurn() {
        val game = rootService.requireGame()
        playerNameLabel.text = game.currentPlayer.name
    }
}


