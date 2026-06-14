package com.game.monopoly.player.state;

import com.game.monopoly.board.Field;
import com.game.monopoly.engine.Dice;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

/**
 * Stan reprezentujący aktywnego gracza, który normalnie porusza się po planszy.
 */
public class ActiveState implements PlayerState {

    private int doublesCount = 0;

    @Override
    public void playTurn(Player player, GameEngine engine) {
        Dice dice = engine.getDice();

        // 1. Rzut kośćmi
        dice.roll();
        engine.notifyMessage(player.getName() + " rzuca kośćmi: " + dice.getRoll1() + " i " + dice.getRoll2());

        // 2. Sprawdzenie zasady 3 dubletów -> wysłanie do więzienia
        if (dice.isDouble()) {
            doublesCount++;
            if (doublesCount == 3) {
                engine.notifyMessage("Trzy dublety z rzędu! " + player.getName() + " trafia prosto do więzienia.");
                player.setPosition(10); // 10 to domyślny indeks Więzienia w Monopoly
                player.changeState(new InJailState());
                engine.notifyPlayerStateChanged(player);
                engine.notifyPlayerMoved(player, 10);

                // Tutaj zostawiamy endTurn(), bo gracz w więzieniu i tak nie może nic więcej zrobić
                engine.endTurn();
                return; // Natychmiastowe przerwanie tury
            }
        } else {
            // Reset licznika, jeśli rzut nie był dubletem
            doublesCount = 0;
        }

        // 3. Utworzenie obiektu TurnContext
        TurnContext ctx = new TurnContext(dice);

        // 4. Przesunięcie gracza
        int oldPosition = player.getPosition();
        player.move(dice.getTotal());
        int newPosition = player.getPosition();

        engine.notifyPlayerMoved(player, newPosition);

        // Zabezpieczenie: premia za przejście przez Start
        if (newPosition < oldPosition) {
            engine.notifyMessage(player.getName() + " przechodzi przez linię Start. Otrzymuje 200$.");
            player.addMoney(200);
        }

        // 5. Pobranie pola z planszy i wywołanie na nim metody onLand(...)
        Field currentField = engine.getBoard().getField(newPosition);
        engine.notifyMessage(player.getName() + " zatrzymuje się na polu: " + currentField.getName());

        // currentField.onLand(player, ctx, engine);

        // 6. GUI TERAZ PRZEJMUJE KONTROLĘ
        // Zamiast wywoływać playTurn() lub endTurn(), po prostu informujemy GUI o dublecie
        if (dice.isDouble()) {
            engine.notifyMessage(player.getName() + " wyrzucił dublet! Przysługuje mu dodatkowy rzut.");
        } else {
            engine.notifyMessage("Oczekiwanie na decyzję gracza...");
        }
    }
}