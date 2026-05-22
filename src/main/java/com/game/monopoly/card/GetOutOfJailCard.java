package com.game.monopoly.card;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;

public class GetOutOfJailCard extends Card {

    public GetOutOfJailCard(String description) {
        super(description);
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " otrzymuje kartę: " + getDescription());
        engine.notifyMessage("Karta zostaje zapisana na koncie gracza. Można jej użyć, aby opuścić areszt bez opłat.");

        // Wywołujemy na obiekcie gracza metodę zwiększającą licznik posiadanych kart wyjścia z więzienia
        player.addOutOfJailCards(1);
    }

    @Override
    public boolean shouldReturnToDeck(){
        return false;
    }
}