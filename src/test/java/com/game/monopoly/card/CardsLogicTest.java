package com.game.monopoly.card;

import com.game.monopoly.board.purchase.StreetField;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardsLogicTest {

    private GameEngine engine;
    private Player player;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        player = new Player("Pechowy Podatnik");
        player.setBalance(2000); // Dajemy dużo gotówki na start
    }

    @Test
    void testBuildingTaxCardCalculatesCorrectTax() {
        // Arrange: Tworzymy dwie ulice z dzielnicy Brązowej (wymaga tylko 2 do monopolu!)
        int[] rentPrices = {10, 50, 150, 450, 625, 750};
        StreetField street1 = new StreetField("Ulica 1", 1, 100, 50, "Brązowa", rentPrices);
        StreetField street2 = new StreetField("Ulica 2", 3, 100, 50, "Brązowa", rentPrices);

        // Przypisujemy ulice do gracza
        street1.setOwner(player);
        street2.setOwner(player);
        player.addProperty(street1);
        player.addProperty(street2);

        // Budujemy legalnie, zgodnie z naszymi zasadami, żeby uniknąć blokad systemowych!

        // Na Ulicy 1: Stawiamy 3 domki
        for (int i = 0; i < 3; i++) {
            street1.buildHouse(engine);
        }

        // Na Ulicy 2: Aby postawić hotel, musimy najpierw postawić 4 domki
        for (int i = 0; i < 4; i++) {
            street2.buildHouse(engine);
        }
        street2.buildHotel(engine); // Dopiero teraz z powodzeniem stawiamy hotel

        // Zapisujemy stan konta PO ZBUDOWANIU, a przed zapłatą podatku
        int balanceBeforeTax = player.getBalance();

        // Tworzymy kartę: 40$ za każdy domek, 115$ za każdy hotel
        BuildingTaxCard taxCard = new BuildingTaxCard("Płacisz za remont: 40$ za domek, 115$ za hotel", 40, 115);

        // Act: Gracz wyciąga kartę i system pobiera podatek
        taxCard.executeAction(player, engine);

        // Assert:
        // Oczekiwany podatek = (3 domki * 40$) + (1 hotel * 115$) = 120$ + 115$ = 235$
        int expectedTax = 235;
        assertEquals(balanceBeforeTax - expectedTax, player.getBalance(),
                "Karta powinna pobrać dokładnie 235$ podatku za 3 domki i 1 hotel");
    }

    @Test
    void testMoveToCardChangesPosition() {
        // Arrange: Ustawiamy gracza na polu nr 7 (np. Szansa)
        player.setPosition(7);

        // Tworzymy kartę przemieszczającą na pole nr 39 (np. Promenada)
        MoveToCard moveCard = new MoveToCard("Przejdź na Promenadę (pole 39)", 39);

        // Act: Wykonanie karty
        moveCard.executeAction(player, engine);

        // Assert: Gracz musi wylądować na polu nr 39
        assertEquals(39, player.getPosition(), "Karta powinna teleportować gracza na pole 39");
    }

    @Test
    void testMoneyCardAddsOrRemovesMoney() {
        // Arrange: Karta błędu bankowego na twoją korzyść (+200$)
        int initialBalance = player.getBalance();
        MoneyCard bankErrorCard = new MoneyCard("Błąd banku na twoją korzyść. Pobierz 200$", 200);

        // Act
        bankErrorCard.executeAction(player, engine);

        // Assert
        assertEquals(initialBalance + 200, player.getBalance(), "Karta powinna dodać 200$");
    }
}