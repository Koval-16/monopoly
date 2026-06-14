package com.game.monopoly.board.action;

import com.game.monopoly.card.Card;
import com.game.monopoly.card.Deck;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class CardField extends ActionField {

    // Typ talii, z której pole ma ciągnąć karty (np. "Szansa", "Kasa Społeczna")
    private String deckType;

    public CardField(String name, int position, String deckType) {
        super(name, position);
        this.deckType = deckType;
    }

    // POTRZEBNE DLA GUI DO OKIENKA ALERT:
    public String getDeckType() {
        return deckType;
    }

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        // 1. ZMIANA: Wejście na pole nie wywołuje już automatycznie akcji karty!
        // Silnik jedynie informuje, że gracz stoi na polu i musi dobrać kartę, a resztą steruje GUI (przycisk "Dobierz Kartę").
        engine.notifyMessage(player.getName() + " staje na polu " + this.deckType + " i musi dobrać kartę.");
    }

    // 2. NOWA METODA: Ręczne wyciągnięcie karty, wywoływane przez GameController po kliknięciu "Dobierz Kartę"
    public Card drawCard(GameEngine engine) {
        Deck deck = getDeck(engine);
        if (deck != null) {
            return deck.drawCard();
        }
        return null;
    }

    // 3. NOWA METODA: Odłożenie karty na spód talii po jej rozpatrzeniu i kliknięciu "OK" w GUI
    public void returnCardToDeck(Card card, GameEngine engine) {
        Deck deck = getDeck(engine);
        // Sprawdzamy czy karta wraca do talii (np. karta "Wyjdziesz z więzienia" mogłaby nie wracać)
        if (deck != null && card != null && card.shouldReturnToDeck()) {
            deck.putCardAtBottom(card);
        }
    }

    // Metoda pomocnicza ukrywająca logikę wyboru talii
    private Deck getDeck(GameEngine engine) {
        if ("Szansa".equals(this.deckType)) {
            return engine.getChanceDeck();
        } else if ("Kasa Społeczna".equals(this.deckType)) {
            return engine.getCommunityChestDeck();
        }
        return null;
    }
}