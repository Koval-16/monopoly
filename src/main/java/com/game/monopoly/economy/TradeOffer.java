package com.game.monopoly.economy;

import com.game.monopoly.player.Player;
import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.board.purchase.StreetField;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca ofertę handlową między dwoma graczami.
 * Hermetyzuje całą transakcję (gotówka, nieruchomości, karty ułaskawienia) w jednym obiekcie.
 */
public class TradeOffer {

    // --- ATRYBUTY ---
    private Player initiator;
    private Player target;

    private int offeredMoney;
    private int requestedMoney;

    private List<PurchaseField> offeredProperties;
    private List<PurchaseField> requestedProperties;

    private int offeredJailCards;
    private int requestedJailCards;

    // --- KONSTRUKTOR ---
    public TradeOffer(Player initiator, Player target) {
        this.initiator = initiator;
        this.target = target;

        // Bezpieczna inicjalizacja pustych wartości
        this.offeredMoney = 0;
        this.requestedMoney = 0;
        this.offeredProperties = new ArrayList<>();
        this.requestedProperties = new ArrayList<>();
        this.offeredJailCards = 0;
        this.requestedJailCards = 0;
    }

    // --- FUNKCJE (Metody) ---

    /**
     * Waliduje ofertę przed jej akceptacją.
     * Sprawdza m.in. czy gracze mają wystarczającą ilość gotówki,
     * czy na oferowanych nieruchomościach nie ma wybudowanych domków (zasada Monopoly)
     * oraz czy posiadają wymagane karty wyjścia z więzienia.
     * * @return true jeśli oferta spełnia zasady gry, false jeśli jest nielegalna.
     */
    public boolean isValid() {
        // 1. Sprawdzenie funduszy
        if (initiator.getBalance() < offeredMoney || target.getBalance() < requestedMoney) {
            return false;
        }

        // 2. Sprawdzenie kart "Wyjdziesz z więzienia"
        if (initiator.getOutOfJailCards() < offeredJailCards || target.getOutOfJailCards() < requestedJailCards) {
            return false;
        }

        // 3. Weryfikacja oferowanych nieruchomości (własność i brak zabudowy)
        if (!verifyProperties(offeredProperties, initiator)) return false;
        if (!verifyProperties(requestedProperties, target)) return false;

        return true;
    }

    private boolean verifyProperties(List<PurchaseField> properties, Player expectedOwner) {
        for (PurchaseField property : properties) {
            // Czy nieruchomość należy do odpowiedniego gracza?
            if (property.getOwner() != expectedOwner) {
                return false;
            }
            // Zgodnie z zasadami Monopoly, nie można handlować ulicą z domkami.
            // Sprawdzamy to za pomocą operatora instanceof.
            if (property instanceof StreetField) {
                StreetField street = (StreetField) property;
                if (street.getHouseCount() > 0 || street.hasHotel()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Główna metoda finalizująca transakcję.
     * Wywoływana dopiero wtedy, gdy gracz docelowy (target) zaakceptuje ofertę, a isValid() zwróci true.
     * Przelewa gotówkę, wymienia karty i przepina atrybut 'owner' na obiektach PurchaseField.
     */
    public void execute() {
        if (!isValid()) {
            throw new IllegalStateException("Próba wykonania nielegalnej transakcji handlowej.");
        }

        // 1. Transfer gotówki
        if (offeredMoney > 0) {
            initiator.payMoney(offeredMoney);
            target.addMoney(offeredMoney);
        }
        if (requestedMoney > 0) {
            target.payMoney(requestedMoney);
            initiator.addMoney(requestedMoney);
        }

        // 2. Transfer kart "Wyjdziesz z więzienia"
        if (offeredJailCards > 0) {
            initiator.removeOutOfJailCards(offeredJailCards);
            target.addOutOfJailCards(offeredJailCards);
        }
        if (requestedJailCards > 0) {
            target.removeOutOfJailCards(requestedJailCards);
            initiator.addOutOfJailCards(requestedJailCards);
        }

        // 3. Przekazanie aktów własności (nieruchomości)
        for (PurchaseField property : offeredProperties) {
            property.setOwner(target);
            initiator.removeProperty(property);
            target.addProperty(property);
        }

        for (PurchaseField property : requestedProperties) {
            property.setOwner(initiator);
            target.removeProperty(property);
            initiator.addProperty(property);
        }
    }

    public void setOfferedMoney(int amount) { this.offeredMoney = amount; }
    public void setRequestedMoney(int amount) { this.requestedMoney = amount; }

    public int getOfferedJailCards() {
        return offeredJailCards;
    }

    public int getOfferedMoney() {
        return offeredMoney;
    }

    public int getRequestedMoney() {
        return requestedMoney;
    }

    public int getRequestedJailCards() {
        return requestedJailCards;
    }

    public void setOfferedJailCards(int count) { this.offeredJailCards = count; }
    public void setRequestedJailCards(int count) { this.requestedJailCards = count; }

    public void addOfferedProperty(PurchaseField field) { this.offeredProperties.add(field); }
    public void addRequestedProperty(PurchaseField field) { this.requestedProperties.add(field); }

    public void removeOfferedProperty(PurchaseField field) { this.offeredProperties.remove(field); }
    public void removeRequestedProperty(PurchaseField field) { this.requestedProperties.remove(field); }

    public Player getInitiator() { return initiator; }
    public Player getTarget() { return target; }
}