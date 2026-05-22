package com.game.monopoly.economy;

import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa reprezentująca trwającą licytację o niechcianą nieruchomość.
 */
public class Auction {

    private PurchaseField property;
    private int currentBid;
    private Player highestBidder;
    private List<Player> activeParticipants;

    public Auction(PurchaseField property, List<Player> allPlayers, Player initiator) {
        this.property = property;
        // Cena wywoławcza to połowa ceny pola
        this.currentBid = property.getPrice() / 2;
        this.highestBidder = null;

        // W licytacji biorą udział "wszyscy pozostali gracze" oprócz tego, który zrezygnował z zakupu
        this.activeParticipants = new ArrayList<>(allPlayers);
        this.activeParticipants.remove(initiator);

        // Zabezpieczenie: usuwamy też bankrutów
        this.activeParticipants.removeIf(p -> p.getBalance() < 0);
    }

    /**
     * Zgłoszenie podbicia stawki przez gracza.
     */
    public boolean placeBid(Player player, int amount) {
        if (activeParticipants.contains(player) && amount > currentBid && player.getBalance() >= amount) {
            this.currentBid = amount;
            this.highestBidder = player;
            return true;
        }
        return false;
    }

    /**
     * Gracz pasuje i wycofuje się z licytacji.
     */
    public void withdraw(Player player) {
        activeParticipants.remove(player);
    }

    /**
     * Licytacja kończy się, gdy zostanie tylko 1 (lub 0) zainteresowanych.
     */
    public boolean isFinished() {
        return activeParticipants.size() <= 1;
    }

    /**
     * Finalizuje aukcję – pobiera pieniądze od zwycięzcy i przypisuje mu akt własności.
     */
    public void resolveAuction() {
        if (highestBidder != null) {
            highestBidder.payMoney(currentBid);
            highestBidder.addProperty(property);
            property.setOwner(highestBidder);
        }
    }

    // --- GETTERY ---
    public PurchaseField getProperty() { return property; }
    public int getCurrentBid() { return currentBid; }
    public Player getHighestBidder() { return highestBidder; }
    public List<Player> getActiveParticipants() { return activeParticipants; }
}