package com.game.monopoly.board.purchase;

import com.game.monopoly.engine.TurnContext;

public class StreetField extends PurchaseField {

    // Dodane atrybuty specyficzne dla ulic (domki, hotele, przynależność do koloru)
    private int houseCount;
    private boolean hasHotel;
    private int housePrice;
    private String colorGroup;
    private int[] rentPrices;
    private int buildingCost;

    public StreetField(String name, int position, int price, int housePrice, String colorGroup, int[] rentPrices) {
        super(name, position, price);
        this.houseCount = 0;
        this.hasHotel = false;
        this.housePrice = housePrice;
        this.colorGroup = colorGroup;

        if (rentPrices == null || rentPrices.length != 6) {
            throw new IllegalArgumentException("Tablica czynszów musi zawierać dokładnie 6 wartości.");
        }
        this.rentPrices = rentPrices;
    }

    @Override
    protected int calculateRent(TurnContext ctx) {
        // Zabezpieczenie przed błędem, gdy pole nie ma właściciela
        if (getOwner() == null) {
            return 0;
        }

        // 1. Czynsz z hotelem (najwyższy priorytet)
        if (this.hasHotel) {
            return this.rentPrices[5];
        }

        // 2. Czynsz z domkami (od 1 do 4)
        if (this.houseCount > 0) {
            return this.rentPrices[this.houseCount];
        }

        // 3. Czynsz za pusty plac - sprawdzenie "Monopolu" (kompletnego koloru)
        if (ownsAllInColorGroup()) {
            return this.rentPrices[0] * 2;
        }

        // 4. Standardowy czynsz bazowy
        return this.rentPrices[0];
    }

    private boolean ownsAllInColorGroup() {
        int count = 0;

        for (PurchaseField property : getOwner().getProperties()) {
            if (property instanceof StreetField) {
                StreetField street = (StreetField) property;
                if (street.getColorGroup().equals(this.colorGroup)) {
                    count++;
                }
            }
        }

        // Zgodnie z planszą Monopoly, dzielnice brązowa i ciemnoniebieska mają po 2 ulice, reszta po 3.
        int requiredAmount = (this.colorGroup.equals("Brązowa") || this.colorGroup.equals("Ciemnoniebieska")) ? 2 : 3;

        return count == requiredAmount;
    }

    public int getHouseCount() {
        return this.houseCount;
    }

    public boolean hasHotel() {
        return this.hasHotel;
    }

    public int getHousePrice() {
        return this.housePrice;
    }

    public String getColorGroup() {
        return this.colorGroup;
    }
}