package com.game.monopoly.card;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;

/**
 * Abstrakcyjna klasa bazowa dla wszystkich kart w grze (Wzorzec Polecenia / Command).
 * Każda konkretna karta będzie dziedziczyć po tej klasie i implementować
 * własną logikę w metodzie executeAction.
 */
public abstract class Card {

    // --- ATRYBUTY ---
    private String description;

    // --- KONSTRUKTOR ---
    public Card(String description) {
        this.description = description;
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Zwraca tekstowy opis akcji przypisanej do karty (do wyświetlenia w GUI).
     */
    public String getDescription() {
        return this.description;
    }

    public boolean shouldReturnToDeck(){
        return true;
    }

    /**
     * Główna metoda wzorca Polecenia.
     * Jej implementacja zależy od konkretnej podklasy (np. dodanie pieniędzy, ruch na pole).
     * * @param player Gracz, który wyciągnął kartę i na którego działa jej efekt.
     * @param engine Główny silnik gry, zapewniający dostęp do banku, planszy itp.
     */
    public abstract void executeAction(Player player, GameEngine engine);

}