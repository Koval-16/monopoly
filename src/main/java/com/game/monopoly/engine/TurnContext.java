package com.game.monopoly.engine;

/**
 * Klasa reprezentująca kontekst trwającej tury.
 * Obiekt ten jest przekazywany w parametrach do metod pól (np. onLand),
 * aby dostarczyć im informacji o rzucie kostką i modyfikatorach.
 */
public class TurnContext {

    // --- ATRYBUTY ---
    private int diceTotal;
    private int rentMultiplier;

    // --- KONSTRUKTOR ---
    public TurnContext(Dice dice) {
        if (dice != null) {
            this.diceTotal = dice.getTotal();
        } else {
            this.diceTotal = 0;
        }
        this.rentMultiplier = 1; // Domyślna wartość mnożnika (standardowy czynsz)
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Zwraca sumę oczek wyrzuconych na kościach w obecnej turze.
     */
    public int getDiceTotal() {
        return this.diceTotal;
    }
    /**
     * Oblicza finalny czynsz na podstawie kwoty bazowej i obecnego mnożnika.
     */
    public int applyMultiplier(int base) {
        return base * this.rentMultiplier;
    }

    public void setRentMultiplier(int rentMultiplier) {
        if (rentMultiplier > 0) {
            this.rentMultiplier = rentMultiplier;
        }
    }
}