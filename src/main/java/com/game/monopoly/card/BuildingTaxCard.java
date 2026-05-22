package com.game.monopoly.card;

import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.board.purchase.StreetField;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;

public class BuildingTaxCard extends Card {
    private int taxPerHouse;
    private int taxPerHotel;

    public BuildingTaxCard(String description, int taxPerHouse, int taxPerHotel) {
        super(description);
        this.taxPerHouse = taxPerHouse;
        this.taxPerHotel = taxPerHotel;
    }

    @Override
    public void executeAction(Player player, GameEngine engine) {
        int totalHouses = 0;
        int totalHotels = 0;

        // Przeszukujemy wszystkie posiadłości gracza
        for (PurchaseField property : player.getProperties()) {
            // Tylko na klasycznych ulicach mogą stać budynki
            if (property instanceof StreetField) {
                StreetField street = (StreetField) property;
                totalHouses += street.getHouseCount();

                if (street.hasHotel()) {
                    totalHotels++;
                }
            }
        }

        // Obliczamy ostateczną kwotę do zapłaty
        int totalTax = (totalHouses * this.taxPerHouse) + (totalHotels * this.taxPerHotel);

        if (totalTax > 0) {
            engine.notifyMessage(player.getName() + " posiada łącznie " + totalHouses + " domków i " + totalHotels + " hoteli.");
            engine.notifyMessage("Musisz zapłacić " + totalTax + "$ podatku za remonty!");

            // Pobrana kwota trafia prosto do banku
            engine.getBank().receiveMoney(player, totalTax);
        } else {
            engine.notifyMessage(player.getName() + " nie posiada żadnych budynków. Uff, tym razem się upiekło!");
        }
    }
}