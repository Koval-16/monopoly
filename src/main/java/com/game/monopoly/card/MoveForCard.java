package com.game.monopoly.card;

import com.game.monopoly.board.Field;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class MoveForCard extends Card {
    private int steps; // Może być ujemne

    public MoveForCard(String description, int steps) {
        super(description);
        this.steps = steps;
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " czyta kartę: " + getDescription());

        int oldPosition = player.getPosition();

        // Obliczenie nowej pozycji (z uwzględnieniem cofania się poza pole 0)
        int newPosition = (oldPosition + this.steps) % 40;
        if (newPosition < 0) {
            newPosition += 40;
        }

        // Zmiana pozycji i powiadomienie GUI
        player.setPosition(newPosition);
        engine.notifyPlayerMoved(player, newPosition);

        // Premia za przejście przez Start przysługuje TYLKO przy ruchu do przodu
        if (this.steps > 0 && newPosition < oldPosition) {
            engine.notifyMessage(player.getName() + " mija pole Start przy okazji ruchu z karty. Otrzymuje 200$.");
            player.addMoney(200);
        }

        // Pobranie nowego pola i wywołanie jego akcji
        Field currentField = engine.getBoard().getField(newPosition);
        engine.notifyMessage(player.getName() + " ląduje po przesunięciu na polu: " + currentField.getName());

        // Kontekst bez rzutu kośćmi (null), ponieważ to ruch wymuszony kartą
        TurnContext ctx = new TurnContext(null);
        currentField.onLand(player, ctx, engine);
    }
}