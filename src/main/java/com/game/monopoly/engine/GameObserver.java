package com.game.monopoly.engine;

import com.game.monopoly.player.Player;
import com.game.monopoly.economy.TradeOffer;

/**
 * Interfejs wzorca Obserwator.
 * Zapewnia asynchroniczną i bezpieczną komunikację między logiką gry (silnikiem)
 * a interfejsem graficznym (GUI), nie tworząc twardych zależności.
 */
public interface GameObserver {

    // --- FUNKCJE (Metody kontraktu) ---

    /**
     * Wysyła ogólną, tekstową informację do wyświetlenia w logach gry (np. "Gracz 1 rzucił 7").
     */
    void onMessage(String message);

    /**
     * Powiadamia interfejs o konieczności przerysowania pionka gracza na nowym polu.
     */
    void onPlayerMoved(Player player, int newPosition);

    /**
     * Informuje GUI o zmianie stanu gracza (przydatne do nałożenia ikonki np. krat więzienia lub bankructwa).
     */
    void onPlayerStateChanged(Player player);

    /**
     * Wywoływana, gdy jeden z graczy utworzy i zatwierdzi poprawną ofertę wymiany.
     * GUI powinno wtedy wyświetlić okienko decyzyjne dla gracza docelowego.
     */
    void onTradeRequested(TradeOffer offer);
}