package com.game.monopoly.gui;

import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.board.purchase.RailroadField;
import com.game.monopoly.board.purchase.StreetField;
import com.game.monopoly.player.Player;
import com.game.monopoly.player.state.InJailState;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class PlayerPanelRenderer {

    private VBox playersSummaryBox;
    private VBox playerCardsBox;
    private final Color[] PLAYER_COLORS = {Color.RED, Color.DODGERBLUE, Color.LIMEGREEN, Color.GOLD};

    public PlayerPanelRenderer(VBox playersSummaryBox, VBox playerCardsBox) {
        this.playersSummaryBox = playersSummaryBox;
        this.playerCardsBox = playerCardsBox;
    }

    public void updatePlayersSummary(List<Player> players, Player currentPlayer) {
        playersSummaryBox.getChildren().clear();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String status = (p.getCurrentState() instanceof InJailState) ? " (Więzienie)" : "";

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Circle pawnColor = new Circle(6, PLAYER_COLORS[i % PLAYER_COLORS.length]);
            pawnColor.setStroke(Color.BLACK);

            Label pLabel = new Label(p.getName() + " - " + p.getBalance() + "$" + status);
            pLabel.setStyle("-fx-font-size: 13px;");

            if (p == currentPlayer) {
                pLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: blue;");
            }

            row.getChildren().addAll(pawnColor, pLabel);
            playersSummaryBox.getChildren().add(row);
        }
    }

    public void updatePlayerCards(Player current) {
        playerCardsBox.getChildren().clear();

        if (current.getProperties().isEmpty()) {
            playerCardsBox.getChildren().add(new Label("Brak nieruchomości."));
            return;
        }

        for (PurchaseField property : current.getProperties()) {
            VBox card = new VBox();
            card.setPrefWidth(210);
            card.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 0);");
            card.setAlignment(Pos.TOP_CENTER);

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

            Label name = new Label(property.getName() + (property.isMortgaged() ? "\n(ZASTAWIONA)" : ""));
            name.setStyle("-fx-font-weight: bold; -fx-padding: 5px; -fx-font-size: 13px;");
            if (property.isMortgaged()) name.setTextFill(Color.RED);
            name.setWrapText(true);
            name.setTextAlignment(TextAlignment.CENTER);

            VBox details = new VBox(2);
            details.setAlignment(Pos.CENTER);
            details.setStyle("-fx-padding: 5px 5px 10px 5px; -fx-font-size: 11px;");
            details.getChildren().add(new Label("Wartość: " + property.getPrice() + "$  |  Zastaw: " + (property.getPrice() / 2) + "$"));

            Separator sep1 = new Separator();
            sep1.setStyle("-fx-padding: 4px 0;");
            details.getChildren().add(sep1);

            if (property instanceof StreetField) {
                StreetField sf = (StreetField) property;
                int[] rents = sf.getRentPrices();
                details.getChildren().addAll(
                        new Label("Czynsz (pusty plac): " + rents[0] + "$"),
                        new Label("Z 1 domkiem: " + rents[1] + "$"),
                        new Label("Z 2 domkami: " + rents[2] + "$"),
                        new Label("Z 3 domkami: " + rents[3] + "$"),
                        new Label("Z 4 domkami: " + rents[4] + "$"),
                        new Label("Z Hotelem: " + rents[5] + "$")
                );
            } else if (property instanceof RailroadField) {
                details.getChildren().addAll(
                        new Label("Czynsz za 1 stację: 25$"),
                        new Label("Czynsz za 2 stacje: 50$"),
                        new Label("Czynsz za 3 stacje: 100$"),
                        new Label("Czynsz za 4 stacje: 200$")
                );
            } else {
                details.getChildren().addAll(
                        new Label("Czynsz (1 zakład): 4x rzut kośćmi"),
                        new Label("Czynsz (2 zakłady): 10x rzut kośćmi")
                );
            }

            card.getChildren().addAll(header, name, details);
            playerCardsBox.getChildren().add(card);
        }
    }

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
}