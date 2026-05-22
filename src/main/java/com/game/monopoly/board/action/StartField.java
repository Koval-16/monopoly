package com.game.monopoly.board.action;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class StartField extends ActionField {

    public StartField(String name, int position) {
        super(name, position);
    }

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        // Premia w wysokości 200$ została już przyznana w logice ruchu (np. ActiveState),
        // ponieważ nowa pozycja (0) jest mniejsza od poprzedniej.
        // Tutaj jedynie wysyłamy powiadomienie do GUI o idealnym lądowaniu.

        engine.notifyMessage(player.getName() + " zatrzymuje się idealnie na polu Start!");
    }
}