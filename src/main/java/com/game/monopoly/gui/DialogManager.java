package com.game.monopoly.gui;

import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.economy.TradeOffer;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.player.Player;
import com.game.monopoly.player.state.BankruptState;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogManager {

    private GameEngine engine;

    public DialogManager(GameEngine engine) {
        this.engine = engine;
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void startAuctionUI(PurchaseField property, List<Player> participants, Runnable onComplete) {
        List<Player> activeBidders = new ArrayList<>(participants);
        activeBidders.removeIf(p -> p.getCurrentState() instanceof BankruptState);

        int currentBid = property.getPrice() / 2;
        Player highestBidder = null;
        int bidderIndex = 0;

        while (activeBidders.size() > 1 || (activeBidders.size() == 1 && highestBidder == null)) {
            Player biddingPlayer = activeBidders.get(bidderIndex);

            TextInputDialog dialog = new TextInputDialog(String.valueOf(currentBid + 10));
            dialog.setTitle("Licytacja: " + property.getName());
            dialog.setHeaderText("Aktualna cena: " + currentBid + "$\nNajwyższa oferta: " + (highestBidder != null ? highestBidder.getName() : "Brak"));
            dialog.setContentText("Gracz " + biddingPlayer.getName() + " (Portfel: " + biddingPlayer.getBalance() + "$)\nPodaj kwotę (lub zostaw puste by spasować):");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent() && !result.get().trim().isEmpty()) {
                try {
                    int bid = Integer.parseInt(result.get().trim());
                    if (bid > currentBid && bid <= biddingPlayer.getBalance()) {
                        currentBid = bid;
                        highestBidder = biddingPlayer;
                        bidderIndex = (bidderIndex + 1) % activeBidders.size();
                    } else {
                        showAlert("Błąd", "Oferta musi być większa niż " + currentBid + "$ i nie może przekraczać budżetu!");
                    }
                } catch (NumberFormatException e) {
                    activeBidders.remove(bidderIndex);
                    if (bidderIndex >= activeBidders.size()) bidderIndex = 0;
                }
            } else {
                activeBidders.remove(bidderIndex);
                if (bidderIndex >= activeBidders.size() && !activeBidders.isEmpty()) bidderIndex = 0;
            }
        }

        if (highestBidder != null) {
            highestBidder.payMoney(currentBid);
            property.setOwner(highestBidder);
            highestBidder.addProperty(property);
            showAlert("Koniec Licytacji", "Gracz " + highestBidder.getName() + " wygrywa za " + currentBid + "$!");
        } else {
            showAlert("Koniec Licytacji", "Nikt nie kupił nieruchomości.");
        }

        if (onComplete != null) onComplete.run();
    }

    public void showTradeDialog(Player initiator, Runnable onComplete) {
        List<Player> possibleTargets = new ArrayList<>(engine.getPlayers());
        possibleTargets.remove(initiator);
        possibleTargets.removeIf(p -> p.getCurrentState() instanceof BankruptState);

        if (possibleTargets.isEmpty()) {
            showAlert("Handel", "Brak innych graczy w grze.");
            return;
        }

        ChoiceDialog<Player> targetDialog = new ChoiceDialog<>(possibleTargets.get(0), possibleTargets);
        targetDialog.setTitle("Propozycja handlu");
        targetDialog.setHeaderText("Z kim chcesz handlować?");
        targetDialog.setContentText("Wybierz gracza:");

        Optional<Player> targetResult = targetDialog.showAndWait();
        if (targetResult.isPresent()) {
            Player target = targetResult.get();
            showDetailedTradeDialog(initiator, target, onComplete);
        }
    }

    private void showDetailedTradeDialog(Player initiator, Player target, Runnable onComplete) {
        Dialog<TradeOffer> dialog = new Dialog<>();
        dialog.setTitle("Konfiguracja Handlu");
        dialog.setHeaderText("Handel pomiędzy: " + initiator.getName() + " a " + target.getName());

        ButtonType proposeButtonType = new ButtonType("Zaproponuj", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(proposeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        grid.add(new Label("Ty oferujesz:"), 0, 0);
        TextField offeredMoneyField = new TextField("0");
        grid.add(new Label("Gotówka ($):"), 0, 1);
        grid.add(offeredMoneyField, 1, 1);
        TextField offeredCardsField = new TextField("0");
        grid.add(new Label("Karty z więzienia (" + initiator.getOutOfJailCards() + "):"), 0, 2);
        grid.add(offeredCardsField, 1, 2);
        ListView<PurchaseField> offeredProps = new ListView<>();
        offeredProps.getItems().addAll(initiator.getProperties());
        offeredProps.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        offeredProps.setPrefHeight(150); offeredProps.setPrefWidth(200);
        offeredProps.setCellFactory(param -> new ListCell<PurchaseField>() {
            @Override protected void updateItem(PurchaseField item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        grid.add(new Label("Nieruchomości:\n(Ctrl - wiele)"), 0, 3);
        grid.add(offeredProps, 1, 3);

        grid.add(new Label("Oczekujesz od " + target.getName() + ":"), 2, 0);
        TextField requestedMoneyField = new TextField("0");
        grid.add(new Label("Gotówka ($):"), 2, 1);
        grid.add(requestedMoneyField, 3, 1);
        TextField requestedCardsField = new TextField("0");
        grid.add(new Label("Karty z więzienia (" + target.getOutOfJailCards() + "):"), 2, 2);
        grid.add(requestedCardsField, 3, 2);
        ListView<PurchaseField> requestedProps = new ListView<>();
        requestedProps.getItems().addAll(target.getProperties());
        requestedProps.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        requestedProps.setPrefHeight(150); requestedProps.setPrefWidth(200);
        requestedProps.setCellFactory(param -> new ListCell<PurchaseField>() {
            @Override protected void updateItem(PurchaseField item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        grid.add(new Label("Nieruchomości:\n(Ctrl - wiele)"), 2, 3);
        grid.add(requestedProps, 3, 3);

        dialog.getDialogPane().setContent(grid);

        List<String> offeredNames = new ArrayList<>();
        List<String> requestedNames = new ArrayList<>();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == proposeButtonType) {
                TradeOffer offer = new TradeOffer(initiator, target);
                try { offer.setOfferedMoney(Integer.parseInt(offeredMoneyField.getText().trim())); } catch(Exception e){}
                try { offer.setRequestedMoney(Integer.parseInt(requestedMoneyField.getText().trim())); } catch(Exception e){}
                try { offer.setOfferedJailCards(Integer.parseInt(offeredCardsField.getText().trim())); } catch(Exception e){}
                try { offer.setRequestedJailCards(Integer.parseInt(requestedCardsField.getText().trim())); } catch(Exception e){}

                for(PurchaseField pf : offeredProps.getSelectionModel().getSelectedItems()) { offer.addOfferedProperty(pf); offeredNames.add(pf.getName()); }
                for(PurchaseField pf : requestedProps.getSelectionModel().getSelectedItems()) { offer.addRequestedProperty(pf); requestedNames.add(pf.getName()); }
                return offer;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(offer -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Weryfikacja Handlu");
            confirmDialog.setHeaderText("Gracz " + target.getName() + ", czy akceptujesz układ?");
            String text = initiator.getName() + " zaoferował:\n- Gotówka: " + offer.getOfferedMoney() + "$\n- Karty: " + offer.getOfferedJailCards() + "\n- Nieruchomości: " + (offeredNames.isEmpty()?"Brak":String.join(", ", offeredNames)) + "\n\n" +
                    initiator.getName() + " żąda:\n- Gotówka: " + offer.getRequestedMoney() + "$\n- Karty: " + offer.getRequestedJailCards() + "\n- Nieruchomości: " + (requestedNames.isEmpty()?"Brak":String.join(", ", requestedNames));
            confirmDialog.setContentText(text);

            Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                if (offer.isValid()) {
                    offer.execute();
                    engine.notifyMessage("Wymiana handlowa zakończona sukcesem!");
                    if (onComplete != null) onComplete.run();
                } else {
                    showAlert("Błąd handlu", "Oferta jest nielegalna! Brak środków lub domki na ulicy.");
                }
            } else {
                engine.notifyMessage(target.getName() + " odrzuca ofertę handlową.");
            }
        });
    }
}