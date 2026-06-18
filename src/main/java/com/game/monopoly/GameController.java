package com.game.monopoly;

import com.game.monopoly.board.action.CardField;
import com.game.monopoly.card.Card;
import com.game.monopoly.economy.TradeOffer;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.GameObserver;
import com.game.monopoly.player.Player;
import com.game.monopoly.player.state.BankruptState;
import com.game.monopoly.player.state.InJailState;
import com.game.monopoly.board.Field;
import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.board.purchase.StreetField;
import com.game.monopoly.gui.BoardRenderer;
import com.game.monopoly.gui.DialogManager;
import com.game.monopoly.gui.PlayerPanelRenderer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.*;

public class GameController implements GameObserver {

    // --- Elementy z pliku FXML ---
    @FXML private Label currentPlayerLabel;
    @FXML private Label balanceLabel;
    @FXML private Button rollDiceButton, endTurnButton, resolveFieldButton, drawCardButton;
    @FXML private Button buyPropertyButton, auctionPropertyButton, tradeButton;
    @FXML private Button buildHouseButton, buildHotelButton, sellBuildingButton;
    @FXML private Button mortgageButton, unmortgageButton;
    @FXML private TextArea gameLogsArea;
    @FXML private GridPane boardGrid;
    @FXML private VBox playersSummaryBox, playerCardsBox, jailActionsBox;
    @FXML private Button payBailButton, useJailCardButton, rollDoubleForJailButton;
    @FXML private ComboBox<String> buildPropertyComboBox, managePropertyComboBox;

    // --- Silnik Gry i Klasy Pomocnicze GUI ---
    private GameEngine engine;
    private BoardRenderer boardRenderer;
    private PlayerPanelRenderer playerPanelRenderer;
    private DialogManager dialogManager;

    private Map<String, StreetField> buildablePropertiesMap = new HashMap<>();
    private Map<String, PurchaseField> ownedPropertiesMap = new HashMap<>();

    @FXML
    public void initialize() {
        engine = new GameEngine();
        engine.addObserver(this);

        // Inicjalizacja nowych klas renderujących (Wzorzec Oddelegowania)
        boardRenderer = new BoardRenderer(boardGrid);
        playerPanelRenderer = new PlayerPanelRenderer(playersSummaryBox, playerCardsBox);
        dialogManager = new DialogManager(engine);

        engine.start(Arrays.asList("Alojzy", "Bernard", "Cecylia", "Danuta"));

        if (buildPropertyComboBox != null) {
            buildPropertyComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateBuildButtons(newVal));
        }
        if (managePropertyComboBox != null) {
            managePropertyComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateMortgageButtons(newVal));
        }

        boardRenderer.initBoard(engine.getBoard().getFields());
        boardRenderer.updatePawns(engine.getPlayers());
        boardRenderer.drawDice(0, 0);
        boardRenderer.updateBoardBuildings(engine.getBoard().getFields());

        updatePlayerUI();
    }

    // ==========================================
    // AKCJE GŁÓWNE (Kupowanie, Kości, Karty)
    // ==========================================

    @FXML
    public void onRollDiceClicked() {
        engine.playTurn();
        boardRenderer.drawDice(engine.getDice().getRoll1(), engine.getDice().getRoll2());

        rollDiceButton.setDisable(true);
        endTurnButton.setDisable(true);
        resolveFieldButton.setDisable(false);

        buyPropertyButton.setDisable(true);
        auctionPropertyButton.setDisable(true);
        if (drawCardButton != null) drawCardButton.setDisable(true);

        updatePlayerUI();
    }

    @FXML
    public void onResolveFieldClicked() {
        Player currentPlayer = engine.getCurrentPlayer();
        Field currentField = engine.getBoard().getFields().get(currentPlayer.getPosition());

        engine.resolveCurrentField();

        resolveFieldButton.setDisable(true);

        // Zamiast: if (currentField instanceof PurchaseField)
        if (currentField.isPurchasable()) {
            PurchaseField property = (PurchaseField) currentField;
            if (!property.isOwned()) {
                buyPropertyButton.setDisable(!property.canBeBoughtBy(currentPlayer));
                auctionPropertyButton.setDisable(false);
                endTurnButton.setDisable(true);
                rollDiceButton.setDisable(true);
            } else {
                enableEndTurnOrDouble();
            }
        }
        else if (currentField.isCardField()) {
            drawCardButton.setDisable(false);
            endTurnButton.setDisable(true);
            rollDiceButton.setDisable(true);
        } else {
            enableEndTurnOrDouble();
        }

        updatePlayerUI();
    }

    @FXML
    public void onBuyPropertyClicked() {
        Player current = engine.getCurrentPlayer();
        Field currentField = engine.getBoard().getFields().get(current.getPosition());

        if (currentField.isPurchasable()) {
            PurchaseField property = (PurchaseField) currentField;
            property.buy(current);

            engine.notifyMessage(current.getName() + " kupuje " + property.getName() + "!");
            buyPropertyButton.setDisable(true);
            auctionPropertyButton.setDisable(true);

            updatePlayerUI();
            enableEndTurnOrDouble();
        }
    }

    @FXML
    public void onAuctionPropertyClicked() {
        Player activePlayer = engine.getCurrentPlayer();
        List<Player> allPlayers = new ArrayList<>(engine.getPlayers());

        Field currentField = engine.getBoard().getFields().get(activePlayer.getPosition());
        if (currentField.isPurchasable()) {
            dialogManager.startAuctionUI((PurchaseField) currentField, allPlayers, () -> {
                buyPropertyButton.setDisable(true);
                auctionPropertyButton.setDisable(true);
                resolveFieldButton.setDisable(true);
                updatePlayerUI();
                enableEndTurnOrDouble();
            });
        }
    }

    @FXML
    public void onDrawCardClicked() {
        Player currentPlayer = engine.getCurrentPlayer();
        Field currentField = engine.getBoard().getFields().get(currentPlayer.getPosition());

        if (currentField.isCardField()) {
            CardField cardField = (CardField) currentField;
            Card drawnCard = cardField.drawAndExecute(currentPlayer, engine);

            if(drawnCard != null) {
                dialogManager.showAlert("Karta - " + cardField.getDeckType(), drawnCard.getDescription());
                engine.notifyMessage(currentPlayer.getName() + " rozpatrzył kartę.");
                drawCardButton.setDisable(true);

                updatePlayerUI();
                boardRenderer.updateBoardBuildings(engine.getBoard().getFields());
                boardRenderer.updatePawns(engine.getPlayers());
                enableEndTurnOrDouble();
            }
        }
    }

    @FXML
    public void onTradeClicked() {
        dialogManager.showTradeDialog(engine.getCurrentPlayer(), this::updatePlayerUI);
    }

    @FXML
    public void onEndTurnClicked() {
        endTurnButton.setDisable(true);
        rollDiceButton.setDisable(false);
        engine.endTurn();
        updatePlayerUI();
    }

    private void enableEndTurnOrDouble() {
        if (engine.canRollAgain()) {
            rollDiceButton.setDisable(false);
            endTurnButton.setDisable(true);
            engine.notifyMessage("DUBLET! Rozpatrzyłeś pole, rzucasz jeszcze raz.");
        } else {
            endTurnButton.setDisable(false);
        }
    }

    // ==========================================
    // BUDOWNICTWO I HIPOTEKA
    // ==========================================

    @FXML
    public void onBuildHouseClicked() {
        String selected = buildPropertyComboBox.getValue();
        if (selected != null && buildablePropertiesMap.containsKey(selected)) {
            if (buildablePropertiesMap.get(selected).buildHouse(engine)) {
                updatePlayerUI();
                boardRenderer.updateBoardBuildings(engine.getBoard().getFields());
            }
        }
    }

    @FXML
    public void onBuildHotelClicked() {
        String selected = buildPropertyComboBox.getValue();
        if (selected != null && buildablePropertiesMap.containsKey(selected)) {
            if (buildablePropertiesMap.get(selected).buildHotel(engine)) {
                updatePlayerUI();
                boardRenderer.updateBoardBuildings(engine.getBoard().getFields());
            }
        }
    }

    @FXML
    public void onSellBuildingClicked() {
        String selected = buildPropertyComboBox.getValue();
        if (selected != null && buildablePropertiesMap.containsKey(selected)) {
            StreetField sf = buildablePropertiesMap.get(selected);
            sf.sellTopBuilding(engine);

            updatePlayerUI();
            boardRenderer.updateBoardBuildings(engine.getBoard().getFields());
        }
    }

    @FXML
    public void onMortgageClicked() {
        String selected = managePropertyComboBox.getValue();
        if (selected != null && ownedPropertiesMap.containsKey(selected)) {
            PurchaseField pf = ownedPropertiesMap.get(selected);
            pf.mortgage();
            engine.notifyMessage(engine.getCurrentPlayer().getName() + " zastawia " + pf.getName() + " za " + (pf.getMortgageValue()) + "$.");
            updatePlayerUI();
        }
    }

    @FXML
    public void onUnmortgageClicked() {
        String selected = managePropertyComboBox.getValue();
        if (selected != null && ownedPropertiesMap.containsKey(selected)) {
            PurchaseField pf = ownedPropertiesMap.get(selected);
            pf.unmortgage();
            engine.notifyMessage(engine.getCurrentPlayer().getName() + " wykupuje z zastawu " + pf.getName() + ".");
            updatePlayerUI();
        }
    }

    // ==========================================
    // OBSŁUGA WIĘZIENIA
    // ==========================================

    @FXML
    public void onPayBailClicked() {
        Player current = engine.getCurrentPlayer();
        current.payBail();
        engine.notifyMessage(current.getName() + " wpłaca 100$ kaucji. Jest wolny i w następnej turze będzie mógł grać.");
        endJailTurn();
    }

    @FXML
    public void onUseJailCardClicked() {
        Player current = engine.getCurrentPlayer();
        current.useJailCard();
        engine.notifyMessage(current.getName() + " używa karty wyjścia z więzienia! Jest wolny.");
        endJailTurn();
    }

    @FXML
    public void onRollDoubleForJailClicked() {
        Player current = engine.getCurrentPlayer();
        boolean success = engine.tryRollForJail();
        int r1 = engine.getDice().getRoll1();
        int r2 = engine.getDice().getRoll2();
        boardRenderer.drawDice(r1, r2);

        if (success) {
            engine.notifyMessage(current.getName() + " wyrzuca dublet! Wychodzi.");
        } else {
            engine.notifyMessage(current.getName() + " nie wyrzuca dubletu.");
        }
        endJailTurn();
    }

    private void endJailTurn() {
        if (jailActionsBox != null) jailActionsBox.setDisable(true);
        endTurnButton.setDisable(false);
        updatePlayerUI();
    }

    // ==========================================
    // AKTUALIZACJE UI
    // ==========================================

    private void updatePlayerUI() {
        Player current = engine.getCurrentPlayer();
        if (current != null) {
            currentPlayerLabel.setText("Tura: " + current.getName());
            balanceLabel.setText("Gotówka: " + current.getBalance() + "$\nKarty z więzienia: " + current.getOutOfJailCards());

            // W GameController:
            boolean inJail = current.isInJail();

            if (jailActionsBox != null) {
                jailActionsBox.setVisible(inJail);
                jailActionsBox.setManaged(inJail);
                jailActionsBox.setDisable(false);
            }
            if (rollDiceButton != null) {
                rollDiceButton.setVisible(!inJail);
                rollDiceButton.setManaged(!inJail);
            }

            if (inJail) {
                payBailButton.setDisable(!current.canPayBail());
                useJailCardButton.setDisable(!current.hasOutOfJailCard());
                rollDoubleForJailButton.setDisable(false);
                endTurnButton.setDisable(true);
                resolveFieldButton.setDisable(true);
            }

            playerPanelRenderer.updatePlayersSummary(engine.getPlayers(), current);
            playerPanelRenderer.updatePlayerCards(current);
            boardRenderer.updateBoardOwnership(engine.getBoard().getFields(), engine.getPlayers());

            updateBuildableProperties(current);
            updateOwnedProperties(current);
        }
    }

    private void updateBuildableProperties(Player current) {
        buildablePropertiesMap.clear();
        String currentlySelected = buildPropertyComboBox.getValue();
        buildPropertyComboBox.getItems().clear();

        // Wystarczy zapytać sam model, czy dany gracz ma monopol na konkretną ulicę!
        for (PurchaseField pf : current.getProperties()) {
            if (pf.isBuildableMonopoly()) {
                buildablePropertiesMap.put(pf.getName(), (StreetField) pf);
                // (Rzutowanie w mapie możesz zostawić, chyba że zmienisz typ mapy na PurchaseField)
                buildPropertyComboBox.getItems().add(pf.getName());
            }
        }

        if (buildPropertyComboBox.getItems().isEmpty()) {
            buildPropertyComboBox.setDisable(true);
            buildPropertyComboBox.setPromptText("Brak pełnych dzielnic");
            buildHouseButton.setDisable(true);
            buildHotelButton.setDisable(true);
            sellBuildingButton.setDisable(true);
        } else {
            buildPropertyComboBox.setDisable(false);
            buildPropertyComboBox.setPromptText("Wybierz ulicę...");
            if (currentlySelected != null && buildPropertyComboBox.getItems().contains(currentlySelected)) {
                buildPropertyComboBox.getSelectionModel().select(currentlySelected);
            } else {
                buildPropertyComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    private void updateBuildButtons(String selectedStreetName) {
        if (selectedStreetName == null || !buildablePropertiesMap.containsKey(selectedStreetName)) {
            buildHouseButton.setDisable(true);
            buildHotelButton.setDisable(true);
            sellBuildingButton.setDisable(true);
            return;
        }

        StreetField sf = buildablePropertiesMap.get(selectedStreetName);
        Player current = engine.getCurrentPlayer();

        boolean canBuildHouse = sf.canBuildHouse(current);
        buildHouseButton.setDisable(!canBuildHouse);
        buildHouseButton.setText("+ Domek (" + sf.getHousePrice() + "$)");

        boolean canBuildHotel = sf.canBuildHotel(current);
        buildHotelButton.setDisable(!canBuildHotel);
        buildHotelButton.setText("+ Hotel (" + sf.getHousePrice() + "$)");

        boolean canSellBuilding = sf.canSellBuilding();
        sellBuildingButton.setDisable(!canSellBuilding);
    }

    private void updateOwnedProperties(Player current) {
        ownedPropertiesMap.clear();
        String currentlySelected = managePropertyComboBox.getValue();
        managePropertyComboBox.getItems().clear();

        for (PurchaseField pf : current.getProperties()) {
            ownedPropertiesMap.put(pf.getName(), pf);
            managePropertyComboBox.getItems().add(pf.getName());
        }

        if (managePropertyComboBox.getItems().isEmpty()) {
            managePropertyComboBox.setDisable(true);
            managePropertyComboBox.setPromptText("Brak posiadłości");
            mortgageButton.setDisable(true);
            unmortgageButton.setDisable(true);
        } else {
            managePropertyComboBox.setDisable(false);
            managePropertyComboBox.setPromptText("Wybierz posiadłość...");
            if (currentlySelected != null && managePropertyComboBox.getItems().contains(currentlySelected)) {
                managePropertyComboBox.getSelectionModel().select(currentlySelected);
            } else {
                managePropertyComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    private void updateMortgageButtons(String selectedProperty) {
        if (selectedProperty == null || !ownedPropertiesMap.containsKey(selectedProperty)) {
            mortgageButton.setDisable(true);
            unmortgageButton.setDisable(true);
            return;
        }

        PurchaseField pf = ownedPropertiesMap.get(selectedProperty);
        Player current = engine.getCurrentPlayer();

        // W GameController:
        boolean canMortgage = pf.canBeMortgaged();
        mortgageButton.setDisable(!canMortgage);
        mortgageButton.setText("Zastaw (+" + pf.getMortgageValue() + "$)");

        boolean canUnmortgage = pf.canBeUnmortgagedBy(current);
        unmortgageButton.setDisable(!canUnmortgage);
        unmortgageButton.setText("Wykup (-" + pf.getUnmortgageCost() + "$)");
    }

    // ==========================================
    // IMPLEMENTACJA METOD OBSERVERA
    // ==========================================

    @Override
    public void onMessage(String message) {
        Platform.runLater(() -> gameLogsArea.appendText(message + "\n"));
    }

    @Override
    public void onPlayerMoved(Player player, int newPosition) {
        Platform.runLater(() -> boardRenderer.updatePawns(engine.getPlayers()));
    }

    @Override
    public void onPlayerStateChanged(Player player) {
        Platform.runLater(this::updatePlayerUI);
    }

    @Override
    public void onTradeRequested(TradeOffer offer) {
        // Logika z okienkami
    }

    @Override
    public void onCardDrawn(Player player, String deckType, String cardDescription) {
        // Używamy nowej struktury z DialogManager
    }
}