package service
import entity.KartentreppeGame


/**
 * The root service class is responsible for managing services and the entity layer reference.
 * This class acts as a central hub for every other service within the application.
 *
 * @property game The current active [KartentreppeGame] instance.
 *         It is set when a new game starts and shared between all services.
 *  @property gameService Handles game-wide logic (setup, drawing, refilling, ending).
 *  @property playerService Handles player actions (combining, discarding, destroying cards).
 */
class RootService {
    @Suppress("LateinitUsage")
    lateinit var game: KartentreppeGame
        private set
    /** Service for game-wide actions (setup, draw/refill, end conditions, etc.). */
    val gameService: GameService = GameService(this)
    /** Service for player-centric actions (combine/discard/destroy, turn flow helpers, etc.). */
    val playerService: PlayerService = PlayerService(this)

    /**
     * Returns the currently active [KartentreppeGame] instance if initialized.
     *
     * @throws IllegalStateException if the game has not been initialized yet.
     * This method is used by service classes to safely access the game state.
     */
    fun requireGame(): KartentreppeGame =
        if (::game.isInitialized) game else throw IllegalStateException("Game not running.")

    /**
     * Registers a [Refreshable] UI listener with all services that emit refresh events.
     *
     * Call this once per screen/panel that should react to state changes.
     *
     * @param newRefreshable The UI listener to register.
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerService.addRefreshable(newRefreshable)
    }
    /**
     * Internal helper to set the current [game] once a new game has been created
     * by the setup flow. Intended to be called from service classes only.
     *
     * @param newGame Newly created game state to become the active one.
     */
    internal fun setGame(newGame: KartentreppeGame) {
        this.game = newGame
    }
}
