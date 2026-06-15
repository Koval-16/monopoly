package com.game.monopoly.gui;

import com.game.monopoly.board.Field;
import com.game.monopoly.board.purchase.PurchaseField;
import com.game.monopoly.board.purchase.StreetField;
import com.game.monopoly.player.Player;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class BoardRenderer {

    private GridPane boardGrid;
    private VBox[] boardCells = new VBox[40];
    private HBox[] pawnContainers = new HBox[40];
    private HBox[] buildingContainers = new HBox[40];
    private HBox diceContainer;

    private final Color[] PLAYER_COLORS = {Color.RED, Color.DODGERBLUE, Color.LIMEGREEN, Color.GOLD};
    private final String[] PLAYER_HEX_COLORS = {"#FF0000", "#1E90FF", "#32CD32", "#FFD700"};

    public BoardRenderer(GridPane boardGrid) {
        this.boardGrid = boardGrid;
    }

    public void initBoard(List<Field> fields) {
        boardGrid.getChildren().clear();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            int col = 0, row = 0;

            if (i >= 0 && i <= 10) { row = 10; col = 10 - i; }
            else if (i > 10 && i <= 20) { col = 0; row = 20 - i; }
            else if (i > 20 && i <= 30) { row = 0; col = i - 20; }
            else if (i > 30 && i < 40) { col = 10; row = i - 30; }

            VBox fieldBox = new VBox();
            fieldBox.setAlignment(Pos.TOP_CENTER);
            fieldBox.setPrefSize(70, 70);
            fieldBox.setStyle("-fx-border-color: #333333; -fx-background-color: #fdfdfd; -fx-border-width: 1px;");

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

            Label nameLabel = new Label(field.getName());
            nameLabel.setWrapText(true);
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            nameLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 5px 0 0 0;");

            fieldBox.getChildren().add(nameLabel);

            HBox pawnBox = new HBox();
            pawnBox.setAlignment(Pos.CENTER);
            pawnBox.setSpacing(5);
            fieldBox.getChildren().add(pawnBox);

            boardCells[i] = fieldBox;
            pawnContainers[i] = pawnBox;
            boardGrid.add(fieldBox, col, row);
        }
    }

    public void updatePawns(List<Player> players) {
        for (HBox container : pawnContainers) {
            if (container != null) container.getChildren().clear();
        }
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int pos = p.getPosition();
            Circle pawn = new Circle(6);
            pawn.setFill(PLAYER_COLORS[i % PLAYER_COLORS.length]);
            pawn.setStroke(Color.BLACK);
            if (pawnContainers[pos] != null) {
                pawnContainers[pos].getChildren().add(pawn);
            }
        }
    }

    public void updateBoardBuildings(List<Field> fields) {
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            HBox container = buildingContainers[i];

            if (container != null && field instanceof StreetField) {
                container.getChildren().clear();
                StreetField street = (StreetField) field;

                if (street.hasHotel()) {
                    Rectangle hotel = new Rectangle(16, 10, Color.RED);
                    hotel.setStroke(Color.BLACK);
                    container.getChildren().add(hotel);
                } else {
                    for (int j = 0; j < street.getHouseCount(); j++) {
                        Rectangle house = new Rectangle(8, 8, Color.LIMEGREEN);
                        house.setStroke(Color.BLACK);
                        container.getChildren().add(house);
                    }
                }
            }
        }
    }

    public void updateBoardOwnership(List<Field> fields, List<Player> players) {
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (field instanceof PurchaseField) {
                Player owner = ((PurchaseField) field).getOwner();
                if (owner != null) {
                    int ownerIndex = players.indexOf(owner);
                    String colorHex = PLAYER_HEX_COLORS[ownerIndex % PLAYER_HEX_COLORS.length];
                    boardCells[i].setStyle("-fx-border-color: " + colorHex + "; -fx-background-color: #fdfdfd; -fx-border-width: 3px;");
                } else {
                    boardCells[i].setStyle("-fx-border-color: #333333; -fx-background-color: #fdfdfd; -fx-border-width: 1px;");
                }
            }
        }
    }

    public void drawDice(int r1, int r2) {
        if (diceContainer == null) {
            diceContainer = new HBox(15);
            diceContainer.setAlignment(Pos.CENTER);
        }
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