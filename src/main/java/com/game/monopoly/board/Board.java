package com.game.monopoly.board;

import java.util.List;
import java.util.ArrayList;

/**
 * Klasa reprezentująca planszę do gry.
 * Zarządza kolekcją wszystkich pól (kompozycja) i udostępnia je silnikowi oraz graczom.
 */
public class Board {

    // --- ATRYBUTY ---
    private List<Field> fields;

    // --- KONSTRUKTOR ---
    public Board() {
        // Zgodnie z relacją (przerywana strzałka do BoardFactory),
        // Plansza zleca Fabryce utworzenie i skonfigurowanie wszystkich 40 pól.
        this.fields = BoardFactory.createBoard();

        // Zabezpieczenie na wypadek, gdyby fabryka jeszcze nie była zaimplementowana
        if (this.fields == null) {
            this.fields = new ArrayList<>();
        }
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Zwraca obiekt pola znajdujący się pod wskazanym indeksem.
     * Automatycznie obsługuje zapętlenie planszy dzięki operatorowi reszty z dzielenia (modulo).
     * * @param pos Oczekiwana pozycja gracza (np. wynik z rzutu kośćmi)
     * @return Obiekt Field na danej pozycji
     */
    public Field getField(int pos) {
        if (fields.isEmpty()) {
            return null; // Zabezpieczenie przed pustą planszą
        }

        // Jeśli pos wynosi np. 42, a getSize() to 40, wynikiem będzie indeks 2.
        int wrappedPosition = pos % getSize();
        return fields.get(wrappedPosition);
    }

    public List<Field> getFields() {
        return fields;
    }

    /**
     * Zwraca całkowity rozmiar planszy (w standardowym Monopoly wynosi on 40).
     */
    public int getSize() {
        return fields.size();
    }
}