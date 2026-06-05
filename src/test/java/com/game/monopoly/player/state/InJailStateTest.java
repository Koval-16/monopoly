package com.game.monopoly.player.state;

import com.game.monopoly.engine.Dice;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InJailStateTest {

    private GameEngine engine;
    private Player player;
    private DeterministicDice mockDice;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        player = new Player("Testowy Więzień");
        player.setBalance(1000);
        player.setPosition(10); // Pole więzienia

        // Celowo ustawiamy stan na więzienie
        player.changeState(new InJailState());

        mockDice = new DeterministicDice();
        engine.setDice(mockDice);
    }

    @Test
    void testUseGetOutOfJailCard() {
        // Arrange: Gracz posiada 1 kartę wyjścia
        player.addOutOfJailCards(1);

        // Kości wyrzucają "cokolwiek" (nie dublet)
        mockDice.setRolls(1, 2);

        // Act: Wykonanie tury
        player.executeTurn(engine);

        // Assert: Gracz powinien użyć karty i wyjść na wolność
        assertEquals(0, player.getOutOfJailCards(), "Karta powinna zostać zużyta");
        assertTrue(player.getCurrentState() instanceof ActiveState, "Gracz powinien wrócić do stanu ActiveState");
        assertEquals(13, player.getPosition(), "Gracz od razu rzuca i powinien przesunąć się na pole 13 (10 + 3)");
    }

    @Test
    void testForcedBailAfterThreeFailedTurns() {
        // Arrange: Ustawiamy brak dubletu
        mockDice.setRolls(1, 2);
        int initialBalance = player.getBalance();

        // Act: Symulujemy 3 tury w więzieniu bez wyrzucenia dubletu
        player.executeTurn(engine); // Tura 1
        player.executeTurn(engine); // Tura 2
        player.executeTurn(engine); // Tura 3

        // Assert: Po 3 turze system powinien pobrać 100$ i wypuścić gracza
        assertEquals(initialBalance - 100, player.getBalance(), "Po 3 nieudanych turach pobierane jest 100$ kaucji");
        assertTrue(player.getCurrentState() instanceof ActiveState, "Gracz powinien wyjść na wolność (ActiveState)");
        assertEquals(13, player.getPosition(), "Po wyjściu za kaucję gracz powinien przesunąć się o rzut z 3. tury (10 + 3)");
    }

    // --- Powielamy klasę pomocniczą na potrzeby tego pliku ---
    private static class DeterministicDice extends Dice {
        private int r1, r2;
        public void setRolls(int r1, int r2) { this.r1 = r1; this.r2 = r2; }
        @Override public void roll() {}
        @Override public int getRoll1() { return r1; }
        @Override public int getRoll2() { return r2; }
        @Override public int getTotal() { return r1 + r2; }
        @Override public boolean isDouble() { return r1 == r2; }
    }
}