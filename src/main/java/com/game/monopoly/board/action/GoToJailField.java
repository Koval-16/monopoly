package com.game.monopoly.board.action;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;
import com.game.monopoly.player.state.InJailState;

public class GoToJailField extends ActionField {

    public GoToJailField(String name, int position) {
        super(name, position);
    }

    @Override
    public void onLand(Player player, TurnContext ctx, GameEngine engine) {
        engine.notifyMessage(player.getName() + " staje na polu 'Idziesz do Więzienia' i zostaje natychmiast aresztowany!");

        // 1. Zmiana pozycji gracza na indeks więzienia (w Monopoly to zawsze pole nr 10)
        player.setPosition(10);
        engine.notifyPlayerMoved(player, 10);

        // 2. Zmiana stanu gracza na uwięzionego
        player.changeState(new InJailState());
        engine.notifyPlayerStateChanged(player);
    }
}