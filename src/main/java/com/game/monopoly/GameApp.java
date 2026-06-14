package com.game.monopoly;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Ładujemy nasz nowy plik FXML (zwróć uwagę na nazwę game-view.fxml)
        FXMLLoader fxmlLoader = new FXMLLoader(GameApp.class.getResource("game-view.fxml"));

        // Tworzymy scenę o wymiarach 1000x800 pikseli
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);

        // Ustawiamy tytuł okna
        stage.setTitle("Monopoly - Edycja JavaFX");

        // Podpinamy scenę pod główne okno (Stage) i wyświetlamy
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Ta metoda jest wywoływana jako pierwsza i uruchamia silnik JavaFX
        launch();
    }
}