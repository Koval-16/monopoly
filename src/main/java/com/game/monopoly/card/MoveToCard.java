package com.game.monopoly.card;

import com.game.monopoly.board.Field;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class MoveToCard extends Card {
    private int targetPosition;

    public MoveToCard(String description, int targetPosition) {
        super(description);
        this.targetPosition = targetPosition;
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " czyta kartę: " + getDescription());

        int oldPosition = player.getPosition();

        // 2. Zmiana pozycji i wysłanie informacji do interfejsu
        player.setPosition(this.targetPosition);
        engine.notifyPlayerMoved(player, this.targetPosition);

        // 1. Sprawdzić, czy gracz minął pole Start w drodze na targetPosition
        // W Monopoly karty "Przejdź do..." wymuszają ruch do przodu, więc jeśli nowy indeks
        // jest mniejszy od starego, gracz okrążył planszę (z wyjątkiem drogi do więzienia,
        // ale to obsługuje osobna karta GoToJailCard).
        if (this.targetPosition < oldPosition) {
            engine.notifyMessage(player.getName() + " mija pole Start w drodze do celu. Otrzymuje 200$.");
            player.addMoney(200);
        }

        // 3. Odpalić akcję nowego pola
        Field currentField = engine.getBoard().getField(this.targetPosition);
        engine.notifyMessage(player.getName() + " ląduje na polu: " + currentField.getName());

        TurnContext ctx = new TurnContext(null); // Ruch z karty, brak kości
        currentField.onLand(player, ctx, engine);
    }
}