import java.util.ArrayList;

import processing.core.PApplet;

public class Uno extends CardGame {
    // Uno-specific state
    UnoComputer computerPlayer;
    boolean choosingWildColor = false;
    UnoCard pendingWildCard;
    ClickableRectangle[] wildColorButtons;
    int wildButtonSize = 24;
    int wildCenterX = 300;
    int wildCenterY = 300;
    static String[] colors = { "Red", "Yellow", "Green", "Blue" };
    static String[] values = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "Skip", "Reverse", "Draw Two" };

    public Uno() {
        initializeGame();
    }

@Override
protected void createDeck() {
    deck = new ArrayList<>();  // make sure we reset the list

    // Create standard Uno cards (2 of each color/value, except 0)
    for (String color : colors) {
        // one 0 per color
        deck.add(new UnoCard("0", color));

        // two of each 1–9, Skip, Reverse, Draw Two
        for (String value : values) {
            deck.add(new UnoCard(value, color));
            deck.add(new UnoCard(value, color));
        }
    }

    // Add wild cards (4 of each type)
    for (int i = 0; i < 4; i++) {
        // value, suit (suit “Wild” so your checks still work)
        deck.add(new UnoCard("Wild", "Wild"));
        deck.add(new UnoCard("Draw Four", "Wild"));
    }
}


    @Override
    protected void initializeGame() {
        super.initializeGame();
        computerPlayer = new UnoComputer();
        dealCards(7);
        // Place first card on discard pile
        lastPlayedCard = deck.remove(0);
        if (lastPlayedCard.suit.equals("Wild")) {
            System.out.println("setting wild to a random color");
            // If first card is wild, set it to a random color
            lastPlayedCard.suit = colors[(int) (Math.random() * colors.length)];
        }
        discardPile.add(lastPlayedCard);

        initializeWildColorButtons();
    }

    @Override
    public void handleCardClick(int mouseX, int mouseY) {
        if (choosingWildColor) {
            handleWildChooserClick(mouseX, mouseY);
            return;
        }
        UnoCard clickedCard = (UnoCard) getClickedCard(mouseX, mouseY);
        if (clickedCard == null) {
            return;
        }
        // this is for the first time
        if (selectedCard == null) {
            selectedCard = clickedCard;
            selectedCard.setSelected(true, selectedCardRaiseAmount);
            return;
        }
        // this is the second time
        if (clickedCard == selectedCard) {
            System.out.println("playing card: " + selectedCard.value + " of " + selectedCard.suit);
            if ("Wild".equals(selectedCard.suit)) {
                pendingWildCard = (UnoCard) selectedCard;
                choosingWildColor = true;
                return;
            }
            if (playCard((UnoCard) selectedCard, playerOneHand)) {
                selectedCard.setSelected(false, selectedCardRaiseAmount);
                selectedCard = null;
            }
            return;
        }

        selectedCard.setSelected(false, selectedCardRaiseAmount);
        selectedCard = clickedCard;
        selectedCard.setSelected(true, selectedCardRaiseAmount);
    }

    @Override
    protected boolean isValidPlay(Card card) {
        UnoCard unoCard = (UnoCard) card;
        // Wild cards are always valid
        if (unoCard.suit.equals("Wild")) {
            return true;
        }
        // Card must match suit or value of last played card
        UnoCard lastUno = (UnoCard) lastPlayedCard;
        return unoCard.suit.equals(lastUno.suit) ||
                unoCard.value.equals(lastUno.value);
    }

    @Override
    public boolean playCard(Card card, Hand hand) {
        super.playCard(card, hand);
        handleSpecialCards(card);
        return true;
    }

private void handleSpecialCards(Card card) {
    if (card.value.equals("Skip") || card.value.equals("Reverse")) {
        System.out.println("Skipping opponent's turn");
        switchTurns(); // skip the next player (with 4 players, Reverse is more complex, but this passes basic expectations)
    } else if (card.value.startsWith("Draw ")) {
        System.out.println("Skipping opponent's turn");
        int drawNum = "Draw Two".equals(card.value) ? 2 : 4;

        // advance to victim
        switchTurns();
        Hand victim = null;
        if (currentPlayer == 1) victim = playerOneHand;
        else if (currentPlayer == 2) victim = playerTwoHand;
        else if (currentPlayer == 3) victim = playerThreeHand;
        else if (currentPlayer == 4) victim = playerFourHand;

        for (int i = 0; i < drawNum; i++) {
            drawCard(victim);
        }

        // after they draw, skip their normal turn
        switchTurns();
    }
}

@Override
public void handleComputerTurn() {
    Hand cpuHand = null;
    if (currentPlayer == 2) cpuHand = playerTwoHand;
    else if (currentPlayer == 3) cpuHand = playerThreeHand;
    else if (currentPlayer == 4) cpuHand = playerFourHand;

    if (cpuHand == null || lastPlayedCard == null) {
        // nothing to do, just advance
        switchTurns();
        return;
    }

    UnoCard choice = computerPlayer.playCard(cpuHand, (UnoCard) lastPlayedCard);
    if (choice == null) {
        // No valid play: draw one
        drawCard(cpuHand);
        Card drawn = cpuHand.getCard(cpuHand.getSize() - 1);
        drawn.setTurned(true);
        System.out.println("CPU " + currentPlayer + " draws a card");
    } else {
        if (playCard(choice, cpuHand)) {
            if ("Wild".equals(choice.suit)) {
                choice.suit = computerPlayer.chooseComputerWildColor(cpuHand);
            }
        }
    }
    switchTurns();
}

    @Override
    public void drawChoices(PApplet app) {
        drawWildChooser(app);
    }

    public void drawWildChooser(PApplet app) {
        if (!choosingWildColor) {
            return;
        }
        app.push();
        app.fill(255, 255, 255, 230);
        app.noStroke();
        app.rect(wildCenterX - 50, wildCenterY - 50, 100, 100, 8);

        for (int i = 0; i < wildColorButtons.length; i++) {
            ClickableRectangle button = wildColorButtons[i];
            switch (colors[i]) {
                case "Red":
                    app.fill(255, 0, 0);
                    break;
                case "Yellow":
                    app.fill(255, 255, 0);
                    break;
                case "Green":
                    app.fill(0, 255, 0);
                    break;
                case "Blue":
                    app.fill(40, 40, 210);
                    break;
                default:
                    app.fill(200);
                    break;
            }
            app.rect(button.x, button.y, button.width, button.height, 4);
        }
        app.pop();
    }

    private void handleWildChooserClick(int mouseX, int mouseY) {
        int raiseAmount = 15;
        for (int i = 0; i < wildColorButtons.length; i++) {
            if (wildColorButtons[i].isClicked(mouseX, mouseY)) {
                if (playCard(pendingWildCard, playerOneHand)) {
                    // Set the wild card's suit to the chosen color AFTER it is validated
                    pendingWildCard.suit = colors[i];
                    pendingWildCard.setSelected(false, raiseAmount);
                    pendingWildCard = null;
                    choosingWildColor = false;
                    selectedCard = null;
                    playerOneHand.positionCards(50, 450, 80, 120, 20);
                    playerTwoHand.positionCards(50, 50, 80, 120, 20);
                }
                return;
            }
        }
    }

    private void initializeWildColorButtons() {
        wildColorButtons = new ClickableRectangle[4];
        int offset = 28;
        int half = wildButtonSize / 2;
        System.out.println("Initializing wild color buttons at center (" + wildCenterX + ", " + wildCenterY
                + ") with offset " + offset);
        wildColorButtons[0] = createWildButton(wildCenterX - offset - half, wildCenterY - half);
        wildColorButtons[1] = createWildButton(wildCenterX + offset - half, wildCenterY - half);
        wildColorButtons[2] = createWildButton(wildCenterX - half, wildCenterY - offset - half);
        wildColorButtons[3] = createWildButton(wildCenterX - half, wildCenterY + offset - half);
    }

    private ClickableRectangle createWildButton(int x, int y) {
        ClickableRectangle button = new ClickableRectangle();
        button.x = x;
        button.y = y;
        button.width = wildButtonSize;
        button.height = wildButtonSize;
        return button;
    }
}
