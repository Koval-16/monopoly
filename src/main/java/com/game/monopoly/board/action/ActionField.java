package com.game.monopoly.board.action;

import com.game.monopoly.board.Field;

/**
 * Klasa abstrakcyjna grupująca wszystkie pola na planszy,
 * które nie są nieruchomościami (Start, Szansa, Podatki, Więzienie).
 */
public abstract class ActionField extends Field {

    public ActionField(String name, int position) {
        // Przekazanie danych do głównej klasy Field
        super(name, position);
    }
}