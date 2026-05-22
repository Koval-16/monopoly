package com.game.monopoly.board.purchase;

import com.game.monopoly.engine.TurnContext;

public class RailroadField extends PurchaseField {

    private int baseRent;

    public RailroadField(String name, int position, int price) {
        super(name, position, price);
        this.baseRent = 25; // Standardowy bazowy czynsz za jeden dworzec w Monopoly
    }

    @Override
    protected int calculateRent(TurnContext ctx) {
        // Zabezpieczenie przed błędem, gdyby z jakiegoś powodu pole nie miało właściciela
        if (getOwner() == null) {
            return 0;
        }

        int railroadCount = 0;

        // Iterujemy po wszystkich posiadłościach gracza, który jest właścicielem tego dworca
        for (PurchaseField property : getOwner().getProperties()) {
            if (property instanceof RailroadField) {
                railroadCount++;
            }
        }

        // Skalowanie potęgowe: 1 -> 25, 2 -> 50, 3 -> 100, 4 -> 200
        if (railroadCount > 0) {
            return this.baseRent * (int) Math.pow(2, railroadCount - 1);
        }

        return 0;
    }
}