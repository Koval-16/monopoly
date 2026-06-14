package com.game.monopoly;

import com.game.monopoly.board.action.CardField;
import com.game.monopoly.economy.TradeOffer;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.GameObserver;
import com.game.monopoly.player.Player;
import com.game.monopoly.player.state.BankruptState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.paint.Color;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import com.game.monopoly.board.Field;

import com.game.monopoly.board.purchase.StreetField;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import java.util.List;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import com.game.monopoly.board.Field;
import com.game.monopoly.board.purchase.PurchaseField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import com.game.monopoly.board.purchase.RailroadField;
import com.game.monopoly.player.state.InJailState;
import javafx.scene.control.ComboBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.Arrays;

public class GameController implements GameObserver {

    // --- Elementy z pliku FXML ---
    @FXML private Label currentPlayerLabel;
    @FXML private Label balanceLabel;
    @FXML private Button rollDiceButton;
    @FXML private Button endTurnButton;
    @FXML private TextArea gameLogsArea;
    @FXML private GridPane boardGrid;
    @FXML private Button buyPropertyButton;
    @FXML private Button buildHouseButton;
    @FXML private VBox playersSummaryBox;
    @FXML private VBox playerCardsBox;
    @FXML private ComboBox<String> buildPropertyComboBox;
    @FXML private Button buildHotelButton;
    @FXML private Button auctionPropertyButton;
    @FXML private Button resolveFieldButton;
    @FXML private Button drawCardButton;
    @FXML private VBox jailActionsBox;
    @FXML private Button payBailButton;
    @FXML private Button useJailCardButton;
    @FXML private Button rollDoubleForJailButton;
    @FXML private Button tradeButton;
    @FXML private Button sellBuildingButton;
    @FXML private ComboBox<String> managePropertyComboBox;
    @FXML private Button mortgageButton;
    @FXML private Button unmortgageButton;

    private HBox diceContainer; // Pudełko na kości na środku planszy

    // --- Silnik Gry ---
    private GameEngine engine;
    private Map<String, PurchaseField> ownedPropertiesMap = new HashMap<>();
    // TABLICE POMOCNICZE DO PIONKÓW:
    private VBox[] boardCells = new VBox[40];
    private HBox[] pawnContainers = new HBox[40]; // Pudełeczka na pionki wewnątrz każdego pola
    // PALETA KOLORÓW GRACZY (Odpowiednio: Gracz 1, 2, 3, 4)
    private final Color[] PLAYER_COLORS = {Color.RED, Color.DODGERBLUE, Color.LIMEGREEN, Color.GOLD};
    private final String[] PLAYER_HEX_COLORS = {"#FF0000", "#1E90FF", "#32CD32", "#FFD700"};
    private Map<String, StreetField> buildablePropertiesMap = new HashMap<>();
    private HBox[] buildingContainers = new HBox[40];

    @FXML
    public void initialize() {
        engine = new GameEngine();
        engine.addObserver(this);
        engine.start(Arrays.asList("Alojzy","Bernard","Cecylia","Danuta"));

        // Nasłuchiwanie zmian na liście ulic (gdy gracz wybierze inną ulicę, odświeżamy przyciski)
        if (buildPropertyComboBox != null) {
            buildPropertyComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                updateBuildButtons(newVal);
            });
        }

        updatePlayerUI();
        drawBoard();
        updatePawns();
        drawDice(0, 0);
        updateBoardBuildings(); // Inicjujemy puste budynki
        if (managePropertyComboBox != null) {
            managePropertyComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                updateMortgageButtons(newVal);
            });
        }

    }

    @FXML
    public void onRollDiceClicked() {
        engine.playTurn();
        drawDice(engine.getDice().getRoll1(), engine.getDice().getRoll2());

        // WYMUSZAMY KLIKNIĘCIE "ROZPATRZ POLE" DLA KAŻDEGO RZUTU!
        rollDiceButton.setDisable(true);
        endTurnButton.setDisable(true);
        resolveFieldButton.setDisable(false);

        // Zabezpieczamy domyślny stan przycisków akcji
        buyPropertyButton.setDisable(true);
        auctionPropertyButton.setDisable(true);
        if(drawCardButton != null) drawCardButton.setDisable(true);

        updatePlayerUI();
    }

    @FXML
    public void onBuyPropertyClicked() {
        Player current = engine.getCurrentPlayer();
        Field currentField = engine.getBoard().getFields().get(current.getPosition());

        if (currentField instanceof PurchaseField) {
            PurchaseField property = (PurchaseField) currentField;

            // Pobieramy pieniądze i przypisujemy pole
            current.setBalance(current.getBalance() - property.getPrice());
            property.setOwner(current);
            current.addProperty(property);

            engine.notifyMessage(current.getName() + " kupuje " + property.getName() + "!");

            // Wyłączamy przyciski decyzyjne, bo pole jest już kupione
            buyPropertyButton.setDisable(true);
            auctionPropertyButton.setDisable(true); // <--- DODANE

            // Odświeżamy GUI
            updatePlayerUI();

            // ODBLOKOWANIE PRZYCISKU ZAKOŃCZENIA TURY (LUB RZUTU PRZY DUBLECIE)
            enableEndTurnOrDouble(); // <--- DODANE
        }
    }

    @FXML
    public void onBuildHouseClicked() {
        String selected = buildPropertyComboBox.getValue();
        if (selected != null && buildablePropertiesMap.containsKey(selected)) {
            StreetField sf = buildablePropertiesMap.get(selected);
            if (sf.buildHouse(engine)) {
                updatePlayerUI();
                updateBoardBuildings();
            }
        }
    }

    @FXML
    public void onBuildHotelClicked() {
        String selected = buildPropertyComboBox.getValue();
        if (selected != null && buildablePropertiesMap.containsKey(selected)) {
            StreetField sf = buildablePropertiesMap.get(selected);
            if (sf.buildHotel(engine)) {
                updatePlayerUI();
                updateBoardBuildings();
            }
        }
    }

    @FXML
    public void onResolveFieldClicked() {
        Player currentPlayer = engine.getCurrentPlayer();
        Field currentField = engine.getBoard().getFields().get(currentPlayer.getPosition());

        // TWORZYMY KONTEKST TURY, PRZEKAZUJĄC WYNIK OSTATNIEGO RZUTU KOŚĆMI
        com.game.monopoly.engine.TurnContext ctx = new com.game.monopoly.engine.TurnContext(engine.getDice());

        // 1. Wywołujemy właściwą akcję pola przekazując utworzony ctx zamiast null
        currentField.onLand(currentPlayer, ctx, engine);

        // 2. Blokujemy przycisk Rozpatrz Pole, bo już to zrobiliśmy
        resolveFieldButton.setDisable(true);

        // 3. Decydujemy, co odblokować w GUI na podstawie pola:
        // W metodzie onResolveFieldClicked() PODMIEŃ blok sprawdzający na ten poprawiony:
        if (currentField instanceof PurchaseField) {
            PurchaseField property = (PurchaseField) currentField;
            if (property.getOwner() == null) {
                buyPropertyButton.setDisable(currentPlayer.getBalance() < property.getPrice());
                auctionPropertyButton.setDisable(false);
                endTurnButton.setDisable(true);
                rollDiceButton.setDisable(true); // Dublet czy nie, musi podjąć decyzję
            } else {
                enableEndTurnOrDouble(); // Nowa metoda pomocnicza (niżej)
            }
        } else if (currentField instanceof CardField) {
            drawCardButton.setDisable(false);
            endTurnButton.setDisable(true);
            rollDiceButton.setDisable(true);
        } else {
            enableEndTurnOrDouble(); // Inne pola
        }

        // 4. Aktualizujemy widok portfela i logów
        updatePlayerUI();
    }

    @FXML
    public void onDrawCardClicked() {
        Player currentPlayer = engine.getCurrentPlayer();
        Field currentField = engine.getBoard().getFields().get(currentPlayer.getPosition());

        if (currentField instanceof CardField) {
            CardField cardField = (CardField) currentField;

            // Zakładam, że wprowadziłeś metodę drawCard(engine) w CardField
            com.game.monopoly.card.Card drawnCard = cardField.drawCard(engine);

            if(drawnCard != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Karta - " + cardField.getDeckType());
                alert.setHeaderText(currentPlayer.getName() + " wylosował kartę!");
                alert.setContentText(drawnCard.getDescription());
                alert.showAndWait();

                drawnCard.executeAction(currentPlayer, engine);
                cardField.returnCardToDeck(drawnCard, engine); // Odkładamy na spód

                engine.notifyMessage(currentPlayer.getName() + " rozpatrzył kartę.");
                drawCardButton.setDisable(true);

                updatePlayerUI();
                updateBoardBuildings();
                updatePawns();

                // Po przeczytaniu karty sprawdzamy dublety
                enableEndTurnOrDouble();
            }
        }
    }

    private void enableEndTurnOrDouble() {
        if (engine.getDice().isDouble()) {
            rollDiceButton.setDisable(false); // Gracz rzuca ponownie
            endTurnButton.setDisable(true);
            engine.notifyMessage("DUBLET! Rozpatrzyłeś pole, rzucasz jeszcze raz.");
        } else {
            endTurnButton.setDisable(false); // Normalny koniec tury
        }
    }

    @FXML
    public void onEndTurnClicked() {
        endTurnButton.setDisable(true);
        rollDiceButton.setDisable(false); // Nowy gracz może rzucić kośćmi

        engine.endTurn();
        updatePlayerUI();
    }

    private void updatePlayerUI() {
        Player current = engine.getCurrentPlayer();
        if (current != null) {
            currentPlayerLabel.setText("Tura: " + current.getName());
            balanceLabel.setText("Gotówka: " + current.getBalance() + "$");

            updatePlayersSummary();
            updatePlayerCards(current);
            updateBoardOwnership();
            updateBuildableProperties(current);
            updateOwnedProperties(current);
        }
    }

    // ==========================================
    // IMPLEMENTACJA METOD Z INTERFEJSU GameObserver
    // ==========================================

    @Override
    public void onMessage(String message) {
        // Platform.runLater upewnia się, że tekst dodajemy w wątku graficznym (bezpieczeństwo!)
        Platform.runLater(() -> {
            gameLogsArea.appendText(message + "\n");
        });
    }

    @Override
    public void onPlayerMoved(Player player, int newPosition) {
        Platform.runLater(() -> {
            // Aktualizujemy pionki na planszy!
            updatePawns();
            System.out.println("GUI: Pionek gracza " + player.getName() + " ląduje na polu " + newPosition);
        });
    }

    @Override
    public void onPlayerStateChanged(Player player) {
        Platform.runLater(() -> {
            // Tutaj np. nałożymy ikonkę krat, jeśli gracz trafił do więzienia
            updatePlayerUI();
        });
    }

    @Override
    public void onTradeRequested(TradeOffer offer) {
        Platform.runLater(() -> {
            // Tutaj wyrzucimy okienko Pop-up (Alert) z pytaniem o akceptację handlu
        });
    }

    // ==========================================
    // RYSOWANIE PLANSZY (FRONT-END)
    // ==========================================

    private void drawBoard() {
        // Upewniamy się, że siatka jest pusta przed rysowaniem
        boardGrid.getChildren().clear();

        List<Field> fields = engine.getBoard().getFields();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            // Matematyka siatki 11x11 dla Monopoly
            int col = 0;
            int row = 0;

            if (i >= 0 && i <= 10) {
                // Dolny rząd (od prawej do lewej)
                row = 10;
                col = 10 - i;
            } else if (i > 10 && i <= 20) {
                // Lewa kolumna (od dołu do góry)
                col = 0;
                row = 20 - i;
            } else if (i > 20 && i <= 30) {
                // Górny rząd (od lewej do prawej)
                row = 0;
                col = i - 20;
            } else if (i > 30 && i < 40) {
                // Prawa kolumna (od góry do dołu)
                col = 10;
                row = i - 30;
            }

            // --- TWORZENIE WIZUALNEGO POLA ---
            VBox fieldBox = new VBox();
            fieldBox.setAlignment(Pos.TOP_CENTER);
            fieldBox.setPrefSize(70, 70); // Rozmiar pojedynczego kafelka
            fieldBox.setStyle("-fx-border-color: #333333; -fx-background-color: #fdfdfd; -fx-border-width: 1px;");

            // Jeśli to "Ulica", rysujemy na górze pasek z kolorem!
            if (field instanceof StreetField) {
                StreetField street = (StreetField) field;
                String hexColor = getColorHex(street.getColorGroup());

                Label colorBar = new Label();
                colorBar.setPrefWidth(70);
                colorBar.setPrefHeight(15);
                colorBar.setStyle("-fx-background-color: " + hexColor + "; -fx-border-color: black; -fx-border-width: 0 0 1 0;");
                fieldBox.getChildren().add(colorBar);

                HBox bBox = new HBox();
                bBox.setAlignment(Pos.CENTER);
                bBox.setPrefHeight(10);
                bBox.setSpacing(2);
                fieldBox.getChildren().add(bBox);
                buildingContainers[i] = bBox;
            }

            // Nazwa pola (wyśrodkowana, mała czcionka, łamanie wierszy)
            Label nameLabel = new Label(field.getName());
            nameLabel.setWrapText(true);
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            nameLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 5px 0 0 0;");

            fieldBox.getChildren().add(nameLabel);

            // NOWE: Dodajemy poziomy pojemnik na pionki na dół pola
            HBox pawnBox = new HBox();
            pawnBox.setAlignment(Pos.CENTER);
            pawnBox.setSpacing(5); // Odstęp między pionkami, jeśli stoją na tym samym polu
            fieldBox.getChildren().add(pawnBox);

            // Zapisujemy referencje do tablic, żeby mieć do nich łatwy dostęp!
            boardCells[i] = fieldBox;
            pawnContainers[i] = pawnBox;

            // Dodajemy zbudowane pole do siatki FXML w odpowiednich współrzędnych
            boardGrid.add(fieldBox, col, row);
        }
    }

    // Prosty tłumacz z Twoich nazw kolorów na kolory HEX wyświetlane w JavaFX
    private String getColorHex(String colorName) {
        if (colorName == null) return "#FFFFFF";
        switch (colorName.toLowerCase()) {
            case "brązowa": return "#8B4513";
            case "jasnoniebieska": return "#87CEFA";
            case "różowa": return "#FF1493";
            case "pomarańczowa": return "#FFA500";
            case "czerwona": return "#FF0000";
            case "żółta": return "#FFFF00";
            case "zielona": return "#32CD32";
            case "granatowa": return "#000080";
            default: return "#CCCCCC";
        }
    }

    private void updatePawns() {
        // 1. Czyścimy wszystkie pionki z całej planszy
        for (HBox container : pawnContainers) {
            if (container != null) {
                container.getChildren().clear();
            }
        }

        // 2. Pobieramy graczy z silnika i rysujemy ich pionki
        List<Player> players = engine.getPlayers();
        Color[] playerColors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW}; // Kolory graczy

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int currentPosition = p.getPosition();

            // Tworzymy kółko (pionek) o promieniu 6 pikseli
            Circle pawn = new Circle(6);
            pawn.setFill(PLAYER_COLORS[i % PLAYER_COLORS.length]); // ZMIANA NA GLOBALNĄ PALETĘ
            pawn.setStroke(Color.BLACK); // Czarna obwódka

            // Wrzucamy pionek do odpowiedniego pojemnika na planszy
            if (pawnContainers[currentPosition] != null) {
                pawnContainers[currentPosition].getChildren().add(pawn);
            }
        }
    }

    // ==========================================
    // NOWE METODY RYSOWANIA (KOŚCI, KARTY, GRACZE)
    // ==========================================

    private void drawDice(int r1, int r2) {
        if (diceContainer == null) {
            diceContainer = new HBox(15);
            diceContainer.setAlignment(Pos.CENTER);
        }

        // ZABEZPIECZENIE: Jeśli plansza nie zawiera naszych kości, wrzucamy je tam na nowo!
        // (W siatce 11x11 środek planszy to kolumna 4, rząd 5)
        if (!boardGrid.getChildren().contains(diceContainer)) {
            boardGrid.add(diceContainer, 4, 5, 3, 1);
        }

        diceContainer.getChildren().clear();
        diceContainer.getChildren().addAll(createSingleDie(r1), createSingleDie(r2));
    }

    private StackPane createSingleDie(int value) {
        StackPane die = new StackPane();
        die.setPrefSize(60, 60);
        die.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0);");

        Label num = new Label(value == 0 ? "?" : String.valueOf(value));
        num.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        die.getChildren().add(num);
        return die;
    }

    private void updatePlayersSummary() {
        playersSummaryBox.getChildren().clear();
        List<Player> players = engine.getPlayers();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String status = (p.getCurrentState() instanceof InJailState) ? " (Więzienie)" : "";

            // Pojemnik w rzędzie na kółko + tekst
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            // Kolorowe kółeczko reprezentujące pionek
            Circle pawnColor = new Circle(6, PLAYER_COLORS[i % PLAYER_COLORS.length]);
            pawnColor.setStroke(Color.BLACK);

            // Tekst gracza
            Label pLabel = new Label(p.getName() + " - " + p.getBalance() + "$" + status);
            pLabel.setStyle("-fx-font-size: 13px;");

            // Pogrubiamy gracza, którego jest teraz tura
            if (p == engine.getCurrentPlayer()) {
                pLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: blue;");
            }

            row.getChildren().addAll(pawnColor, pLabel);
            playersSummaryBox.getChildren().add(row);
        }
    }

    private void updatePlayerCards(Player current) {
        playerCardsBox.getChildren().clear();

        if (current.getProperties().isEmpty()) {
            playerCardsBox.getChildren().add(new Label("Brak nieruchomości."));
            return;
        }

        for (PurchaseField property : current.getProperties()) {
            VBox card = new VBox();
            card.setPrefWidth(210); // Poszerzamy kartę, by zmieścić detale
            // Zauważ brak card.setPrefHeight() - dzięki temu karta sama wydłuży się w dół
            card.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            card.setAlignment(Pos.TOP_CENTER);

            // GÓRNY PASEK (KOLOR LUB TYP)
            Label header = new Label();
            header.setPrefWidth(Double.MAX_VALUE);
            header.setPrefHeight(20);
            header.setAlignment(Pos.CENTER);

            if (property instanceof StreetField) {
                StreetField street = (StreetField) property;
                header.setStyle("-fx-background-color: " + getColorHex(street.getColorGroup()) + "; -fx-border-color: black; -fx-border-width: 0 0 2px 0;");
            } else {
                header.setText(property instanceof RailroadField ? "🚂 DWORZEC" : "💡 UŻYTECZNOŚĆ");
                header.setStyle("-fx-background-color: #dddddd; -fx-border-color: black; -fx-border-width: 0 0 2px 0; -fx-font-weight: bold; -fx-font-size: 11px;");
            }

            // NAZWA POLA (I STAN ZASTAWU)
            Label name = new Label(property.getName() + (property.isMortgaged() ? "\n(ZASTAWIONA)" : ""));
            name.setStyle("-fx-font-weight: bold; -fx-padding: 5px; -fx-font-size: 13px;");
            if (property.isMortgaged()) name.setTextFill(Color.RED);
            name.setWrapText(true);
            name.setTextAlignment(TextAlignment.CENTER);

            // SEKCJA SZCZEGÓŁÓW (CZYNSZE I OPŁATY)
            VBox details = new VBox(2);
            details.setAlignment(Pos.CENTER);
            details.setStyle("-fx-padding: 5px 5px 10px 5px; -fx-font-size: 11px;");

            details.getChildren().add(new Label("Wartość: " + property.getPrice() + "$  |  Zastaw: " + (property.getPrice() / 2) + "$"));

            Separator sep1 = new Separator();
            sep1.setStyle("-fx-padding: 4px 0;");
            details.getChildren().add(sep1);

            // CZYNSZE W ZALEŻNOŚCI OD TYPU POLA
            if (property instanceof StreetField) {
                StreetField sf = (StreetField) property;
                int[] rents = sf.getRentPrices();

                details.getChildren().add(new Label("Czynsz (pusty plac): " + rents[0] + "$"));
                details.getChildren().add(new Label("Z 1 domkiem: " + rents[1] + "$"));
                details.getChildren().add(new Label("Z 2 domkami: " + rents[2] + "$"));
                details.getChildren().add(new Label("Z 3 domkami: " + rents[3] + "$"));
                details.getChildren().add(new Label("Z 4 domkami: " + rents[4] + "$"));
                details.getChildren().add(new Label("Z Hotelem: " + rents[5] + "$"));

                Separator sep2 = new Separator();
                sep2.setStyle("-fx-padding: 4px 0;");
                details.getChildren().add(sep2);

                details.getChildren().add(new Label("Koszt budowy domku/hotelu: " + sf.getHousePrice() + "$"));

            } else if (property instanceof RailroadField) {
                details.getChildren().add(new Label("Czynsz za 1 stację: 25$"));
                details.getChildren().add(new Label("Czynsz za 2 stacje: 50$"));
                details.getChildren().add(new Label("Czynsz za 3 stacje: 100$"));
                details.getChildren().add(new Label("Czynsz za 4 stacje: 200$"));

            } else {
                // UtilityField
                details.getChildren().add(new Label("Czynsz (1 zakład): 4x rzut kośćmi"));
                details.getChildren().add(new Label("Czynsz (2 zakłady): 10x rzut kośćmi"));
            }

            card.getChildren().addAll(header, name, details);
            playerCardsBox.getChildren().add(card);
        }
    }

    private void updateBoardOwnership() {
        List<Field> fields = engine.getBoard().getFields();
        List<Player> players = engine.getPlayers();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);

            // Sprawdzamy czy to pole można kupić i czy ma już właściciela
            if (field instanceof PurchaseField) {
                Player owner = ((PurchaseField) field).getOwner();
                if (owner != null) {
                    // Szukamy indeksu gracza, żeby dobrać odpowiedni kolor HEX
                    int ownerIndex = players.indexOf(owner);
                    String colorHex = PLAYER_HEX_COLORS[ownerIndex % PLAYER_HEX_COLORS.length];

                    // Zmieniamy ramkę kafelka na grubą (3px) w kolorze gracza
                    boardCells[i].setStyle("-fx-border-color: " + colorHex + "; -fx-background-color: #fdfdfd; -fx-border-width: 3px;");
                }
            }
        }
    }

    @Override
    public void onCardDrawn(Player player, String deckType, String cardDescription) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Karta - " + deckType);
            alert.setHeaderText(player.getName() + " wyciąga kartę!");
            alert.setContentText(cardDescription);

            // showAndWait() to magia! Blokuje całe główne okno gry (nie da się kliknąć
            // 'Zakończ Turę'), dopóki gracz nie przeczyta karty i nie kliknie OK.
            alert.showAndWait();

            // Dopiero po kliknięciu OK, odświeżamy interfejs (bo backend już
            // pobrał graczowi pieniądze albo przesunął jego pionek)
            updatePlayerUI();
            updatePawns();
        });
    }

    private void updateBuildableProperties(Player current) {
        buildablePropertiesMap.clear();
        String currentlySelected = buildPropertyComboBox.getValue();
        buildPropertyComboBox.getItems().clear();

        // Grupujemy ulice gracza według koloru
        Map<String, List<StreetField>> colorMap = new HashMap<>();
        for (PurchaseField pf : current.getProperties()) {
            if (pf instanceof StreetField) {
                StreetField sf = (StreetField) pf;
                colorMap.computeIfAbsent(sf.getColorGroup(), k -> new ArrayList<>()).add(sf);
            }
        }

        // Sprawdzamy, które zbiory kolorów są pełne
        for (Map.Entry<String, List<StreetField>> entry : colorMap.entrySet()) {
            String color = entry.getKey();
            int required = (color.equals("Brązowa") || color.equals("Ciemnoniebieska")) ? 2 : 3;

            if (entry.getValue().size() == required) {
                // Monopol osiągnięty! Dodajemy wszystkie ulice z tego koloru na listę
                for (StreetField sf : entry.getValue()) {
                    buildablePropertiesMap.put(sf.getName(), sf);
                    buildPropertyComboBox.getItems().add(sf.getName());
                }
            }
        }

        if (buildPropertyComboBox.getItems().isEmpty()) {
            buildPropertyComboBox.setDisable(true);
            buildPropertyComboBox.setPromptText("Brak pełnych dzielnic");
            buildHouseButton.setDisable(true);
            buildHotelButton.setDisable(true);
        } else {
            buildPropertyComboBox.setDisable(false);
            buildPropertyComboBox.setPromptText("Wybierz ulicę...");

            // Zaznaczamy to samo, co wcześniej, żeby nie zgubić wyboru po rzucie
            if (currentlySelected != null && buildPropertyComboBox.getItems().contains(currentlySelected)) {
                buildPropertyComboBox.getSelectionModel().select(currentlySelected);
            } else {
                buildPropertyComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    // Aktywowanie/Dezaktywowanie przycisków na podstawie portfela
    private void updateBuildButtons(String selectedStreetName) {
        if (selectedStreetName == null || !buildablePropertiesMap.containsKey(selectedStreetName)) {
            buildHouseButton.setDisable(true);
            buildHotelButton.setDisable(true);

            return;
        }

        StreetField sf = buildablePropertiesMap.get(selectedStreetName);
        Player current = engine.getCurrentPlayer();

        // Można budować domek: nie ma hotelu, domków jest mniej niż 4, i stać nas
        boolean canBuildHouse = !sf.hasHotel() && sf.getHouseCount() < 4 && current.getBalance() >= sf.getHousePrice();
        buildHouseButton.setDisable(!canBuildHouse);
        buildHouseButton.setText("+ Domek (" + sf.getHousePrice() + "$)");

        // Można budować hotel: nie ma hotelu, domków jest dokładnie 4, i stać nas
        boolean canBuildHotel = !sf.hasHotel() && sf.getHouseCount() == 4 && current.getBalance() >= sf.getHousePrice();
        buildHotelButton.setDisable(!canBuildHotel);
        buildHotelButton.setText("+ Hotel (" + sf.getHousePrice() + "$)");

        // Pod linią włączania buildHotelButton dopisz:
        boolean canSellBuilding = sf.getHouseCount() > 0 || sf.hasHotel();
        sellBuildingButton.setDisable(!canSellBuilding);
    }

    // Renderowanie grafiki na kwadraciku planszy
    private void updateBoardBuildings() {
        List<Field> fields = engine.getBoard().getFields();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            HBox container = buildingContainers[i];

            if (container != null && field instanceof StreetField) {
                container.getChildren().clear();
                StreetField street = (StreetField) field;

                if (street.hasHotel()) {
                    // Rysujemy czerwony prostokąt - Hotel
                    Rectangle hotel = new Rectangle(16, 10, Color.RED);
                    hotel.setStroke(Color.BLACK);
                    container.getChildren().add(hotel);
                } else {
                    // Rysujemy zielone kwadraciki - Domki
                    for (int j = 0; j < street.getHouseCount(); j++) {
                        Rectangle house = new Rectangle(8, 8, Color.LIMEGREEN);
                        house.setStroke(Color.BLACK);
                        container.getChildren().add(house);
                    }
                }
            }
        }
    }

    @FXML
    public void onAuctionPropertyClicked() {
        Player activePlayer = engine.getCurrentPlayer(); // Gracz, który stoi na polu

        // 1. Stwórz listę wszystkich graczy
        List<Player> allPlayers = new ArrayList<>(engine.getPlayers());

        // 2. Usuń z listy gracza, który stoi na polu (on nie może licytować)
        allPlayers.remove(activePlayer);

        // 3. Usuń bankrutów (opcjonalnie)
        allPlayers.removeIf(p -> p.getCurrentState() instanceof BankruptState);

        // 4. Jeśli po odfiltrowaniu nie ma nikogo, kto mógłby licytować, wyświetl info
        if (allPlayers.isEmpty()) {
            showAlert("Licytacja", "Brak graczy uprawnionych do licytacji!");
            enableEndTurnOrDouble(); // Zakończ turę, skoro nikt nie licytuje
            return;
        }

        // 5. Teraz wywołaj aukcję tylko dla pozostałych graczy
        Field currentField = engine.getBoard().getFields().get(activePlayer.getPosition());
        if (currentField instanceof PurchaseField) {
            startAuctionUI((PurchaseField) currentField, allPlayers);
        }

        enableEndTurnOrDouble();
    }

    private void startAuctionUI(PurchaseField property, List<Player> participants) {
        List<Player> activeBidders = new ArrayList<>(participants);
        // Usuń z licytacji bankrutów
        activeBidders.removeIf(p -> p.getCurrentState() instanceof BankruptState); // Zgodnie z diagramem stanów

        int currentBid = property.getPrice() / 2; // Licytacja zaczyna się od połowy ceny
        Player highestBidder = null;
        int bidderIndex = 0;

        while (activeBidders.size() > 1 || (activeBidders.size() == 1 && highestBidder == null)) {
            Player biddingPlayer = activeBidders.get(bidderIndex);

            TextInputDialog dialog = new TextInputDialog(String.valueOf(currentBid + 10));
            dialog.setTitle("Licytacja: " + property.getName());
            dialog.setHeaderText("Aktualna cena: " + currentBid + "$\nNajwyższa oferta: " + (highestBidder != null ? highestBidder.getName() : "Brak"));
            dialog.setContentText("Gracz " + biddingPlayer.getName() + " (Stan konta: " + biddingPlayer.getBalance() + "$)\nPodaj kwotę (lub zostaw puste by spasować):");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent() && !result.get().trim().isEmpty()) {
                try {
                    int bid = Integer.parseInt(result.get().trim());
                    if (bid > currentBid && bid <= biddingPlayer.getBalance()) {
                        currentBid = bid;
                        highestBidder = biddingPlayer;
                        bidderIndex = (bidderIndex + 1) % activeBidders.size();
                    } else {
                        showAlert("Błąd", "Oferta musi być większa niż " + currentBid + "$ i nie może przekraczać twojego budżetu!");
                        // Nie zmieniamy indeksu, gracz próbuje jeszcze raz
                    }
                } catch (NumberFormatException e) {
                    activeBidders.remove(bidderIndex); // Błędny format = pasuje
                    if (bidderIndex >= activeBidders.size()) bidderIndex = 0;
                }
            } else {
                // Gracz pasuje
                activeBidders.remove(bidderIndex);
                if (bidderIndex >= activeBidders.size() && !activeBidders.isEmpty()) {
                    bidderIndex = 0;
                }
            }
        }

        if (highestBidder != null) {
            highestBidder.payMoney(currentBid);
            property.setOwner(highestBidder);
            highestBidder.addProperty(property);
            showAlert("Koniec Licytacji", "Gracz " + highestBidder.getName() + " wygrywa licytację za " + currentBid + "$!");
        } else {
            showAlert("Koniec Licytacji", "Nikt nie kupił nieruchomości.");
        }
        buyPropertyButton.setDisable(true);
        auctionPropertyButton.setDisable(true);
        resolveFieldButton.setDisable(true);
        enableEndTurnOrDouble(); // Odblokowujemy koniec tury
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ==========================================
    // OBSŁUGA WIĘZIENIA
    // ==========================================

    @FXML
    public void onPayBailClicked() {
        Player current = engine.getCurrentPlayer();
        current.payMoney(100);

        // Zmiana stanu na aktywny
        current.changeState(new com.game.monopoly.player.state.ActiveState());
        engine.notifyMessage(current.getName() + " wpłaca 100$ kaucji. Jest wolny i w następnej turze będzie mógł grać.");

        endJailTurn();
    }

    @FXML
    public void onUseJailCardClicked() {
        Player current = engine.getCurrentPlayer();
        current.removeOutOfJailCards(1);

        current.changeState(new com.game.monopoly.player.state.ActiveState());
        engine.notifyMessage(current.getName() + " używa karty wyjścia z więzienia! Jest wolny.");

        endJailTurn();
    }

    @FXML
    public void onRollDoubleForJailClicked() {
        Player current = engine.getCurrentPlayer();

        engine.getDice().roll();
        int r1 = engine.getDice().getRoll1();
        int r2 = engine.getDice().getRoll2();
        drawDice(r1, r2);

        if (engine.getDice().isDouble()) {
            current.changeState(new com.game.monopoly.player.state.ActiveState());
            engine.notifyMessage(current.getName() + " wyrzuca dublet (" + r1 + ", " + r2 + ")! Wychodzi z więzienia.");
        } else {
            engine.notifyMessage(current.getName() + " wyrzuca (" + r1 + ", " + r2 + "). Brak dubletu, pozostaje w więzieniu.");
        }

        endJailTurn();
    }

    private void endJailTurn() {
        if (jailActionsBox != null) jailActionsBox.setDisable(true); // Gracz dokonał wyboru, blokujemy klikanie
        endTurnButton.setDisable(false); // Pozwalamy zakończyć turę
        updatePlayerUI();
    }

    // ==========================================
    // NOWE: ZARZĄDZANIE WŁASNOŚCIĄ I BUDYNKAMI
    // ==========================================

    @FXML
    public void onSellBuildingClicked() {
        String selected = buildPropertyComboBox.getValue();
        if (selected != null && buildablePropertiesMap.containsKey(selected)) {
            StreetField sf = buildablePropertiesMap.get(selected);
            if (sf.hasHotel()) {
                sf.sellHotel(engine);
            } else if (sf.getHouseCount() > 0) {
                sf.sellHouse(engine);
            }
            updatePlayerUI();
            updateBoardBuildings();
        }
    }

    @FXML
    public void onMortgageClicked() {
        String selected = managePropertyComboBox.getValue();
        if (selected != null && ownedPropertiesMap.containsKey(selected)) {
            PurchaseField pf = ownedPropertiesMap.get(selected);
            pf.mortgage(); // Metoda z Twojego backendu!
            engine.notifyMessage(engine.getCurrentPlayer().getName() + " zastawia " + pf.getName() + " za " + (pf.getPrice()/2) + "$.");
            updatePlayerUI();
        }
    }

    @FXML
    public void onUnmortgageClicked() {
        String selected = managePropertyComboBox.getValue();
        if (selected != null && ownedPropertiesMap.containsKey(selected)) {
            PurchaseField pf = ownedPropertiesMap.get(selected);
            pf.unmortgage(); // Metoda z Twojego backendu!
            engine.notifyMessage(engine.getCurrentPlayer().getName() + " wykupuje z zastawu " + pf.getName() + ".");
            updatePlayerUI();
        }
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

        // Nie można zastawić ulicy, na której są budynki!
        boolean hasBuildings = false;
        if (pf instanceof StreetField) {
            StreetField sf = (StreetField) pf;
            hasBuildings = sf.getHouseCount() > 0 || sf.hasHotel();
        }

        // Aktywacja Zastaw
        boolean canMortgage = !pf.isMortgaged() && !hasBuildings;
        mortgageButton.setDisable(!canMortgage);
        mortgageButton.setText("Zastaw (+" + (pf.getPrice()/2) + "$)");

        // Aktywacja Wykup (koszt + 10%)
        int unmortgageCost = (pf.getPrice() / 2) + (int)((pf.getPrice() / 2) * 0.1);
        boolean canUnmortgage = pf.isMortgaged() && current.getBalance() >= unmortgageCost;
        unmortgageButton.setDisable(!canUnmortgage);
        unmortgageButton.setText("Wykup (-" + unmortgageCost + "$)");
    }

    // ==========================================
    // NOWE: SYSTEM HANDLU MIĘDZY GRACZAMI
    // ==========================================

    @FXML
    public void onTradeClicked() {
        Player initiator = engine.getCurrentPlayer();
        List<Player> possibleTargets = new ArrayList<>(engine.getPlayers());
        possibleTargets.remove(initiator);
        possibleTargets.removeIf(p -> p.getCurrentState() instanceof BankruptState);

        if (possibleTargets.isEmpty()) {
            showAlert("Handel", "Brak innych graczy w grze.");
            return;
        }

        // 1. KROK - Wybierz gracza do handlu
        javafx.scene.control.ChoiceDialog<Player> targetDialog = new javafx.scene.control.ChoiceDialog<>(possibleTargets.get(0), possibleTargets);
        targetDialog.setTitle("Propozycja handlu");
        targetDialog.setHeaderText("Z kim chcesz handlować?");
        targetDialog.setContentText("Wybierz gracza:");

        Optional<Player> result = targetDialog.showAndWait();
        if (result.isPresent()) {
            Player target = result.get();
            showDetailedTradeDialog(initiator, target);
        }
    }

    private void showDetailedTradeDialog(Player initiator, Player target) {
        javafx.scene.control.Dialog<TradeOffer> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Konfiguracja Handlu");
        dialog.setHeaderText("Handel pomiędzy: " + initiator.getName() + " a " + target.getName());

        javafx.scene.control.ButtonType proposeButtonType = new javafx.scene.control.ButtonType("Zaproponuj", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(proposeButtonType, javafx.scene.control.ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        // --- LEWA STRONA (Inicjator - "Ty oferujesz") ---
        grid.add(new Label("Ty oferujesz:"), 0, 0);

        javafx.scene.control.TextField offeredMoneyField = new javafx.scene.control.TextField("0");
        grid.add(new Label("Gotówka ($):"), 0, 1);
        grid.add(offeredMoneyField, 1, 1);

        javafx.scene.control.TextField offeredCardsField = new javafx.scene.control.TextField("0");
        grid.add(new Label("Karty z więzienia (Masz: " + initiator.getOutOfJailCards() + "):"), 0, 2);
        grid.add(offeredCardsField, 1, 2);

        javafx.scene.control.ListView<PurchaseField> offeredProps = new javafx.scene.control.ListView<>();
        offeredProps.getItems().addAll(initiator.getProperties());
        offeredProps.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        offeredProps.setPrefHeight(150);
        offeredProps.setPrefWidth(200);

        // Zmieniamy domyślne referencje obiektów na ładne nazwy ulic
        offeredProps.setCellFactory(param -> new javafx.scene.control.ListCell<PurchaseField>() {
            @Override
            protected void updateItem(PurchaseField item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); } else { setText(item.getName()); }
            }
        });

        grid.add(new Label("Nieruchomości:\n(Ctrl/Shift - wiele)"), 0, 3);
        grid.add(offeredProps, 1, 3);

        // --- PRAWA STRONA (Cel handlu - "Żądasz") ---
        grid.add(new Label("Oczekujesz od " + target.getName() + ":"), 2, 0);

        javafx.scene.control.TextField requestedMoneyField = new javafx.scene.control.TextField("0");
        grid.add(new Label("Gotówka ($):"), 2, 1);
        grid.add(requestedMoneyField, 3, 1);

        javafx.scene.control.TextField requestedCardsField = new javafx.scene.control.TextField("0");
        grid.add(new Label("Karty z więzienia (Ma: " + target.getOutOfJailCards() + "):"), 2, 2);
        grid.add(requestedCardsField, 3, 2);

        javafx.scene.control.ListView<PurchaseField> requestedProps = new javafx.scene.control.ListView<>();
        requestedProps.getItems().addAll(target.getProperties());
        requestedProps.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        requestedProps.setPrefHeight(150);
        requestedProps.setPrefWidth(200);

        requestedProps.setCellFactory(param -> new javafx.scene.control.ListCell<PurchaseField>() {
            @Override
            protected void updateItem(PurchaseField item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); } else { setText(item.getName()); }
            }
        });

        grid.add(new Label("Nieruchomości:\n(Ctrl/Shift - wiele)"), 2, 3);
        grid.add(requestedProps, 3, 3);

        dialog.getDialogPane().setContent(grid);

        // Zapisywanie nazw do podsumowania
        List<String> offeredNames = new ArrayList<>();
        List<String> requestedNames = new ArrayList<>();

        // Przekonwertowanie wpisanych danych na obiekt TradeOffer
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == proposeButtonType) {
                TradeOffer offer = new TradeOffer(initiator, target);

                try { offer.setOfferedMoney(Integer.parseInt(offeredMoneyField.getText().trim())); } catch(Exception e){}
                try { offer.setRequestedMoney(Integer.parseInt(requestedMoneyField.getText().trim())); } catch(Exception e){}
                try { offer.setOfferedJailCards(Integer.parseInt(offeredCardsField.getText().trim())); } catch(Exception e){}
                try { offer.setRequestedJailCards(Integer.parseInt(requestedCardsField.getText().trim())); } catch(Exception e){}

                for(PurchaseField pf : offeredProps.getSelectionModel().getSelectedItems()) {
                    offer.addOfferedProperty(pf);
                    offeredNames.add(pf.getName());
                }
                for(PurchaseField pf : requestedProps.getSelectionModel().getSelectedItems()) {
                    offer.addRequestedProperty(pf);
                    requestedNames.add(pf.getName());
                }
                return offer;
            }
            return null;
        });

        // Wyświetlenie podsumowania dla gracza celowanego
        Optional<TradeOffer> result = dialog.showAndWait();
        result.ifPresent(offer -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Weryfikacja Handlu");
            confirmDialog.setHeaderText("Gracz " + target.getName() + ", czy akceptujesz układ?");

            StringBuilder sb = new StringBuilder();
            sb.append(initiator.getName()).append(" zaoferował:\n");
            sb.append("- Gotówka: ").append(offer.getOfferedMoney()).append("$\n");
            sb.append("- Karty z więzienia: ").append(offer.getOfferedJailCards()).append("\n");
            sb.append("- Nieruchomości: ").append(offeredNames.isEmpty() ? "Brak" : String.join(", ", offeredNames)).append("\n\n");

            sb.append(initiator.getName()).append(" żąda od Ciebie:\n");
            sb.append("- Gotówka: ").append(offer.getRequestedMoney()).append("$\n");
            sb.append("- Karty z więzienia: ").append(offer.getRequestedJailCards()).append("\n");
            sb.append("- Nieruchomości: ").append(requestedNames.isEmpty() ? "Brak" : String.join(", ", requestedNames)).append("\n");

            confirmDialog.setContentText(sb.toString());

            Optional<javafx.scene.control.ButtonType> confirmResult = confirmDialog.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == javafx.scene.control.ButtonType.OK) {
                // Ewaluacja po kliknięciu OK
                if (offer.isValid()) {
                    offer.execute();
                    engine.notifyMessage("Wymiana handlowa zakończona sukcesem!");
                    updatePlayerUI();
                } else {
                    showAlert("Błąd handlu", "Oferta jest nielegalna!\nBrak środków, kart wyjścia z więzienia, lub próba handlu ulicą z wybudowanymi budynkami.");
                }
            } else {
                engine.notifyMessage(target.getName() + " odrzuca ofertę handlową.");
            }
        });
    }

}