module com.game.monopoly {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.game.monopoly to javafx.fxml;

    exports com.game.monopoly;
    exports com.game.monopoly.board;
    exports com.game.monopoly.board.action;
    exports com.game.monopoly.board.purchase;
    exports com.game.monopoly.card;
    exports com.game.monopoly.economy;
    exports com.game.monopoly.engine;
    exports com.game.monopoly.player;
    exports com.game.monopoly.player.state;
}