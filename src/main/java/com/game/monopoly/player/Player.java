package com.game.monopoly.player;

import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.state.PlayerState;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca gracza w grze Monopoly.
 * Przechowuje jego zasoby, pozycję oraz obecny stan maszyny stanów.
 */
public class Player {

    // --- ATRYBUTY ---
    private String name;
    private int balance;
    private int position;
    private PlayerState currentState;
    private List<PurchaseField> properties;
    private int getOutOfJailCards;

    // --- KONSTRUKTOR ---
    public Player(String name) {
        this.name = name;
        this.balance = 1500; // Standardowa kwota startowa w Monopoly
        this.position = 0;   // Startowy indeks pola (Start)
        this.properties = new ArrayList<>();
        this.getOutOfJailCards = 0;
        // UWAGA: Inicjalizację domyślnego stanu (np. ActiveState)
        // warto przeprowadzić po stworzeniu instancji stanów, aby uniknąć problemów.
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Główna metoda tury gracza. Zamiast instrukcji IF, deleguje wykonanie
     * całej logiki do obecnie ustawionego obiektu PlayerState.
     */
    public void executeTurn(GameEngine engine) {
        if (currentState != null) {
            currentState.playTurn(this, engine);
        }
    }

    /**
     * Zmienia stan gracza (np. z ActiveState na InJailState lub BankruptState).
     */
    public void changeState(PlayerState newState) {
        this.currentState = newState;
    }

    /**
     * Dodaje podaną kwotę do konta gracza (np. po przejściu przez Start).
     */
    public void addMoney(int amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }

    /**
     * Odejmuje podaną kwotę z konta gracza (np. opłata czynszu, podatek).
     */
    public void payMoney(int amount) {
        if (amount > 0) {
            this.balance -= amount;
        }
    }

    /**
     * Przesuwa gracza o określoną liczbę pól do przodu (z uwzględnieniem zapętlenia planszy).
     */
    public void move(int stepCount) {
        // Obliczenie nowej pozycji (40 to standardowy rozmiar planszy)
        this.position = (this.position + stepCount) % 40;
    }

    /**
     * Przenosi gracza bezpośrednio na konkretne pole (np. wskutek działania Karty "Idziesz do więzienia").
     */
    public void setPosition(int position) {
        this.position = position;
    }

    public void setBalance(int balance){
        this.balance = balance;
    }

    public void addProperty(PurchaseField property) {
        if (property != null && !this.properties.contains(property)) {
            this.properties.add(property);
        }
    }

    public void removeProperty(PurchaseField property) {
        this.properties.remove(property);
    }

    public void addOutOfJailCards(int count) {
        if (count > 0) {
            this.getOutOfJailCards += count;
        }
    }

    public void removeOutOfJailCards(int count) {
        if (count > 0 && this.getOutOfJailCards >= count) {
            this.getOutOfJailCards -= count;
        }
    }

    /**
     * Zwraca obecny indeks pola, na którym stoi gracz.
     */
    public int getPosition() {
        return this.position;
    }

    public String getName(){
        return this.name;
    }

    public int getBalance() {
        return this.balance;
    }

    public int getOutOfJailCards() {
        return this.getOutOfJailCards;
    }

    public List<PurchaseField> getProperties() {
        return this.properties;
    }

    public PlayerState getCurrentState() {
        return this.currentState;
    }
}