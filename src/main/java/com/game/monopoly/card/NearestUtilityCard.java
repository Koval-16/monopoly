package com.game.monopoly.card;

import com.game.monopoly.board.Field;
import com.game.monopoly.engine.Dice;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class NearestUtilityCard extends Card {

    public NearestUtilityCard(String description) {
        super(description);
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " czyta kartę: " + getDescription());

        int currentPos = player.getPosition();
        int targetPos;

        // Użyteczności są na 12 (Elektrownia) i 28 (Wodociągi)
        if (currentPos < 12) targetPos = 12;
        else if (currentPos < 28) targetPos = 28;
        else targetPos = 12; // Okrążenie planszy

        player.setPosition(targetPos);
        engine.notifyPlayerMoved(player, targetPos);

        if (targetPos < currentPos) {
            engine.notifyMessage(player.getName() + " okrąża planszę. Otrzymuje 200$.");
            player.addMoney(200);
        }

        Field targetField = engine.getBoard().getField(targetPos);
        engine.notifyMessage(player.getName() + " ląduje na najbliższym polu użyteczności: " + targetField.getName());

        // Zgodnie z zasadą, gracz musi rzucić kostką po dotarciu na miejsce
        Dice dice = engine.getDice();
        dice.roll();
        engine.notifyMessage("Rzut kośćmi wymagany przez kartę: wyrzucono " + dice.getTotal());

        TurnContext ctx = new TurnContext(dice);
        // UtilityField użyje tego rzutu do pomnożenia wyliczeń. W standardzie Monopoly
        // to wystarcza, aby obsłużyć mechanikę wielokrotności dla użyteczności.
        targetField.onLand(player, ctx, engine);
    }
}