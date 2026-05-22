package com.game.monopoly.board.action;

import com.game.monopoly.card.Card;
import com.game.monopoly.card.Deck;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class CardField extends ActionField {

    // Typ talii, z której pole ma ciągnąć karty (np. "Chance")
    private String deckType;

    public CardField(String name, int position, String deckType) {
        super(name, position);
        this.deckType = deckType;
    }

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        Deck deck = null;

        // 1. Pobranie odpowiedniej talii z silnika na podstawie deckType
        if ("Szansa".equals(this.deckType)) {
            deck = engine.getChanceDeck();
        } else if ("Kasa Społeczna".equals(this.deckType)) {
            deck = engine.getCommunityChestDeck();
        }

        if (deck != null) {
            // 2. Wyciągnięcie karty z wierzchu talii
            Card card = deck.drawCard();

            if (card != null) {
                // Informujemy system o wyciągniętej karcie
                engine.notifyMessage(player.getName() + " ciągnie kartę [" + this.deckType + "]: " + card.getDescription());

                // 3. Wykonanie logiki ukrytej w karcie (Wzorzec Polecenie)
                card.executeAction(player, engine);

                // 4. Odłożenie karty na spód talii
                // UWAGA: Logika kart typu "Wyjdziesz z więzienia" może wymagać późniejszego
                // dodania w klasie Deck metody sprawdzającej, czy karta ma zostać w ręce gracza.
                if (card.shouldReturnToDeck()) {
                    deck.putCardAtBottom(card);
                }
            }
        }
    }
}