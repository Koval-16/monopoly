package com.game.monopoly.card;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;
import com.game.monopoly.player.state.InJailState;

public class GoToJailCard extends Card {

    public GoToJailCard(String description) {
        super(description);
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " czyta kartę: " + getDescription());
        engine.notifyMessage("Natychmiastowe aresztowanie! " + player.getName() + " trafia do więzienia.");

        // 1. Zmiana pozycji gracza na indeks więzienia (10)
        player.setPosition(10);
        engine.notifyPlayerMoved(player, 10);

        // 2. Zmiana stanu gracza na uwięzionego
        player.changeState(new InJailState());
        engine.notifyPlayerStateChanged(player);

        // Tura gracza kończy się w tym miejscu, co jest obsługiwane
        // przez główny obieg silnika lub ActiveState po zauważeniu zmiany stanu.
    }
}