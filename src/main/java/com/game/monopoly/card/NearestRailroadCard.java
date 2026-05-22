package com.game.monopoly.card;

import com.game.monopoly.board.Field;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class NearestRailroadCard extends Card {

    public NearestRailroadCard(String description) {
        super(description);
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " czyta kartę: " + getDescription());

        int currentPos = player.getPosition();
        int targetPos;

        // Dworce znajdują się na polach 5, 15, 25 i 35
        if (currentPos < 5) targetPos = 5;
        else if (currentPos < 15) targetPos = 15;
        else if (currentPos < 25) targetPos = 25;
        else if (currentPos < 35) targetPos = 35;
        else targetPos = 5; // Okrążenie planszy

        player.setPosition(targetPos);
        engine.notifyPlayerMoved(player, targetPos);

        if (targetPos < currentPos) {
            engine.notifyMessage(player.getName() + " okrąża planszę w drodze do stacji. Otrzymuje 200$.");
            player.addMoney(200);
        }

        Field targetField = engine.getBoard().getField(targetPos);
        engine.notifyMessage(player.getName() + " ląduje na najbliższej kolei: " + targetField.getName());

        TurnContext ctx = new TurnContext(null);
        // Ustawienie wymaganego dwukrotnego czynszu
        ctx.setRentMultiplier(2);
        targetField.onLand(player, ctx, engine);
    }
}