package com.game.monopoly.economy;

import com.game.monopoly.board.purchase.StreetField;
import com.game.monopoly.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    private Player initiator;
    private Player bidder1;
    private Player bidder2;
    private StreetField property;
    private Auction auction;

    @BeforeEach
    void setUp() {
        // Przygotowanie "czystej karty" przed każdym testem
        initiator = new Player("Gracz Inicjujący"); // Ten, który odrzucił zakup
        bidder1 = new Player("Licytant 1");
        bidder2 = new Player("Licytant 2");

        // Ustawiamy salda
        initiator.setBalance(1000);
        bidder1.setBalance(500); // Licytant 1 celowo dostaje mało gotówki
        bidder2.setBalance(2000);

        // Tworzymy pole, które kosztuje 200$. Cena wywoławcza w Monopoly to połowa (100$).
        int[] rentPrices = {20, 100, 300, 900, 1250, 1500};
        property = new StreetField("Testowa Aleja", 5, 200, 100, "Czerwona", rentPrices);

        List<Player> allPlayers = Arrays.asList(initiator, bidder1, bidder2);

        // Tworzymy nową licytację o to pole
        auction = new Auction(property, allPlayers, initiator);
    }

    @Test
    void testAuctionInitialization() {
        // Assert 1: Cena wywoławcza to równe 50% ceny nominalnej (200 / 2 = 100)
        assertEquals(100, auction.getCurrentBid(), "Cena wywoławcza powinna wynosić połowę ceny pola (100$)");

        // Assert 2: Inicjator nie bierze udziału w licytacji, a pozostali tak
        assertFalse(auction.getActiveParticipants().contains(initiator), "Inicjator nie powinien brać udziału w licytacji");
        assertTrue(auction.getActiveParticipants().contains(bidder1), "Licytant 1 powinien być na liście aktywnych");
        assertTrue(auction.getActiveParticipants().contains(bidder2), "Licytant 2 powinien być na liście aktywnych");
    }

    @Test
    void testPlaceBidValidAndInvalid() {
        // Act 1: Licytant 2 podbija stawkę na 150$ (ma na to środki, 150 > 100)
        boolean success1 = auction.placeBid(bidder2, 150);

        // Assert 1: Sukces
        assertTrue(success1, "Licytant 2 powinien móc przebić na 150$");
        assertEquals(150, auction.getCurrentBid(), "Obecna stawka to teraz 150$");
        assertEquals(bidder2, auction.getHighestBidder(), "Licytant 2 jest na prowadzeniu");

        // Act 2: Licytant 1 próbuje przebić na 120$ (stawka niższa niż obecne 150$) - to zły ruch
        boolean success2 = auction.placeBid(bidder1, 120);
        assertFalse(success2, "Nie można podać stawki niższej niż obecna najwyższa");

        // Act 3: Licytant 1 próbuje przebić na 600$ (nie ma tylu środków na koncie)
        boolean success3 = auction.placeBid(bidder1, 600);
        assertFalse(success3, "Nie można przebić stawki kwotą, której nie posiada się na koncie");
    }

    @Test
    void testAuctionResolution() {
        // Arrange: Licytant 2 wchodzi z agresywną stawką 300$
        auction.placeBid(bidder2, 300);

        // Act: Licytant 1 odpuszcza (pasuje)
        auction.withdraw(bidder1);

        // Assert 1: Aukcja powinna być oznaczona jako zakończona (został 1 gracz)
        assertTrue(auction.isFinished(), "Aukcja powinna się zakończyć, gdy zostanie tylko 1 licytant");

        // Act: Finalizujemy aukcję (rozliczenie gotówki i własności)
        auction.resolveAuction();

        // Assert 2: Licytant 2 traci z konta 300$, a ulica trafia do niego
        assertEquals(1700, bidder2.getBalance(), "Licytant 2 powinien zapłacić 300$ ze swojego salda startowego (2000 - 300)");
        assertEquals(bidder2, property.getOwner(), "Licytant 2 powinien zostać wpisany jako właściciel pola");
        assertTrue(bidder2.getProperties().contains(property), "Pole powinno trafić na stałe do majątku Licytanta 2");
    }
}