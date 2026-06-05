package com.game.monopoly.player.state;

import com.game.monopoly.engine.Dice;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActiveStateTest {

    private GameEngine engine;
    private Player player;
    private DeterministicDice mockDice; // Nasza "oszukana" kość

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        player = new Player("Testowy Gracz");
        player.setBalance(1000);
        player.changeState(new ActiveState());

        // Podmieniamy prawdziwe kości na nasze przewidywalne
        mockDice = new DeterministicDice();
        engine.setDice(mockDice);
    }

    @Test
    void testPassingGoAdds200ToBalance() {
        // Arrange: Ustawiamy gracza na końcu planszy (np. pole 38 - Aleje Ujazdowskie)
        player.setPosition(38);
        int initialBalance = player.getBalance();

        // Ustawiamy rzut kośćmi na 4 (2 i 2). Gracz przekroczy Start i wyląduje na polu 2.
        mockDice.setRolls(2, 2);

        // Act: Wykonujemy turę
        player.executeTurn(engine);

        // Assert: Gracz powinien otrzymać 200$ premii
        assertEquals(2, player.getPosition(), "Gracz powinien przesunąć się na pole nr 2");
        assertEquals(initialBalance + 200, player.getBalance(), "Przejście przez Start powinno dodać 200$");
    }

    @Test
    void testThreeDoublesSendsPlayerToJail() {
        // Arrange: Ustawiamy gracza na Start i podajemy mu 3 dublety z rzędu
        player.setPosition(0);

        // Act: Rzucamy 3 razy pod rząd dublet
        for (int i = 0; i < 3; i++) {
            mockDice.setRolls(3, 3); // Zawsze wyrzuca 3 i 3
            player.executeTurn(engine);
        }

        // Assert: Po trzecim rzucie gracz musi trafić do więzienia
        assertEquals(10, player.getPosition(), "Po 3 dubletach pozycja gracza to musi być 10 (Więzienie)");
        assertTrue(player.getCurrentState() instanceof InJailState, "Stan gracza musi zmienić się na InJailState");
    }

    // --- Klasa Pomocnicza (Stub) ---
    // Symuluje rzuty kośćmi, pozwalając nam ustawić konkretny wynik na potrzeby testu.
    private static class DeterministicDice extends Dice {
        private int r1, r2;

        public void setRolls(int r1, int r2) {
            this.r1 = r1;
            this.r2 = r2;
        }

        @Override
        public void roll() { /* Nic nie robimy, wartości są już ustawione w setRolls */ }

        @Override
        public int getRoll1() { return r1; }

        @Override
        public int getRoll2() { return r2; }

        @Override
        public int getTotal() { return r1 + r2; }

        @Override
        public boolean isDouble() { return r1 == r2; }
    }
}