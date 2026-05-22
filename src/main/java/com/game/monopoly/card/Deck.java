package com.game.monopoly.card;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Klasa reprezentująca talię kart w grze (Szansa lub Kasa Społeczna).
 * Zarządza zbiorem kart (Agregacja), pozwalając na ich tasowanie, dobieranie i odkładanie.
 */
public class Deck {

    // --- ATRYBUTY ---
    private List<Card> cards;
    private String deckType; // np. "Chance" lub "Community Chest"

    // --- KONSTRUKTOR ---
    public Deck(String deckType) {
        this.deckType = deckType;
        // LinkedList jest idealna do operacji pobierania z początku i dodawania na koniec
        this.cards = new LinkedList<>();
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Tasuje wszystkie karty obecne aktualnie w talii.
     */
    public void shuffle() {
        // Używamy gotowej metody z biblioteki standardowej Javy
        if (cards != null && !cards.isEmpty()) {
            Collections.shuffle(this.cards);
        }
    }

    /**
     * Pobiera kartę z samej góry talii (początek listy) i usuwa ją ze stosu.
     * @return Zwraca pobraną kartę lub null, jeśli talia jest pusta.
     */
    public Card drawCard() {
        if (this.cards != null && !this.cards.isEmpty()) {
            // remove(0) zwraca element i jednocześnie usuwa go z listy
            return this.cards.remove(0);
        }
        return null;
    }

    /**
     * Odkłada zużytą kartę na sam spód talii (koniec listy).
     */
    public void putCardAtBottom(Card card) {
        if (card != null) {
            // add() domyślnie dodaje na sam koniec listy
            this.cards.add(card);
        }
    }

    public void addCard(Card card) {
        if (card != null) {
            this.cards.add(card);
        }
    }

    public String getDeckType() {
        return this.deckType;
    }
}