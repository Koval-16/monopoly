package com.game.monopoly.card;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;

import java.util.List;

public class PlayersMoneyCard extends Card {
    private int amountPerPlayer; // Dodatnie = oni płacą tobie, ujemne = ty płacisz im

    public PlayersMoneyCard(String description, int amountPerPlayer) {
        super(description);
        this.amountPerPlayer = amountPerPlayer;
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        engine.notifyMessage(player.getName() + " czyta kartę: " + getDescription());

        List<Player> allPlayers = engine.getPlayers();
        int totalTransferred = 0;

        // Wyświetlamy ogólną informację
        if (this.amountPerPlayer > 0) {
            engine.notifyMessage("Każdy gracz musi zapłacić " + this.amountPerPlayer + "$ graczowi " + player.getName() + ".");
        } else {
            engine.notifyMessage(player.getName() + " musi zapłacić po " + Math.abs(this.amountPerPlayer) + "$ każdemu graczowi.");
        }

        // Iterujemy po wszystkich uczestnikach gry
        for (Player otherPlayer : allPlayers) {

            // Pomijamy samego siebie
            if (otherPlayer == player) {
                continue;
            }

            // Opcjonalne zabezpieczenie: pomijamy graczy, którzy już zbankrutowali
            // Zależnie od implementacji, mogą oni być usuwani z listy, ale to dodatkowy bezpiecznik
            if (otherPlayer.getBalance() < 0) {
                continue;
            }

            // Przelewy bezpośrednie między graczami
            if (this.amountPerPlayer > 0) {
                otherPlayer.payMoney(this.amountPerPlayer);
                player.addMoney(this.amountPerPlayer);
                totalTransferred += this.amountPerPlayer;
            } else {
                int penalty = Math.abs(this.amountPerPlayer);
                player.payMoney(penalty);
                otherPlayer.addMoney(penalty);
                totalTransferred += penalty;
            }
        }

        // Podsumowanie transakcji na ekranie
        if (this.amountPerPlayer > 0) {
            engine.notifyMessage(player.getName() + " zebrał łącznie " + totalTransferred + "$ od pozostałych graczy.");
        } else {
            engine.notifyMessage(player.getName() + " rozdał łącznie " + totalTransferred + "$ pozostałym graczom.");
        }
    }
}