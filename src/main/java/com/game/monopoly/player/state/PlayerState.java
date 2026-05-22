package com.game.monopoly.player.state;

import com.game.monopoly.player.Player;
import com.game.monopoly.engine.GameEngine;

/**
 * Interfejs wzorca Stan (State) dla obiektu Gracza.
 * Definiuje kontrakt, który muszą spełnić wszystkie możliwe stany gracza
 * (np. bycie w grze, w więzieniu, lub bycie bankrutem).
 */
public interface PlayerState {

    // --- FUNKCJE (Metody kontraktu) ---

    /**
     * Główna metoda definiująca przebieg tury gracza w danym stanie.
     * * @param player Obiekt gracza, którego dotyczy tura (kontekst wzorca State).
     * @param engine Silnik gry, pozwalający stanowi np. na rzut kośćmi czy wywołanie kolejnego gracza.
     */
    void playTurn(Player player, GameEngine engine);

}