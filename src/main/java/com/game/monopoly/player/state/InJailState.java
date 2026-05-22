package com.game.monopoly.player.state;

import com.game.monopoly.board.Field;
import com.game.monopoly.card.GetOutOfJailCard;
import com.game.monopoly.engine.Dice;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

/**
 * Stan reprezentujący gracza uwięzionego.
 * Gracz w tym stanie ma ograniczone możliwości ruchowe i musi się uwolnić.
 */
public class InJailState implements PlayerState {

    // --- ATRYBUTY ---
    private int turnsLeft;

    // --- KONSTRUKTOR ---
    public InJailState() {
        // Zgodnie z zasadami Monopoly, gracz może próbować rzucić dublet przez maksymalnie 3 tury
        this.turnsLeft = 3;
    }

    // --- FUNKCJE (Metody) ---

    @Override
    public void playTurn(Player player, GameEngine engine) {
        this.turnsLeft--;
        engine.notifyMessage(player.getName() + " rozpoczyna turę w więzieniu. Pozostało prób: " + (this.turnsLeft + 1));

        // 1. STRATEGIA 1: Jeśli gracz posiada kartę "Wyjdziesz bezpłatnie z więzienia" - używa jej natychmiast
        if (player.getOutOfJailCards() > 0) {
            engine.notifyMessage(player.getName() + " używa karty 'Wyjdziesz bezpłatnie z więzienia' i opuszcza areszt!");
            player.removeOutOfJailCards(1);
            engine.getChanceDeck().putCardAtBottom(new GetOutOfJailCard("Wyjdziesz bezpłatnie z więzienia"));

            // Zmiana stanu na aktywny
            ActiveState activeState = new ActiveState();
            player.changeState(activeState);
            engine.notifyPlayerStateChanged(player);

            // Rozegranie normalnej, pełnej tury (rzut z prawem do kolejnego dubletu)
            activeState.playTurn(player, engine);
            return;
        }

        // 2. STRATEGIA 2: Próba rzutu kośćmi (szukanie dubletu)
        Dice dice = engine.getDice();
        dice.roll();
        engine.notifyMessage(player.getName() + " rzuca w więzieniu i wyrzuca: " + dice.getRoll1() + " oraz " + dice.getRoll2());

        if (dice.isDouble()) {
            engine.notifyMessage(player.getName() + " wyrzucił dublet! Wychodzi na wolność.");

            player.changeState(new ActiveState());
            engine.notifyPlayerStateChanged(player);

            // Wykonanie ruchu (w tej turze dublet nie daje ponownego rzutu)
            executeMoveAndLand(player, engine, dice);
        } else {
            // 3. STRATEGIA 3: Brak dubletu - sprawdzenie czy to była ostatnia szansa
            if (this.turnsLeft == 0) {
                engine.notifyMessage(player.getName() + " spędził 3 tury w więzieniu. Jest zmuszony zapłacić kaucję 50$ i opuszcza areszt.");

                // Transakcja gotówkowa
                player.payMoney(50);
                engine.getBank().receiveMoney(player, 50);

                player.changeState(new ActiveState());
                engine.notifyPlayerStateChanged(player);

                // Po zapłaceniu kaucji w 3. turze gracz rusza się o wyrzucone właśnie oczka
                executeMoveAndLand(player, engine, dice);
            } else {
                engine.notifyMessage(player.getName() + " nie wyrzucił dubletu i zostaje w więzieniu na kolejną rundę.");
                engine.endTurn();
            }
        }
    }

    private void executeMoveAndLand(Player player, GameEngine engine, Dice dice) {
        int oldPosition = player.getPosition();
        player.move(dice.getTotal());
        int newPosition = player.getPosition();

        engine.notifyPlayerMoved(player, newPosition);

        // Premia za przejście przez Start, jeśli opuszczając więzienie okrążył planszę
        if (newPosition < oldPosition) {
            engine.notifyMessage(player.getName() + " przechodzi przez linię Start i otrzymuje 200$.");
            player.addMoney(200);
        }

        // Odpalenie akcji pola docelowego
        Field currentField = engine.getBoard().getField(newPosition);
        engine.notifyMessage(player.getName() + " ląduje na polu: " + currentField.getName());

        TurnContext ctx = new TurnContext(dice);
        currentField.onLand(player, ctx, engine);

        // Koniec tury (brak dodatkowego rzutu)
        engine.endTurn();
    }
}