package com.game.monopoly.board.purchase;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StreetFieldTest {

    private GameEngine engine;
    private Player owner;
    private StreetField street1;
    private StreetField street2;
    private TurnContext context;

    @BeforeEach
    void setUp() {
        // Przygotowanie środowiska przed KAŻDYM testem (Arrange)
        engine = new GameEngine();
        owner = new Player("Testowy Gracz");

        // Zapewniamy graczowi na start 1000$ gotówki (nadpisujemy ew. domyślne 1500)
        owner.setBalance(1000);

        // Tablica czynszów: Pusty, 1 domek, 2 domki, 3 domki, 4 domki, Hotel
        int[] rentPrices = {10, 50, 150, 450, 625, 750};

        // Tworzymy dwie ulice z tej samej dzielnicy (Brązowa wymaga 2 ulic do monopolu)
        street1 = new StreetField("Ulica 1", 1, 60, 50, "Brązowa", rentPrices);
        street2 = new StreetField("Ulica 2", 3, 60, 50, "Brązowa", rentPrices);

        context = new TurnContext(null); // Pusty kontekst, rzut kośćmi nie ma tu znaczenia
    }

    @Test
    void testBaseRentWithoutMonopoly() {
        // Act: Gracz kupuje tylko JEDNĄ ulicę
        street1.setOwner(owner);
        owner.addProperty(street1);

        // Assert: Czynsz powinien wynosić podstawową stawkę za pusty plac (indeks 0 -> 10$)
        int rent = street1.calculateRent(context);
        assertEquals(10, rent, "Czynsz bazowy za 1 ulicę powinien wynosić 10$");
    }

    @Test
    void testDoubleRentWithMonopoly() {
        // Act: Gracz zdobywa całą dzielnicę (tzw. monopol)
        street1.setOwner(owner);
        owner.addProperty(street1);

        street2.setOwner(owner);
        owner.addProperty(street2);

        // Assert: Czynsz powinien być podwojony (10$ * 2 = 20$)
        int rent = street1.calculateRent(context);
        assertEquals(20, rent, "Posiadanie wszystkich ulic w kolorze powinno podwoić czynsz za pusty plac (20$)");
    }

    @Test
    void testBuildHouseFailsWithoutMonopoly() {
        // Act: Gracz kupuje tylko jedną ulicę i próbuje budować
        street1.setOwner(owner);
        owner.addProperty(street1);

        boolean success = street1.buildHouse(engine);

        // Assert: System powinien zablokować budowę
        assertFalse(success, "Nie można budować domków bez pełnego koloru!");
        assertEquals(0, street1.getHouseCount(), "Liczba domków powinna wynosić 0");
    }

    @Test
    void testBuildHouseSuccessAndRentIncrease() {
        // Arrange: Gracz ma cały kolor i gotówkę
        street1.setOwner(owner);
        owner.addProperty(street1);
        street2.setOwner(owner);
        owner.addProperty(street2);

        // Zapamiętujemy stan konta przed budową (domek kosztuje 50$)
        int initialBalance = owner.getBalance();

        // Act: Budujemy 1 domek na ulicy 1
        boolean success = street1.buildHouse(engine);

        // Assert: Weryfikacja wszystkich efektów budowy
        assertTrue(success, "Budowa domku powinna się powieść");
        assertEquals(1, street1.getHouseCount(), "Na ulicy powinien stać dokładnie 1 domek");
        assertEquals(initialBalance - 50, owner.getBalance(), "Z konta gracza powinno ubyć 50$ na domek");

        // Assert: Weryfikacja nowego czynszu (indeks 1 tablicy -> 50$)
        int newRent = street1.calculateRent(context);
        assertEquals(50, newRent, "Czynsz z 1 domkiem powinien wynosić 50$");
    }
}