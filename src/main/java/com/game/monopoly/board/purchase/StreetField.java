package com.game.monopoly.board.purchase;

import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.TurnContext;
import com.game.monopoly.player.Player;

public class StreetField extends PurchaseField {

    // --- ATRYBUTY ---
    private int houseCount;
    private boolean hasHotel;
    private int housePrice;
    private String colorGroup;
    private int[] rentPrices;

    // --- KONSTRUKTOR ---
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

    // --- FUNKCJE (Metody) ---

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

    @Override
    public boolean canBeMortgaged() {
        // Ulicę można zastawić tylko gdy nie jest zastawiona ORAZ nie ma budynków!
        return !this.isMortgaged() && this.getHouseCount() == 0 && !this.hasHotel();
    }


    public boolean canBuildHouse(Player player) {
        return !hasHotel() && houseCount < 4 && player.getBalance() >= housePrice;
    }
    public boolean canBuildHotel(Player player) {
        return !hasHotel() && houseCount == 4 && player.getBalance() >= housePrice;
    }

    // W StreetField.java:
    @Override
    public boolean isBuildableMonopoly() {
        return this.ownsAllInColorGroup();
    }

    // W StreetField:
    public void sellTopBuilding(GameEngine engine) {
        if (this.hasHotel()) {
            this.sellHotel(engine);
        } else if (this.getHouseCount() > 0) {
            this.sellHouse(engine);
        }
    }

    public boolean canSellBuilding() {
        return this.getHouseCount() > 0 || this.hasHotel();
    }

    public boolean ownsAllInColorGroup() {
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

    /**
     * Próba wybudowania domku na tej ulicy.
     */
    public boolean buildHouse(GameEngine engine) {
        // 1. Sprawdzenie, czy gracz posiada całą dzielnicę (tzw. Monopol)
        if (!ownsAllInColorGroup()) {
            engine.notifyMessage("Nie możesz budować. Musisz posiadać wszystkie ulice w kolorze: " + getColorGroup());
            return false;
        }
        // 2. Sprawdzenie limitów budynków na polu
        if (this.hasHotel) {
            engine.notifyMessage("Na polu " + getName() + " stoi już hotel. Nie możesz budować domków.");
            return false;
        }
        if (this.houseCount >= 4) {
            engine.notifyMessage("Na polu " + getName() + " stoją już 4 domki. Możesz teraz zbudować tylko hotel.");
            return false;
        }
        // 3. Sprawdzenie funduszy i dostępności domków w Banku
        if (getOwner().getBalance() >= this.housePrice) {
            if (engine.getBank().requestHouse()) {
                // Bank pobiera pieniądze, a my zwiększamy licznik domków
                engine.getBank().receiveMoney(getOwner(), this.housePrice);
                this.houseCount++;
                engine.notifyMessage(getOwner().getName() + " buduje domek na " + getName() + ". Liczba domków: " + this.houseCount);
                return true;
            } else {
                engine.notifyMessage("Bank nie posiada już wolnych domków!");
                return false;
            }
        } else {
            engine.notifyMessage(getOwner().getName() + " nie ma wystarczająco pieniędzy na budowę domku (" + this.housePrice + "$).");
            return false;
        }
    }

    /**
     * Próba wybudowania hotelu na tej ulicy.
     */
    public boolean buildHotel(GameEngine engine) {
        if (this.hasHotel) {
            engine.notifyMessage("Na polu " + getName() + " stoi już hotel.");
            return false;
        }
        if (this.houseCount < 4) {
            engine.notifyMessage("Aby zbudować hotel na " + getName() + ", musisz najpierw postawić 4 domki.");
            return false;
        }
        if (getOwner().getBalance() >= this.housePrice) {
            if (engine.getBank().requestHotel()) {
                // Budowa hotelu oznacza oddanie 4 domków do banku
                for (int i = 0; i < 4; i++) {
                    engine.getBank().returnHouse();
                }

                engine.getBank().receiveMoney(getOwner(), this.housePrice);
                this.houseCount = 0; // Zerujemy domki
                this.hasHotel = true; // Stawiamy hotel
                engine.notifyMessage(getOwner().getName() + " buduje potężny HOTEL na " + getName() + "!");
                return true;
            } else {
                engine.notifyMessage("Bank nie posiada już wolnych hoteli!");
                return false;
            }
        } else {
            engine.notifyMessage(getOwner().getName() + " nie ma wystarczająco pieniędzy na budowę hotelu (" + this.housePrice + "$).");
            return false;
        }
    }

    /**
     * Sprzedaż domku z powrotem do banku za połowę ceny.
     */
    public boolean sellHouse(GameEngine engine) {
        if (this.houseCount > 0 && !this.hasHotel) {
            this.houseCount--;
            engine.getBank().returnHouse();

            int refund = this.housePrice / 2;
            engine.getBank().payOutMoney(getOwner(), refund);

            engine.notifyMessage(getOwner().getName() + " sprzedaje domek z " + getName() + " za " + refund + "$.");
            return true;
        }
        return false;
    }

    /**
     * Sprzedaż hotelu z powrotem do banku za połowę ceny.
     */
    public boolean sellHotel(GameEngine engine) {
        if (this.hasHotel) {
            // Zgodnie z zasadami, sprzedaż hotelu oddaje graczowi 4 domki i zwraca połowę ceny postawienia hotelu.
            this.hasHotel = false;
            this.houseCount = 4;
            engine.getBank().returnHotel();

            int refund = this.housePrice / 2;
            engine.getBank().payOutMoney(getOwner(), refund);

            engine.notifyMessage(getOwner().getName() + " sprzedaje hotel z " + getName() + " za " + refund + "$. Na ulicę wracają 4 domki.");
            return true;
        }
        return false;
    }

    public int[] getRentPrices() {
        return this.rentPrices;
    }
    // --- GETTERY ---

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