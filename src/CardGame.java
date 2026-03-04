import java.util.ArrayList;
import java.util.Collections;

import processing.core.PApplet;

public class CardGame {
    // Core game components
    ArrayList<Card> deck = new ArrayList<>();
    Hand playerOneHand;
    Hand playerTwoHand;
    Hand playerThreeHand;
    Hand playerFourHand;
    ArrayList<Card> discardPile = new ArrayList<>();
    Card selectedCard;
    int selectedCardRaiseAmount = 15;

    // Game state
    boolean playerOneTurn = true;
    Card lastPlayedCard;
    boolean gameActive;

    // UI
    ClickableRectangle drawButton;
    int drawButtonX = 250;
    int drawButtonY = 400;
    int drawButtonWidth = 100;
    int drawButtonHeight = 35;

    public CardGame() {
        initializeGame();
        dealCards(6);
    }

    protected void initializeGame() {
        // Initialize draw button
        drawButton = new ClickableRectangle();
        drawButton.x = drawButtonX;
        drawButton.y = drawButtonY;
        drawButton.width = drawButtonWidth;
        drawButton.height = drawButtonHeight;

        // Initialize decks and hands
        deck = new ArrayList<>();
        discardPile = new ArrayList<>();
        playerOneHand = new Hand();
        playerTwoHand = new Hand();
        playerThreeHand = new Hand();
        playerFourHand = new Hand();
        gameActive = true;

        createDeck();
    }

    protected void createDeck() {
        // Create a standard deck of cards (for simplicity, using numbers and suits)
        String[] suits = { "Hearts", "Diamonds", "Clubs", "Spades" };
        String[] values = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
        for (String suit : suits) {
            for (String value : values) {
                deck.add(new Card(value, suit));
            }
        }
    }


        protected void dealCards(int numCards) {
    Collections.shuffle(deck);

    for (int i = 0; i < numCards; i++) {
        // Player 1 (bottom)
        playerOneHand.addCard(deck.remove(0));

        // Player 2 (top)
        Card card2 = deck.remove(0);
        card2.setTurned(true);
        playerTwoHand.addCard(card2);

        // Player 3 (left)
        Card card3 = deck.remove(0);
        card3.setTurned(true);
        playerThreeHand.addCard(card3);

        // Player 4 (right)
        Card card4 = deck.remove(0);
        card4.setTurned(true);
        playerFourHand.addCard(card4);
    }

//Arrangement of hands
playerOneHand.positionCards(120, 520, 70, 110, 20);
//top
playerTwoHand.positionCards(120, 40, 70, 110, 20);
// Left (CPU 3)
positionVerticalHand(playerThreeHand, 40, 140, 70, 110, 25);
// Right (CPU 4)
positionVerticalHand(playerFourHand, 540, 140, 70, 110, 25);

}


    // Position a hand vertically (for left and right players)
    protected void positionVerticalHand(Hand hand, int startX, int startY, int cardWidth, int cardHeight, int spacing) {
        for (int i = 0; i < hand.getSize(); i++) {
            int y = startY + (i * spacing);
            Card c = hand.getCard(i);
            if (c != null) {
                c.setPosition(startX, y, cardWidth, cardHeight);
            }
        }
    }


    protected boolean isValidPlay(Card card) {
        return true;
    }

    public void drawCard(Hand hand) {
        if (deck != null && !deck.isEmpty()) {
            hand.addCard(deck.remove(0));
        } else if (discardPile != null && discardPile.size() > 1) {
            // Reshuffle discard pile into deck if deck is empty
            lastPlayedCard = discardPile.remove(discardPile.size() - 1);
            deck.addAll(discardPile);
            discardPile.clear();
            discardPile.add(lastPlayedCard);
            Collections.shuffle(deck);

            if (!deck.isEmpty()) {
                hand.addCard(deck.remove(0));
            }
        }
    }

    public void handleDrawButtonClick(int mouseX, int mouseY) {
        if (drawButton.isClicked(mouseX, mouseY) && playerOneTurn) {
            drawCard(playerOneHand);
            // Switch turns after drawing
            switchTurns();
            
        }
    }

    public boolean playCard(Card card, Hand hand) {
        // Check if card is valid to play
        if (!isValidPlay(card)) {
            System.out.println("Invalid play: " + card.value + " of " + card.suit);
            return false;
        }
        // Remove card from hand
        hand.removeCard(card);
        card.setTurned(false);
        // Add to discard pile
        discardPile.add(card);
        lastPlayedCard = card;
        // Switch turns
        switchTurns();
        return true;
    }
int currentPlayer = 1; // 1 = human, 2–4 = CPU

public void switchTurns() {
    currentPlayer++;
    if (currentPlayer > 4) currentPlayer = 1; // loop back

    // reposition after each turn
    playerOneHand.positionCards(100, 520, 80, 120, 20);
    playerTwoHand.positionCards(100, 40, 80, 120, 20);
    positionVerticalHand(playerThreeHand, 40, 100, 80, 120, 25);
    positionVerticalHand(playerFourHand, 520, 100, 80, 120, 25);
}
public String getCurrentPlayer() {
    switch (currentPlayer) {
        case 1: return "Player One";
        case 2: return "Player Two";
        case 3: return "Player Three";
        case 4: return "Player Four";
        default: return "Unknown";
    }
}

    // public String getCurrentPlayer() {
    //     return playerOneTurn ? "Player One" : "Player Two";
    // }

    public Card getLastPlayedCard() {
        return lastPlayedCard;
    }

    public int getDeckSize() {
        return deck != null ? deck.size() : 0;
    }

    public Hand getPlayerOneHand() {
        return playerOneHand;
    }

    public Hand getPlayerTwoHand() {
        return playerTwoHand;
    }

    public void handleComputerTurn() {
    Hand cpuHand = null;
    if (currentPlayer == 2) cpuHand = playerTwoHand;
    else if (currentPlayer == 3) cpuHand = playerThreeHand;
    else if (currentPlayer == 4) cpuHand = playerFourHand;

    if (cpuHand != null) {
        drawCard(cpuHand);
        System.out.println("CPU " + currentPlayer + " draws a card");
        switchTurns();
    }
}

    public void handleCardClick(int mouseX, int mouseY) {
        if (!playerOneTurn) {
            return;
        }
        Card clickedCard = getClickedCard(mouseX, mouseY);
        if (clickedCard == null) {
            return;
        }
        // this is for the first time
        if (selectedCard == null) {
            selectedCard = clickedCard;
            selectedCard.setSelected(true, selectedCardRaiseAmount);
            return;
        }

        if (selectedCard == clickedCard) {
            System.out.println("playing card: " + selectedCard.value + " of " + selectedCard.suit);
            if (playCard(selectedCard, playerOneHand)) {
                selectedCard.setSelected(false, selectedCardRaiseAmount);
                selectedCard = null;
            }
            return;
        }
        // change selection
        selectedCard.setSelected(false, selectedCardRaiseAmount);
        selectedCard = clickedCard;
        selectedCard.setSelected(true, selectedCardRaiseAmount);
    }

    // return the card that is clicked!
    public Card getClickedCard(int mouseX, int mouseY) {
        for (int i = playerOneHand.getSize() - 1; i >= 0; i--) {
            Card card = playerOneHand.getCard(i);
            if (card != null && card.isClicked(mouseX, mouseY)) {
                return card;
            }
        }
        return null;
    }

    public void drawChoices(PApplet app) {
        // this method is available for overriding
        // if you want to draw additional things (like Uno's wild color choices)
    }
}
