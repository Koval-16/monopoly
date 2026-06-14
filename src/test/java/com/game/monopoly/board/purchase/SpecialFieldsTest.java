package com.game.monopoly.board.purchase;

import com.game.monopoly.engine.Dice;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecialFieldsTest {

    private GameEngine engine;
    private Player owner;
    private DeterministicDice mockDice;
    private TurnContext context;

    // Pola kolei
    private RailroadField r1, r2, r3, r4;
    // Pola użyteczności (Wodociągi, Elektrownia)
    private UtilityField u1, u2;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        owner = new Player("Potentat Nieruchomości");

        mockDice = new DeterministicDice();
        engine.setDice(mockDice);

        // Kontekst tury połączony z silnikiem/kośćmi
        context = new TurnContext(mockDice);

        // POPRAWA: Inicjalizacja Kolei (konstruktor przyjmuje tylko: nazwa, pozycja, cena)
        r1 = new RailroadField("Kolej 1", 5, 200);
        r2 = new RailroadField("Kolej 2", 15, 200);
        r3 = new RailroadField("Kolej 3", 25, 200);
        r4 = new RailroadField("Kolej 4", 35, 200);

        // POPRAWA: Inicjalizacja Wodociągów/Elektrowni (konstruktor przyjmuje tylko: nazwa, pozycja, cena)
        u1 = new UtilityField("Wodociągi", 12, 150);
        u2 = new UtilityField("Elektrownia", 28, 150);
    }

    @Test
    void testRailroadRentScaling() {
        // Assert 1: Gracz ma 1 kolej (Czynsz = 25)
        r1.setOwner(owner);
        owner.addProperty(r1);
        assertEquals(25, r1.calculateRent(context), "Czynsz za 1 kolej powinien wynosić 25$");

        // Assert 2: Gracz ma 2 koleje (Czynsz = 50)
        r2.setOwner(owner);
        owner.addProperty(r2);
        assertEquals(50, r2.calculateRent(context), "Czynsz za 2 koleje powinien wynosić 50$");

        // Assert 3: Gracz ma 3 koleje (Czynsz = 100)
        r3.setOwner(owner);
        owner.addProperty(r3);
        assertEquals(100, r3.calculateRent(context), "Czynsz za 3 koleje powinien wynosić 100$");

        // Assert 4: Gracz ma 4 koleje (Czynsz = 200)
        r4.setOwner(owner);
        owner.addProperty(r4);
        assertEquals(200, r4.calculateRent(context), "Czynsz za 4 koleje powinien wynosić 200$");
    }

    @Test
    void testUtilityRentScaling() {
        // Arrange: Ustawiamy sztywny wynik rzutu kośćmi na 7 (np. 3 i 4)
        mockDice.setRolls(3, 4);

        // NAPRAWA: Tworzymy nowy kontekst tury DOPIERO TERAZ, po wymuszeniu rzutu!
        // Dzięki temu TurnContext poprawnie "złapie" wynik 7, a nie początkowe 0.
        context = new TurnContext(mockDice);

        // Act & Assert 1: Gracz ma tylko 1 użyteczność. Mnożnik x4. Czynsz: 7 * 4 = 28$
        u1.setOwner(owner);
        owner.addProperty(u1);
        assertEquals(28, u1.calculateRent(context), "Czynsz za 1 użyteczność przy wyrzuceniu 7 powinien wynosić 28$ (7 * 4)");

        // Act & Assert 2: Gracz ma obie użyteczności. Mnożnik x10. Czynsz: 7 * 10 = 70$
        u2.setOwner(owner);
        owner.addProperty(u2);
        assertEquals(70, u2.calculateRent(context), "Czynsz za 2 użyteczności przy wyrzuceniu 7 powinien wynosić 70$ (7 * 10)");
    }

    // --- Klasa Pomocnicza (Stub) dla kości ---
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