package gui

import entity.Card
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextArea
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual
import tools.aqua.bgw.components.layoutviews.Pane


/**
 * This is the main scene where the game is played.
 * It shows staircase, hands, log, score and reacts to changes.
 */
class GameScene(
    private val app: SopraApplication,
    private val rootService: RootService
) : BoardGameScene(1920, 1080), Refreshable {

    private val loader = CardImageLoader()

    private var selectedCardView: CardView? = null
    private var selectedCard: Card? = null
    private var previousFrontVisual: Visual? = null

    /**
     * Selects a card from player's hand so it can be used.
     */
    private fun selectCard(cardView: CardView, card: Card) {
        if (selectedCardView != null && previousFrontVisual != null) {
            selectedCardView!!.frontVisual = previousFrontVisual!!
        }

        selectedCardView = cardView
        selectedCard = card
        previousFrontVisual = cardView.frontVisual
        cardView.frontVisual = ColorVisual(255, 255, 180)
    }

    /**
     * Clears card selection.
     */
    private fun clearSelection() {
        if (selectedCardView != null && previousFrontVisual != null) {
            selectedCardView!!.frontVisual = previousFrontVisual!!
        }

        selectedCard = null
        selectedCardView = null
        previousFrontVisual = null
    }

    private val drawStack = CardStack<CardView>(
        posX = 1450.0, posY = 400.0,
        visual = ColorVisual(0, 120, 0)
    ).apply {
        width = 110.0; height = 150.0
    }

    private val discardStack = CardStack<CardView>(
        posX = 1600.0, posY = 400.0,
        visual = ColorVisual(40, 40, 40)
    ).apply {
        width = 110.0; height = 150.0
    }



        private val cardWidth = 80.0
        private val cardHeight = 110.0

        private val staircaseLeftX = 400.0
        private val staircaseTopY = 400.0
        private val staircaseColumnGap = cardWidth + 15.0
        private val staircaseVerticalGap = 20.0

        private val staircasePiles: List<Pane<CardView>> = List(5) { col ->
            Pane<CardView>(
                posX = staircaseLeftX + col * staircaseColumnGap,
                posY = staircaseTopY,
                width = cardWidth,
                height = 5 * (cardHeight + staircaseVerticalGap)
            )
        }




    private val player1Hand = LinearLayout<CardView>(
        posX = 450.0,
        posY = 850.0,
        spacing = 30.0
    ).apply {
        width = 800.0
        height = 160.0
    }

    private val player2Hand = LinearLayout<CardView>(
        posX = 450.0,
        posY = 40.0,
        spacing = 30.0
    ).apply {
        width = 800.0
        height = 160.0
    }

    private val logArea = TextArea(
        posX = 10.00, posY = 700.0,
        width = 300.0, height = 300.0,
        text = "LOG:\n",
        visual = ColorVisual(245, 245, 245)
    )

    private val scoreLabel = Label(
        posX = 1600.0, posY = 900.0,
        width = 200.0, height = 50.0,
        text = "SCORE: 0"
    ).apply {
        font = font.copy(size = 22)
    }

    init {
        background = ColorVisual(0, 100, 0)
        addComponents(drawStack, discardStack)
        staircasePiles.forEach { addComponents(it) }
        addComponents(player1Hand, player2Hand, logArea, scoreLabel)
        discardStack.onMouseClicked = { handleClickOnDiscard() }
    }

    /**
     * When clicking discard pile with a selected card.
     */
    private fun handleClickOnDiscard() {
        val card = selectedCard ?: return
        rootService.playerService.discardCard(card)
        clearSelection()
    }

    /**
     * Called after game starts. Sets everything up.
     */
    override fun refreshAfterStartGame() {
        val game = rootService.requireGame()

        refreshHand(0)

        for (i in 0 until 5)
            refreshStaircase(i)

        refreshDrawStack()
        refreshDiscardStack()
        refreshLog()

        scoreLabel.text = "SCORE: ${game.currentPlayer.score}"
        clearSelection()
    }

    /**
     * Updates one row of the staircase.
     */
    override fun refreshStaircase(index: Int) {
        val game = rootService.requireGame()


        val staircaseBottomY = 750.0
        val staircaseVerticalGap = 20.0

        if (index == -1) {
            for (colIndex in game.staircase.indices) {
                refreshStaircase(colIndex)
            }
            return
        }

        val columnPane = staircasePiles[index]
        val colCards = game.staircase[index]

        columnPane.clear()
        if (colCards.isEmpty()) return

        val bottomTopLocalY = staircaseBottomY - cardHeight - columnPane.posY

        for (i in colCards.indices) {
            val card = colCards[i]

            val view = CardView(
                front = loader.frontImageFor(card.suit, card.value),
                back = loader.backImage
            ).apply {
                width = cardWidth
                height = cardHeight
            }

            view.posX = 0.0
            view.posY = bottomTopLocalY - i * (cardHeight + staircaseVerticalGap)

            if (i == colCards.lastIndex) {
                view.showFront()
            } else {
                view.showBack()
            }

            view.onMouseClicked = stairHandler@{
                val selected = selectedCard
                val topCard = colCards.lastOrNull()
                val isTop = (card == topCard)
                if (selected != null) {
                    val match = (selected.suit == card.suit) || (selected.value == card.value)

                    if (match && isTop) {
                        rootService.playerService.combineCard(selected, card)
                        clearSelection()
                    }
                    return@stairHandler
                }
                if (!isTop) {
                    return@stairHandler
                }
                if (game.currentPlayer.hasDestroyed) {
                    game.log.add("You already destroyed a card this turn.")
                    refreshLog()
                    return@stairHandler
                }
                if (game.currentPlayer.score >= 5) {
                    rootService.playerService.destroyCard(card)
                    return@stairHandler
                }

                game.log.add("Not enough points to destroy a card! You need at least 5.")
                refreshLog()
            }

            columnPane.add(view)
        }
    }





    /**
     * Refreshes the player's hand (always bottom).
     */
    override fun refreshHand(index: Int) {

        clearSelection()

        val game = rootService.requireGame()
        val current = game.currentPlayer

        player1Hand.clear()
        player2Hand.clear()

        val bottomLayout = player1Hand

        val cards = current.handCards

        cards.forEach { card ->
            val cv = CardView(
                front = loader.frontImageFor(card.suit, card.value),
                back = loader.backImage
            ).apply {
                width = cardWidth
                height = cardHeight

                onMouseClicked = {
                    if (selectedCardView == this) {
                        clearSelection()
                    } else {
                        selectCard(this, card)
                    }
                }
            }

            cv.showFront()
            bottomLayout.add(cv)
        }
    }

    /**
     * Updates draw stack view.
     */
    override fun refreshDrawStack() {
        drawStack.clear()
        val size = rootService.requireGame().drawStack.size

        repeat(size) {
            val cv = CardView(
                front = loader.blankImage,
                back = loader.backImage
            ).apply {
                width = 110.0
                height = 150.0
                showBack()
            }
            drawStack.add(cv)
        }
    }

    /**
     * Updates discard stack view.
     */
    override fun refreshDiscardStack() {
        discardStack.clear()

        val top = rootService.requireGame().discardStack.lastOrNull()
        if (top != null) {
            val cv = CardView(
                front = loader.frontImageFor(top.suit, top.value),
                back = loader.backImage
            ).apply {
                width = 110.0
                height = 150.0
                showFront()
            }
            discardStack.add(cv)
        }
    }

    /**
     * Updates log text.
     */
    override fun refreshLog() {
        val game = rootService.requireGame()
        logArea.text = "LOG:\n" + game.log.joinToString("\n")
    }

    /**
     * Called when turn ends. Shows next player's turn.
     */
    override fun refreshAfterTurn() {
        val game = rootService.requireGame()

        scoreLabel.text = "SCORE: ${game.currentPlayer.score}"
        refreshHand(0)
        clearSelection()
        app.showStartTurnScene()
    }

    /**
     * Goes to result scene when game ends.
     */
    override fun refreshToEndGame() {
        app.showResultScene()
    }
}

