package com.game.monopoly.economy;

import com.game.monopoly.board.purchase.StreetField;
import com.game.monopoly.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradeOfferTest {

    private Player initiator;
    private Player target;
    private StreetField propertyToTrade;

    @BeforeEach
    void setUp() {
        // Arrange: Przygotowanie "czystej karty" przed każdym testem
        initiator = new Player("Gracz A"); // Ten, który proponuje wymianę
        target = new Player("Gracz B");    // Ten, do którego kierowana jest oferta

        // Dajemy każdemu po 1000$ na start
        initiator.setBalance(1000);
        target.setBalance(1000);

        // Tworzymy przykładową ulicę i oddajemy ją w ręce Gracza A
        int[] rentPrices = {10, 50, 150, 450, 625, 750};
        propertyToTrade = new StreetField("Ulica Testowa", 1, 100, 50, "Czerwona", rentPrices);

        propertyToTrade.setOwner(initiator);
        initiator.addProperty(propertyToTrade);
    }

    @Test
    void testValidTradeExecution() {
        // Arrange: Konstruujemy ofertę. Gracz A chce oddać Ulicę za 200$ od Gracza B
        TradeOffer offer = new TradeOffer(initiator, target);

        // Zależnie od tego, jak nazwaliśmy metody w Twojej klasie TradeOffer:
        offer.addOfferedProperty(propertyToTrade);
        offer.setRequestedMoney(200);

        // Upewniamy się, że oferta jest "legalna"
        assertTrue(offer.isValid(), "Oferta powinna być prawidłowa, gracza B stać na zapłatę 200$");

        // Act: Wykonujemy transakcję (tak jakby Gracz B kliknął "Akceptuj" w GUI)
        offer.execute();

        // Assert 1: Weryfikacja transferu gotówki
        assertEquals(1200, initiator.getBalance(), "Gracz A (inicjator) powinien otrzymać 200$");
        assertEquals(800, target.getBalance(), "Gracz B (cel) powinien zapłacić 200$");

        // Assert 2: Weryfikacja zmiany właściciela na samej karcie (polu)
        assertEquals(target, propertyToTrade.getOwner(), "Gracz B powinien być nowym właścicielem ulicy");

        // Assert 3: Weryfikacja zmian na listach posiadłości obu graczy
        assertTrue(target.getProperties().contains(propertyToTrade), "Ulica powinna znaleźć się na liście majątku Gracza B");
        assertFalse(initiator.getProperties().contains(propertyToTrade), "Ulica powinna zostać usunięta z listy majątku Gracza A");
    }

    @Test
    void testTradeFailsWhenTargetHasInsufficientFunds() {
        // Arrange: Gracz A proponuje oddanie Ulicy, ale żąda zaporowej kwoty 5000$
        TradeOffer offer = new TradeOffer(initiator, target);
        offer.addOfferedProperty(propertyToTrade);
        offer.setRequestedMoney(5000); // Gracz B ma tylko 1000$

        // Act & Assert: Metoda weryfikująca powinna natychmiast wychwycić oszustwo/brak środków
        assertFalse(offer.isValid(), "Oferta nie powinna być prawidłowa, jeśli cel nie posiada wystarczającej ilości gotówki");
    }
}