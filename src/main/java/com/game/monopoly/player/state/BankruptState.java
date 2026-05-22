package com.game.monopoly.player.state;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;

/**
 * Stan reprezentujący gracza, który zbankrutował.
 * Taki gracz nie bierze już aktywnego udziału w grze.
 */
public class BankruptState implements PlayerState {

    @Override
    public void playTurn(Player player, GameEngine engine) {
        // Informujemy GUI o tym, że tura tego gracza jest pomijana
        engine.notifyMessage(player.getName() + " jest bankrutem i pomija turę.");

        // Od razu kończymy turę, co wewnątrz silnika wywoła przekazanie
        // kolejki do następnego gracza (engine.nextPlayer())
        engine.endTurn();
    }
}