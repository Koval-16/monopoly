package com.game.monopoly.board.action;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class JailField extends ActionField {

    public JailField(String name, int position) {
        super(name, position);
    }

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        // Zwykłe "Odwiedziny". Gracz jest bezpieczny, nie trafia do więzienia i nie traci kolejki.
        engine.notifyMessage(player.getName() + " ląduje na polu Więzienia, ale to 'Tylko odwiedziny'. Nic się nie dzieje.");
    }
}