package com.game.monopoly.board.purchase;

import com.game.monopoly.board.Field;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

/**
 * Klasa abstrakcyjna reprezentująca wszystkie pola, które gracze mogą kupić.
 * Implementuje Wzorzec Metody Szablonowej (Template Method) dla operacji onLand.
 */
public abstract class PurchaseField extends Field {

    // --- ATRYBUTY ---
    private int price;
    private Player owner;
    private boolean isMortgaged;

    // --- KONSTRUKTOR ---
    public PurchaseField(String name, int position, int price) {
        super(name, position);
        this.price = price;
        this.owner = null; // Na początku pole nie ma właściciela
        this.isMortgaged = false;
    }

    // --- FUNKCJE (Metody) ---

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        if (owner == null) {
            // Pole jest wolne - informujemy system o możliwości zakupu
            engine.notifyMessage(player.getName() + " staje na wolnym polu " + getName() + ". Możliwość zakupu za " + price + "$.");
        } else if (owner != player && !isMortgaged) {
            // Logika płatności - pole zajęte, wylicz czynsz polimorficznie
            int rent = calculateRent(ctx);
            int finalRent = ctx.applyMultiplier(rent);

            engine.notifyMessage(player.getName() + " płaci czynsz w wysokości " + finalRent + "$ graczowi " + owner.getName() + " za postój na " + getName() + ".");

            player.payMoney(finalRent);
            owner.addMoney(finalRent);
        } else if (isMortgaged) {
            engine.notifyMessage("Pole " + getName() + " jest zastawione. " + player.getName() + " nie płaci czynszu.");
        }
    }

    /**
     * Metoda abstrakcyjna (hak/krok szablonu).
     * Każda podklasa wylicza czynsz w zupełnie inny sposób.
     */
    protected abstract int calculateRent(TurnContext ctx);

    public void buy(Player player) {
        if (this.owner == null && player.getBalance() >= this.price) {
            player.payMoney(this.price);
            this.owner = player;
            player.addProperty(this);
        }
    }

    public void mortgage() {
        if (!this.isMortgaged && this.owner != null) {
            this.isMortgaged = true;
            this.owner.addMoney(this.price / 2);
        }
    }

    public void unmortgage() {
        if (this.isMortgaged && this.owner != null) {
            int mortgageValue = this.price / 2;
            int unmortgageCost = mortgageValue + (int)(mortgageValue * 0.1);

            if (this.owner.getBalance() >= unmortgageCost) {
                this.owner.payMoney(unmortgageCost);
                this.isMortgaged = false;
            }
        }
    }

    public boolean canBeMortgaged() {
        return !this.isMortgaged; // Zwykłe pole można zastawić, jeśli nie jest zastawione
    }

    // W PurchaseField.java:
    public boolean isBuildableMonopoly() {
        return false;
    }

    // W PurchaseField:
    public int getMortgageValue() {
        return this.price / 2;
    }

    // W PurchaseField:
    public boolean canBeUnmortgagedBy(Player player) {
        return this.isMortgaged() && player.getBalance() >= this.getUnmortgageCost();
    }

    public boolean isOwned() {
        return this.owner != null;
    }

    public boolean canBeBoughtBy(Player player) {
        return !this.isOwned() && player.getBalance() >= this.price;
    }

    public boolean isStreet(){return false;}

    @Override
    public boolean isPurchasable() { return true; }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player newOwner) {
        this.owner = newOwner;
    }

    public int getPrice() {
        return price;
    }

    public boolean isMortgaged() {
        return isMortgaged;
    }

    public int getUnmortgageCost() {
        return (this.price / 2) + (int)((this.price / 2) * 0.1);
    }
}