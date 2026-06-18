package com.game.monopoly.board;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

/**
 * Abstrakcyjna klasa bazowa reprezentująca pojedyncze pole na planszy Monopoly.
 * Definiuje wspólne cechy (nazwa, pozycja) oraz polimorficzną metodę onLand.
 */
public abstract class Field {

    // --- ATRYBUTY ---
    private String name;
    private int position; // Indeks pola na planszy (od 0 do 39)

    // --- KONSTRUKTOR ---
    public Field(String name, int position) {
        this.name = name;
        this.position = position;
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Zwraca pozycję (indeks) tego pola na planszy.
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Zwraca nazwę pola (dodane, aby silnik i GUI mogły identyfikować pole tekstowo).
     */
    public String getName() {
        return this.name;
    }

    /**
     * Główna metoda polimorficzna. Wywoływana przez GameEngine, gdy gracz kończy ruch na tym polu.
     * Każde pole nadpisze tę metodę, aby wykonać specyficzną dla siebie akcję.
     * * @param player Gracz, który stanął na polu.
     * @param ctx Kontekst obecnej tury (np. mnożniki, rzut kośćmi).
     * @param engine Silnik gry dający dostęp do globalnych zasobów (Bank, Deck itp.).
     */
    public abstract void onLand(Player player, TurnContext ctx, GameEngine engine);

    // W klasie bazowej Field.java:
    public boolean isPurchasable() { return false; }
    public boolean isCardField() { return false; }

}