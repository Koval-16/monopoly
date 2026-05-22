package com.game.monopoly.engine;

import java.util.Random;

/**
 * Klasa reprezentująca parę kości do gry w Monopoly.
 * Cyklem życia tego obiektu zarządza GameEngine (Kompozycja).
 */
public class Dice {

    // --- ATRYBUTY ---
    private int roll1;
    private int roll2;

    // Dodane pole: generator liczb losowych niezbędny do rzutów
    private Random random;

    // --- KONSTRUKTOR ---
    public Dice() {
        this.random = new Random();
        this.roll1 = 1;
        this.roll2 = 1;
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Główna metoda rzucająca kośćmi.
     * Nadaje nowe wartości (od 1 do 6) polom roll1 i roll2.
     */
    public void roll() {
        // nextInt(6) generuje liczby od 0 do 5, dlatego dodajemy 1
        this.roll1 = this.random.nextInt(6) + 1;
        this.roll2 = this.random.nextInt(6) + 1;
    }

    /**
     * Zwraca sumę oczek z ostatniego rzutu.
     * Używane m.in. do wyliczania czynszu na Wodociągach oraz przesuwania gracza.
     */
    public int getTotal() {
        return this.roll1 + this.roll2;
    }

    /**
     * Sprawdza, czy na obu kościach wypadła ta sama wartość.
     * Niezbędne do weryfikacji dodatkowego rzutu lub wyjścia z więzienia.
     */
    public boolean isDouble() {
        return this.roll1 == this.roll2;
    }

    public int getRoll1() {
        return this.roll1;
    }

    /**
     * Zwraca wynik drugiej kości (przydatne do poprawnego wyrenderowania grafiki).
     */
    public int getRoll2() {
        return this.roll2;
    }
}