package com.game.monopoly.board.action;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class TaxField extends ActionField {

    // Dodane pole: kwota podatku
    private int taxAmount;

    public TaxField(String name, int position, int taxAmount) {
        super(name, position);
        this.taxAmount = taxAmount;
    }

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        // Informujemy gracza o konieczności zapłaty
        engine.notifyMessage(player.getName() + " ląduje na polu '" + getName() + "' i musi zapłacić " + this.taxAmount + "$ podatku.");

        // Pobrana kwota trafia do banku (Bank automatycznie potrąca środki z konta gracza)
        engine.getBank().receiveMoney(player, this.taxAmount);
    }
}