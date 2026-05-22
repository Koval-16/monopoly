package com.game.monopoly.economy;

import com.game.monopoly.player.Player;

/**
 * Klasa reprezentująca Bank w grze Monopoly.
 * Zarządza globalnymi zasobami (domki, hotele) oraz ułatwia transfery gotówkowe.
 * Cyklem życia tego obiektu zarządza GameEngine.
 */
public class Bank {

    // --- ATRYBUTY ---
    private int availableHouses;
    private int availableHotels;

    // --- KONSTRUKTOR ---
    public Bank() {
        // Zgodnie z oficjalnymi zasadami Monopoly
        this.availableHouses = 32;
        this.availableHotels = 12;
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Bank pobiera pieniądze od gracza (np. opłata za podatek lub zakup pola).
     */
    public void receiveMoney(Player player, int amount) {
        if (player != null && amount > 0) {
            player.payMoney(amount);
        }
    }

    /**
     * Bank wypłaca pieniądze graczowi (np. nagroda za przejście przez Start).
     */
    public void payOutMoney(Player player, int amount) {
        if (player != null && amount > 0) {
            player.addMoney(amount);
        }
    }

    /**
     * Próbuje pobrać jeden domek z puli banku przy jego budowie.
     * @return true jeśli domek był dostępny i został pobrany, false jeśli pula jest pusta.
     */
    public boolean requestHouse() {
        if (this.availableHouses > 0) {
            this.availableHouses--;
            return true;
        }
        return false;
    }

    /**
     * Próbuje pobrać jeden hotel z puli banku przy jego budowie.
     * @return true jeśli hotel był dostępny i został pobrany, false w przeciwnym razie.
     */
    public boolean requestHotel() {
        if (this.availableHotels > 0) {
            this.availableHotels--;
            return true;
        }
        return false;
    }

    /**
     * Odkłada jeden domek z powrotem do puli banku (np. przy sprzedaży budynków).
     */
    public void returnHouse() {
        if (this.availableHouses < 32) {
            this.availableHouses++;
        }
    }

    /**
     * Odkłada jeden hotel z powrotem do puli banku.
     */
    public void returnHotel() {
        if (this.availableHotels < 12) {
            this.availableHotels++;
        }
    }

    public int getAvailableHouses() {
        return this.availableHouses;
    }

    public int getAvailableHotels() {
        return this.availableHotels;
    }
}