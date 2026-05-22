package com.game.monopoly.board.purchase;

import com.game.monopoly.engine.TurnContext;

public class UtilityField extends PurchaseField {

    private int[] multipliers;

    public UtilityField(String name, int position, int price) {
        super(name, position, price);
        // Oficjalne mnożniki w Monopoly
        this.multipliers = new int[]{4, 10};
    }

    @Override
    protected int calculateRent(TurnContext ctx) {
        // Zabezpieczenie przed błędem, gdyby pole nie miało właściciela
        if (getOwner() == null) {
            return 0;
        }

        int utilityCount = 0;

        // Zliczamy, ile użyteczności (Wodociągi/Elektrownia) posiada ten gracz
        for (PurchaseField property : getOwner().getProperties()) {
            if (property instanceof UtilityField) {
                utilityCount++;
            }
        }

        // Wyliczenie czynszu w oparciu o rzut kośćmi z obecnej tury i mnożnik
        if (utilityCount > 0) {
            // Indeks 0 dla 1 użyteczności (x4), indeks 1 dla 2 (x10)
            int currentMultiplier = (utilityCount == 1) ? this.multipliers[0] : this.multipliers[1];
            return ctx.getDiceTotal() * currentMultiplier;
        }

        return 0;
    }
}