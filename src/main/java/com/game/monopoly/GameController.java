package com.game.monopoly;

import com.game.monopoly.economy.TradeOffer;
import com.game.monopoly.engine.GameEngine;
import com.game.monopoly.engine.GameObserver;
import com.game.monopoly.player.Player;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private HBox diceContainer; // Pudełko na kości na środku planszy

    // --- Silnik Gry ---
    private GameEngine engine;

    // TABLICE POMOCNICZE DO PIONKÓW:
    private VBox[] boardCells = new VBox[40];
    private HBox[] pawnContainers = new HBox[40]; // Pudełeczka na pionki wewnątrz każdego pola
    // PALETA KOLORÓW GRACZY (Odpowiednio: Gracz 1, 2, 3, 4)
    private final Color[] PLAYER_COLORS = {Color.RED, Color.DODGERBLUE, Color.LIMEGREEN, Color.GOLD};
    private final String[] PLAYER_HEX_COLORS = {"#FF0000", "#1E90FF", "#32CD32", "#FFD700"};

    @FXML
    public void initialize() {
        engine = new GameEngine();
        engine.addObserver(this);
        engine.start(Arrays.asList("Alojzy","Bernard","Cecylia","Danuta"));

        updatePlayerUI();

        // WYWOŁANIE NASZEGO NOWEGO KODU:
        drawBoard();
        updatePawns();
        drawDice(0, 0);
    }

    @FXML
    public void onRollDiceClicked() {
        engine.playTurn();
        drawDice(engine.getDice().getRoll1(), engine.getDice().getRoll2());

        // Jeśli gracz wyrzucił dublet, NIE wyłączamy mu przycisku rzutu!
        if (engine.getDice().isDouble()) {
            rollDiceButton.setDisable(false);
            endTurnButton.setDisable(true); // Wymuszamy kolejny rzut
            engine.notifyMessage("DUBLET! Rzucasz jeszcze raz.");
        } else {
            rollDiceButton.setDisable(true);
            endTurnButton.setDisable(false);
        }

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

            // Wyłączamy przycisk, bo pole jest już kupione
            buyPropertyButton.setDisable(true);

            // Odświeżamy GUI (pokaże nowy stan konta oraz nową kartę własności!)
            updatePlayerUI();

            // UWAGA: Usunięto drawBoard(), aby plansza i kości nie znikały!
        }
    }

    @FXML
    public void onBuildHouseClicked() {
        // Tym zajmiemy się za chwilę!
        engine.notifyMessage("Opcja budowania domków będzie wkrótce dostępna.");
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

            // Pobieramy pole, na którym aktualnie stoi gracz
            Field currentField = engine.getBoard().getFields().get(current.getPosition());

            // Domyślnie wyłączamy przyciski akcji
            buyPropertyButton.setDisable(true);
            buildHouseButton.setDisable(true);

            // LOGIKA: Czy gracz może kupić to pole?
            if (currentField instanceof PurchaseField) {
                PurchaseField property = (PurchaseField) currentField;
                // Jeśli pole nie ma właściciela i gracza na nie stać
                if (property.getOwner() == null && current.getBalance() >= property.getPrice()) {
                    buyPropertyButton.setDisable(false);
                    buyPropertyButton.setText("Kup za " + property.getPrice() + "$");
                }
            }

            updatePlayersSummary();
            updatePlayerCards(current);
            updateBoardOwnership();
            // (W przyszłości dodamy tu sprawdzanie, czy gracz ma pełne dzielnice, żeby włączyć buildHouseButton)
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
            card.setPrefSize(180, 100);
            card.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            card.setAlignment(Pos.TOP_CENTER);

            // Górny pasek z kolorem lub typem (Dworzec/Użyteczność)
            Label header = new Label();
            header.setPrefWidth(Double.MAX_VALUE);
            header.setPrefHeight(25);
            header.setAlignment(Pos.CENTER);

            if (property instanceof StreetField) {
                StreetField street = (StreetField) property;
                header.setStyle("-fx-background-color: " + getColorHex(street.getColorGroup()) + "; -fx-border-color: black; -fx-border-width: 0 0 2px 0;");
            } else {
                header.setText(property instanceof RailroadField ? "🚂 DWORZEC" : "💡 UŻYTECZNOŚĆ");
                header.setStyle("-fx-background-color: #dddddd; -fx-border-color: black; -fx-border-width: 0 0 2px 0; -fx-font-weight: bold;");
            }

            Label name = new Label(property.getName());
            name.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
            name.setWrapText(true);
            name.setTextAlignment(TextAlignment.CENTER);

            Label value = new Label("Wartość: " + property.getPrice() + "$");

            card.getChildren().addAll(header, name, value);
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


}