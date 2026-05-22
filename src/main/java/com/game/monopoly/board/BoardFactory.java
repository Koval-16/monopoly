package com.game.monopoly.board;

import java.util.ArrayList;
import java.util.List;
import com.game.monopoly.board.action.*;
import com.game.monopoly.board.purchase.*;

/**
 * Klasa fabryczna odpowiedzialna za wygenerowanie kompletnej planszy do gry.
 * Zawiera w sobie logikę inicjalizacji wszystkich 40 pól (ulic, stacji, szans, podatków itp.).
 */
public class BoardFactory {

    /**
     * Tworzy i konfiguruje listę wszystkich pól na planszy w odpowiedniej kolejności.
     * Metoda jest statyczna, aby klasa Board mogła jej użyć bezpośrednio w swoim konstruktorze.
     * * @return Uzupełniona lista obiektów dziedziczących po Field (rozmiar 40)
     */
    public static List<Field> createBoard() {
        List<Field> fields = new ArrayList<>();

        // Indeksowanie od 0 do 39 – zgodnie z ruchem wskazówek zegara na klasycznej planszy

        // --- POZYCJE 0 - 9 ---
        fields.add(new StartField("Start", 0));

        // Brązowe (Cena: 60, Domek: 50)
        fields.add(new StreetField("Ulica Gancarska", 1, 60, 50, "Brązowa", new int[]{2, 10, 30, 90, 160, 250}));
        fields.add(new CardField("Kasa Społeczna", 2, "Kasa Społeczna"));
        fields.add(new StreetField("Ulica Główna", 3, 60, 50, "Brązowa", new int[]{4, 20, 60, 180, 320, 450}));

        fields.add(new TaxField("Podatek Dochodowy", 4, 200));
        fields.add(new RailroadField("Dworzec Zachodni", 5, 200));

        // Jasnoniebieskie (Cena: 100-120, Domek: 50)
        fields.add(new StreetField("Ulica Konopnickiej", 6, 100, 50, "Jasnoniebieska", new int[]{6, 30, 90, 270, 400, 550}));
        fields.add(new CardField("Szansa", 7, "Szansa"));
        fields.add(new StreetField("Ulica Stalowa", 8, 100, 50, "Jasnoniebieska", new int[]{6, 30, 90, 270, 400, 550}));
        fields.add(new StreetField("Ulica Korzenna", 9, 120, 50, "Jasnoniebieska", new int[]{8, 40, 100, 300, 450, 600}));

        // --- POZYCJE 10 - 19 ---
        fields.add(new JailField("Więzienie / Odwiedziny", 10));

        // Różowe (Cena: 140-160, Domek: 100)
        fields.add(new StreetField("Ulica Marszałkowska", 11, 140, 100, "Różowa", new int[]{10, 50, 150, 450, 625, 750}));
        fields.add(new UtilityField("Elektrownia", 12, 150));
        fields.add(new StreetField("Ulica Piękna", 13, 140, 100, "Różowa", new int[]{10, 50, 150, 450, 625, 750}));
        fields.add(new StreetField("Ulica Senatorska", 14, 160, 100, "Różowa", new int[]{12, 60, 180, 500, 700, 900}));

        fields.add(new RailroadField("Dworzec Gdański", 15, 200));

        // Pomarańczowe (Cena: 180-200, Domek: 100)
        fields.add(new StreetField("Ulica Świętokrzyska", 16, 180, 100, "Pomarańczowa", new int[]{14, 70, 200, 550, 750, 950}));
        fields.add(new CardField("Kasa Społeczna", 17, "Kasa Społeczna"));
        fields.add(new StreetField("Ulica Nowy Świat", 18, 180, 100, "Pomarańczowa", new int[]{14, 70, 200, 550, 750, 950}));
        fields.add(new StreetField("Aleje Jerozolimskie", 19, 200, 100, "Pomarańczowa", new int[]{16, 80, 220, 600, 800, 1000}));

        // --- POZYCJE 20 - 29 ---
        fields.add(new ParkingField("Bezpłatny Parking", 20));

        // Czerwone (Cena: 220-240, Domek: 150)
        fields.add(new StreetField("Ulica Towarowa", 21, 220, 150, "Czerwona", new int[]{18, 90, 250, 700, 875, 1050}));
        fields.add(new CardField("Szansa", 22, "Szansa"));
        fields.add(new StreetField("Ulica Chmielna", 23, 220, 150, "Czerwona", new int[]{18, 90, 250, 700, 875, 1050}));
        fields.add(new StreetField("Plac Trzech Krzyży", 24, 240, 150, "Czerwona", new int[]{20, 100, 300, 750, 925, 1100}));

        fields.add(new RailroadField("Dworzec Wschodni", 25, 200));

        // Żółte (Cena: 260-280, Domek: 150)
        fields.add(new StreetField("Ulica Świętojańska", 26, 260, 150, "Żółta", new int[]{22, 110, 330, 800, 975, 1150}));
        fields.add(new StreetField("Ulica Grodzka", 27, 260, 150, "Żółta", new int[]{22, 110, 330, 800, 975, 1150}));
        fields.add(new UtilityField("Wodociągi", 28, 150));
        fields.add(new StreetField("Ulica Floriańska", 29, 280, 150, "Żółta", new int[]{24, 120, 360, 850, 1025, 1200}));

        // --- POZYCJE 30 - 39 ---
        fields.add(new GoToJailField("Idziesz do Więzienia", 30));

        // Zielone (Cena: 300-320, Domek: 200)
        fields.add(new StreetField("Ulica Długa", 31, 300, 200, "Zielona", new int[]{26, 130, 390, 900, 1100, 1275}));
        fields.add(new StreetField("Ulica Miodowa", 32, 300, 200, "Zielona", new int[]{26, 130, 390, 900, 1100, 1275}));
        fields.add(new CardField("Kasa Społeczna", 33, "Kasa Społeczna"));
        fields.add(new StreetField("Ulica Krakowskie Przedmieście", 34, 320, 200, "Zielona", new int[]{28, 150, 450, 1000, 1200, 1400}));

        fields.add(new RailroadField("Dworzec Centralny", 35, 200));
        fields.add(new CardField("Szansa", 36, "Szansa"));

        // Ciemnoniebieskie (Cena: 350-400, Domek: 200)
        fields.add(new StreetField("Ulica Belwederska", 37, 350, 200, "Ciemnoniebieska", new int[]{35, 175, 500, 1100, 1300, 1500}));
        fields.add(new TaxField("Podatek od Luksusu", 38, 100));
        fields.add(new StreetField("Aleje Ujazdowskie", 39, 400, 200, "Ciemnoniebieska", new int[]{50, 200, 600, 1400, 1700, 2000}));

        return fields;
    }
}