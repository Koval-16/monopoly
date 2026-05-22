package com.game.monopoly.board.action;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class ParkingField extends ActionField {

    public ParkingField(String name, int position) {
        super(name, position);
    }

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        // Zgodnie z oficjalnymi zasadami Monopoly, Bezpłatny Parking to pole całkowicie neutralne.
        // Gracz nie płaci kar, nie ciągnie kart i nie traci kolejki.
        engine.notifyMessage(player.getName() + " zatrzymuje się na Bezpłatnym Parkingu. Chwila relaksu, nic się nie dzieje!");
    }
}