package gui

import tools.aqua.bgw.core.BoardGameApplication
import service.RootService

/**
 * Main application class for the SoPra game.
 * It creates the different scenes and switches between them.
 */
class SopraApplication : BoardGameApplication("SoPra Game") {

    /** Central service that handles all game logic. */
    val rootService = RootService()

    /** Main menu scene shown at the start. */
    val mainMenuScene = MainMenuScene(this, rootService)

    /** Scene where the actual game is played. */
    val gameScene = GameScene(this, rootService)

    /** Scene shown at the beginning of each player's turn. */
    val startTurnScene = StartTurnScene(this, rootService)

    /** Final scene shown when the game ends. */
    val resultScene = ResultScene(this, rootService)

    init {
        // Register scenes so they can get refresh updates from the game logic
        rootService.addRefreshable(mainMenuScene)
        rootService.addRefreshable(gameScene)
        rootService.addRefreshable(startTurnScene)
        rootService.addRefreshable(resultScene)

        // Start with the main menu
        showGameScene(mainMenuScene)
    }

    /** Shows the main menu screen. */
    fun showMainMenuScene() = showGameScene(mainMenuScene)

    /** Shows the main game screen. */
    fun showGameScene() = showGameScene(gameScene)

    /** Shows the scene that appears at the start of a player's turn. */
    fun showStartTurnScene() = showGameScene(startTurnScene)

    /** Shows the final results screen. */
    fun showResultScene() = showGameScene(resultScene)
}


