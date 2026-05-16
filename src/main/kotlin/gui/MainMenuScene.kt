package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.visual.ColorVisual

/**
 * This is the first screen of the game.
 * Here the user enters two player names and starts the game.
 */
class MainMenuScene(
    private val app: SopraApplication,
    private val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {

    /**
     * Label that shows an error message if names are wrong.
     */
    private val errorLabel = Label(
        posX = 810,
        posY = 570,
        width = 300,
        height = 40,
        text = "",
        visual = ColorVisual(255, 200, 200)
    ).apply {
        isVisible = false
    }

    init {
        background = ColorVisual(245, 245, 245)

        /**
         * Small box holding the input fields and button.
         */
        val inputBox = Pane<UIComponent>(
            posX = 810,
            posY = 300,
            width = 300,
            height = 250,
            visual = ColorVisual(220, 220, 220)
        )

        /**
         * Field for player 1's name.
         */
        val player1Field = TextField(
            posX = 50, posY = 40,
            width = 200, height = 40,
            prompt = "Enter Player 1 name",
            visual = ColorVisual.WHITE
        )

        /**
         * Field for player 2's name.
         */
        val player2Field = TextField(
            posX = 50, posY = 100,
            width = 200, height = 40,
            prompt = "Enter Player 2 name",
            visual = ColorVisual.WHITE
        )

        /**
         * Button that tries to start the game.
         */
        val startButton = Button(
            posX = 90,
            posY = 170,
            width = 120,
            height = 40,
            text = "Start Game",
            visual = ColorVisual(173, 216, 230)
        ).apply {

            onMouseClicked = {
                val name1 = player1Field.text.trim()
                val name2 = player2Field.text.trim()

                try {
                    rootService.gameService.startGame(name1, name2)
                    app.showGameScene()
                } catch (e: IllegalArgumentException) {
                    errorLabel.text = e.message.orEmpty()
                    errorLabel.isVisible = true
                }
            }
        }

        inputBox.addAll(player1Field, player2Field, startButton)

        addComponents(inputBox, errorLabel)
    }
}
