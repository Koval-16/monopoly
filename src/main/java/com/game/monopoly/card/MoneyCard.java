package com.game.monopoly.card;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;

public class MoneyCard extends Card {
    private int amount; // Wartość dodatnia (nagroda) lub ujemna (kara)

    public MoneyCard(String description, int amount) {
        super(description);
        this.amount = amount;
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " wyciąga kartę: " + getDescription());

        if (this.amount > 0) {
            engine.notifyMessage("Bank wypłaca graczowi " + player.getName() + " kwotę: " + this.amount + "$");
            engine.getBank().payOutMoney(player, this.amount);
        } else if (this.amount < 0) {
            int penalty = Math.abs(this.amount);
            engine.notifyMessage(player.getName() + " musi zapłacić do banku: " + penalty + "$");
            engine.getBank().receiveMoney(player, penalty);
        }
    }
}